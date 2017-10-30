package be.thomastoye.speelsysteem.data

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.models.{ ConfigWrapper, Tenant }

import scala.concurrent.Future

trait ConfigService {
  def getConfig(domain: String): Future[Option[ConfigWrapper]]
  def insert(id: String, config: ConfigWrapper): Future[Unit]
  def update(id: String, config: ConfigWrapper): Future[Unit]
}
