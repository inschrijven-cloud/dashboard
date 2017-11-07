package be.thomastoye.speelsysteem.data.couchdb

import javax.inject.{Inject, Singleton}

import be.thomastoye.speelsysteem.data.TenantDatabaseService
import be.thomastoye.speelsysteem.data.couchdb.CouchTenantDatabaseService.ReplicationDocument
import be.thomastoye.speelsysteem.models.DbName
import com.ibm.couchdb.Res.DocOk
import com.ibm.couchdb.{CouchDb, CouchDesign, CouchException, Res, TypeMapping}
import com.typesafe.scalalogging.StrictLogging
import delorean._
import com.netaporter.uri.dsl._
import play.api.libs.json.{Format, JsValue, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

object CouchTenantDatabaseService {
  case class ReplicationDocument(source: String, target: String)
}

@Singleton
class CouchTenantDatabaseService @Inject()(wsClient: WSClient, couchdbConfig: CouchDbConfig)(implicit ec: ExecutionContext)
    extends TenantDatabaseService with StrictLogging {

  implicit val replicationDocumentForm: Format[ReplicationDocument] = Json.format[ReplicationDocument]

  private val client = (for {
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

  override def all: Future[Seq[DbName]] = client.dbs.getAll.unsafeToFuture().map(_.map(DbName.create(_).get))

  override def create(db: DbName): Future[Res.Ok] = client.dbs.create(db.value).unsafeToFuture()

  override def details(db: DbName): Future[Res.DbInfo] = client.dbs.get(db.value).unsafeToFuture()

  override def drop(db: DbName): Future[Res.Ok] = client.dbs.delete(db.value).unsafeToFuture()

  override def designDocExists(dbName: DbName, designName: String): Future[Option[String]] = {
    client.db(dbName.value, TypeMapping.empty).design.get(designName).unsafeToFuture().map(t => Some(t._rev)).recover {
      case e: CouchException[_] => None
    }
  }

  override def createDesignDoc(db: DbName, couchDesign: CouchDesign): Future[DocOk] = {
    client.db(db.value, TypeMapping.empty).design.create(couchDesign).unsafeToFuture()
  }

  override def startReplicationToRemote(db: DbName, target: CouchDbConfig): Future[JsValue] = {
    val url = target.uri / db.value
    val document = Json.toJson(ReplicationDocument(db.value, url))

    logger.info("Starting replication local --> remote. Replication document to post: " + document)

    wsClient.url(couchdbConfig.uri / "_replicate").post(document).map(_.json)
  }

  override def startReplicationFromRemote(db: CouchDbConfig, target: DbName): Future[JsValue] = {
    val url = db.uri / target.value
    val document = Json.toJson(ReplicationDocument(url, target.value))

    logger.info("Starting replication remote --> local. Replication document to post: " + document)

    wsClient.url(couchdbConfig.uri / "_replicate").post(document).map(_.json)
  }
}
