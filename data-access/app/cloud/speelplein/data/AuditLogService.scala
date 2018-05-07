package cloud.speelplein.data

import cloud.speelplein.models.{AuditLogEntry, Tenant}

import scala.concurrent.Future

trait AuditLogService {
  def registerAuditLogEntry(entry: AuditLogEntry)(
      implicit tenant: Tenant): Future[Unit]

  def getLogData(count: Int, offset: Int)(
      implicit tenant: Tenant): Future[Seq[AuditLogEntry]]
}
