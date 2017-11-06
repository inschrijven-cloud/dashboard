package be.thomastoye.speelsysteem.dashboard.services

import javax.inject.{ Inject, Singleton }

import be.thomastoye.speelsysteem.models.DbName
import com.ibm.couchdb.Res.DocOk
import play.api.Configuration
import com.ibm.couchdb._
import delorean._
import com.netaporter.uri.dsl._
import com.netaporter.uri.Uri
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.{ Format, JsValue, Json }
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait TenantDatabaseService {
  def all: Future[Seq[DbName]]

  def create(db: DbName): Future[Res.Ok]

  def details(db: DbName): Future[Res.DbInfo]

  def drop(db: DbName): Future[Res.Ok]

  /**
   * Check if a design doc exists
   * @return Maybe the rev of the existing design doc
   */
  def designDocExists(db: DbName, designName: String): Future[Option[String]]

  def createDesignDoc(db: DbName, couchDesign: CouchDesign): Future[DocOk]

  def startReplicationToRemote(db: DbName, target: CouchDBConfig): Future[JsValue]

  def startReplicationFromRemote(db: CouchDBConfig, target: DbName): Future[JsValue]
}

trait CouchDBConfig {
  val host: String
  val port: Int
  val https: Boolean
  val user: Option[String]
  val pass: Option[String]

  def uri: Uri = {
    val uri = Uri.empty
      .withScheme(if (https) "https" else "http")
      .withHost(host)
      .withPort(port)

    (for {
      u <- user
      p <- pass
    } yield uri.withUser(u).withPassword(p)).getOrElse(uri)
  }
}

case class AutoCouchDBConfig @Inject() (configuration: Configuration) extends CouchDBConfig {
  lazy val host: String = configuration.get[String]("couchdb.server.host")
  lazy val port: Int = configuration.get[Int]("couchdb.server.port")
  lazy val https: Boolean = configuration.getOptional[Boolean]("couchdb.server.https").getOrElse(true)
  lazy val user: Option[String] = configuration.getOptional[String]("couchdb.server.user")
  lazy val pass: Option[String] = configuration.getOptional[String]("couchdb.server.pass")
}

case class AutoRemoteCouchDBConfig @Inject() (configuration: Configuration) extends CouchDBConfig {
  lazy val host: String = configuration.get[String]("couchdb.remote.host")
  lazy val port: Int = configuration.get[Int]("couchdb.remote.port")
  lazy val https: Boolean = configuration.getOptional[Boolean]("couchdb.remote.https").getOrElse(true)
  lazy val user: Option[String] = configuration.getOptional[String]("couchdb.remote.user")
  lazy val pass: Option[String] = configuration.getOptional[String]("couchdb.remote.pass")
}

case class ReplicationDocument(source: String, target: String)

@Singleton
class CouchdbTenantDatabaseService @Inject() (wsClient: WSClient, couchdbConfig: CouchDBConfig)
    extends TenantDatabaseService with StrictLogging {

  implicit val replicationDocumentForm: Format[ReplicationDocument] = Json.format[ReplicationDocument]

  val client = (for {
    user <- couchdbConfig.user
    pass <- couchdbConfig.pass
  } yield {
    CouchDb(
      couchdbConfig.host,
      couchdbConfig.port,
      https = couchdbConfig.https,
      user,
      pass
    )
  }) getOrElse CouchDb(
    couchdbConfig.host,
    couchdbConfig.port,
    https = couchdbConfig.https
  )

  override def all = client.dbs.getAll.unsafeToFuture().map(_.map(DbName.create(_).get))

  override def create(db: DbName) = client.dbs.create(db.value).unsafeToFuture()

  override def details(db: DbName) = client.dbs.get(db.value).unsafeToFuture()

  override def drop(db: DbName) = client.dbs.delete(db.value).unsafeToFuture()

  override def designDocExists(dbName: DbName, designName: String): Future[Option[String]] = {
    client.db(dbName.value, TypeMapping.empty).design.get(designName).unsafeToFuture().map(t => Some(t._rev)).recover {
      case e: CouchException[_] => None
    }
  }

  override def createDesignDoc(db: DbName, couchDesign: CouchDesign): Future[DocOk] = {
    client.db(db.value, TypeMapping.empty).design.create(couchDesign).unsafeToFuture()
  }

  override def startReplicationToRemote(db: DbName, target: CouchDBConfig): Future[JsValue] = {
    val url = target.uri / db.value
    val document = Json.toJson(ReplicationDocument(db.value, url))

    logger.info("Starting replication local --> remote. Replication document to post: " + document)

    wsClient.url(couchdbConfig.uri / "_replicate").post(document).map(_.json)
  }

  override def startReplicationFromRemote(db: CouchDBConfig, target: DbName): Future[JsValue] = {
    val url = db.uri / target.value
    val document = Json.toJson(ReplicationDocument(url, target.value))

    logger.info("Starting replication remote --> local. Replication document to post: " + document)

    wsClient.url(couchdbConfig.uri / "_replicate").post(document).map(_.json)
  }
}
