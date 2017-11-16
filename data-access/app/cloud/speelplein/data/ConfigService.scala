package cloud.speelplein.data

import cloud.speelplein.EntityWithId
import cloud.speelplein.models.ConfigWrapper
import play.api.libs.json.JsObject

import scala.concurrent.Future

trait ConfigService {
  def getConfig(domain: String): Future[Option[ConfigWrapper]]
  def getAllConfig: Future[Seq[EntityWithId[String, ConfigWrapper]]]
  def insert(id: String, config: ConfigWrapper): Future[Unit]
  def update(id: String, config: ConfigWrapper): Future[Unit]
  def insertDesignDocs: Future[JsObject]
}
