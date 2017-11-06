import be.thomastoye.speelsysteem.dashboard.services.{ JwtVerificationService, PdiJwtVerificationService }
import be.thomastoye.speelsysteem.data.couchdb._
import be.thomastoye.speelsysteem.data.util.{ UuidService, UuidServiceImpl }
import be.thomastoye.speelsysteem.data._
import com.google.inject.AbstractModule

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ChildRepository]).to(classOf[CouchChildRepository])
    bind(classOf[ContactPersonRepository]).to(classOf[CouchContactPersonRepository])
    bind(classOf[CrewRepository]).to(classOf[CouchCrewRepository])
    bind(classOf[UuidService]).to(classOf[UuidServiceImpl])
    bind(classOf[CouchDatabase]).to(classOf[CouchDatabaseImpl])
    bind(classOf[DayService]).to(classOf[CouchDayService])
    bind(classOf[ChildAttendancesService]).to(classOf[CouchChildAttendancesService])
    bind(classOf[ReportService]).to(classOf[ReportServiceImpl])
    bind(classOf[ConfigService]).to(classOf[CouchConfigService])
    bind(classOf[JwtVerificationService]).to(classOf[PdiJwtVerificationService])
  }
}
