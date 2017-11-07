package be.thomastoye.speelsysteem.data.couchdb

import javax.inject.{ Inject, Singleton }

import be.thomastoye.speelsysteem.models.Tenant
import com.ibm.couchdb._
import com.netaporter.uri.Uri
import play.Logger
import play.api.Configuration

import scalaz.{ -\/, \/- }

trait CouchDbConfig {
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

class CouchDbConfigImpl @Inject() (config: Configuration) extends CouchDbConfig {
  val host: String = config.get[String]("couchdb.server.host")
  val port: Int = config.get[Int]("couchdb.server.port")
  val https: Boolean = config.getOptional[Boolean]("couchdb.server.https").getOrElse(true)
  val user: Option[String] = config.getOptional[String]("couchdb.server.user")
  val pass: Option[String] = config.getOptional[String]("couchdb.server.pass")
}

class RemoteDbConfigImp @Inject() (config: Configuration) extends CouchDbConfig {
  val host: String = config.get[String]("couchdb.remote.host")
  val port: Int = config.get[Int]("couchdb.remote.port")
  val https: Boolean = config.getOptional[Boolean]("couchdb.remote.https").getOrElse(true)
  val user: Option[String] = config.getOptional[String]("couchdb.remote.user")
  val pass: Option[String] = config.getOptional[String]("couchdb.remote.pass")
}

trait CouchDatabase {
  def getDb(typeMapping: TypeMapping, tenant: Tenant): CouchDbApi
  def getDb(typeMapping: TypeMapping, dbName: String): CouchDbApi
}

@Singleton
class CouchDatabaseImpl @Inject() (couchConfig: CouchDbConfig) extends CouchDatabase {
  val couchdb: CouchDb = (for (user <- couchConfig.user; pass <- couchConfig.pass)
    yield CouchDb(couchConfig.host, couchConfig.port, couchConfig.https, user, pass)) getOrElse CouchDb(couchConfig.host, couchConfig.port, couchConfig.https)

  couchdb.server.info.unsafePerformAsync {
    case -\/(e) => Logger.warn("Could not connect to CouchDB", e)
    case \/-(res) => Logger.info(s"Successfully connected to CouchDB ${res.version} (vendor: ${res.vendor.name}): ${res.couchdb}")
  }

  override def getDb(typeMapping: TypeMapping, tenant: Tenant): CouchDbApi = couchdb.db(tenant.databaseName.value, typeMapping)

  override def getDb(typeMapping: TypeMapping, dbName: String) = couchdb.db(dbName, typeMapping)
}
