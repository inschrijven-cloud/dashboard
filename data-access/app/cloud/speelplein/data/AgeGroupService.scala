package cloud.speelplein.data

import cloud.speelplein.models.{AgeGroupConfig, Tenant}

import scala.concurrent.Future

trait AgeGroupService {
  def get()(implicit tenant: Tenant): Future[AgeGroupConfig]

  def updateOrCreate(newConfigHolder: AgeGroupConfig)(
      implicit tenant: Tenant): Future[Unit]
}
