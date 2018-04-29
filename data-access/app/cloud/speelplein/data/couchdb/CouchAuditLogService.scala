package cloud.speelplein.data.couchdb

import cloud.speelplein.data.{
  AuditLogService,
  PlayJsonReaderUpickleCompat,
  PlayJsonWriterUpickleCompat
}
import cloud.speelplein.data.util.ScalazExtensions._
import cloud.speelplein.models.{AuditLogEntry, AuditLogTriggeredBy, Tenant}
import cloud.speelplein.models.JsonFormats.{
  auditLogEntryFormat,
  auditLogTriggeredByFormat
}
import com.ibm.couchdb.{MappedDocType, TypeMapping}
import upickle.default.{Reader, Writer}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

object CouchAuditLogService {
  val auditLogEntryKind = "type/auditLogEntry/v1"

  implicit val auditLogEntryReader: Reader[AuditLogEntry] =
    new PlayJsonReaderUpickleCompat[AuditLogEntry]
  implicit val auditLogEntryWriter: Writer[AuditLogEntry] =
    new PlayJsonWriterUpickleCompat[AuditLogEntry]

  implicit val auditLogTriggeredByReader: Reader[AuditLogTriggeredBy] =
    new PlayJsonReaderUpickleCompat[AuditLogTriggeredBy]
  implicit val auditLogTriggeredByWriter: Writer[AuditLogTriggeredBy] =
    new PlayJsonWriterUpickleCompat[AuditLogTriggeredBy]
}

class CouchAuditLogService @Inject()(couchDatabase: CouchDatabase)(
    implicit ec: ExecutionContext)
    extends AuditLogService {

  import CouchAuditLogService._

  private def db(implicit tenant: Tenant) =
    couchDatabase.getDb(
      TypeMapping(
        classOf[AuditLogEntry] -> CouchAuditLogService.auditLogEntryKind),
      tenant)

  override def registerAuditLogEntry(entry: AuditLogEntry)(
      implicit tenant: Tenant): Future[String] = {
    db.docs.create[AuditLogEntry](entry).toFuture.map(_.id)
  }

  override def getLogData(count: Int, offset: Int)(
      implicit tenant: Tenant): Future[Seq[AuditLogEntry]] = {
    db.docs.getMany
      .byType[AuditLogEntry]("all-audit-log-entry",
                             "default",
                             MappedDocType(auditLogEntryKind))
      .skip(offset)
      .limit(count)
      .includeDocs(auditLogEntryReader)
      .build
      .query
      .toFuture
      .map(_.getDocsData)
  }
}
