package be.thomastoye.speelsysteem.data.couchdb

import javax.inject.{ Inject, Singleton }

import com.ibm.couchdb._
import play.Logger
import play.api.Configuration

import scalaz.{ -\/, \/- }

object CouchConfiguration {
  def fromConfig(config: Configuration): CouchConfiguration = {
    CouchConfiguration(
      config.get[String]("couchdb.server.host"),
      config.get[Int]("couchdb.server.port"),
      config.get[Boolean]("couchdb.server.https"),
      config.getOptional[String]("couchdb.server.user"),
      config.getOptional[String]("couchdb.server.pass"),
      config.get[String]("couchdb.server.db")
    )
  }
}

case class CouchConfiguration(host: String, port: Int, https: Boolean, user: Option[String], pass: Option[String], db: String)

object CouchDatabase {
  case class CouchPersistenceException(msg: String) extends Exception(msg) // TODO is this used?
}

trait CouchDatabase {
  def getDb(typeMapping: TypeMapping): CouchDbApi
}

@Singleton
class CouchDatabaseImpl @Inject() (config: Configuration) extends CouchDatabase {
  private val couchConfig = CouchConfiguration.fromConfig(config)
  val couchdb = (for (user <- couchConfig.user; pass <- couchConfig.pass)
    yield CouchDb(couchConfig.host, couchConfig.port, couchConfig.https, user, pass)) getOrElse CouchDb(couchConfig.host, couchConfig.port, couchConfig.https)

  couchdb.server.info.unsafePerformAsync {
    case -\/(e) => Logger.warn("Could not connect to CouchDB", e)
    case \/-(res) => Logger.info(s"Successfully connected to CouchDB ${res.version} (vendor: ${res.vendor.name}): ${res.couchdb}")
  }

  override def getDb(typeMapping: TypeMapping): CouchDbApi = couchdb.db(couchConfig.db, typeMapping)

}
