import cloud.speelplein.dashboard.controllers.actions.{
  JwtAuthorizationBuilder,
  JwtAuthorizationBuilderImpl
}
import cloud.speelplein.dashboard.services._
import cloud.speelplein.data.couchdb._
import cloud.speelplein.data.util.{UuidService, UuidServiceImpl}
import cloud.speelplein.data._
import cloud.speelplein.dashboard.controllers.actions.{
  JwtAuthorizationBuilder,
  JwtAuthorizationBuilderImpl
}
import cloud.speelplein.dashboard.services.{
  JwtVerificationService,
  PdiJwtVerificationService
}
import cloud.speelplein.data.couchdb._
import cloud.speelplein.data._
import com.google.inject.AbstractModule

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ChildRepository]).to(classOf[CouchChildRepository])
    bind(classOf[ContactPersonRepository])
      .to(classOf[CouchContactPersonRepository])
    bind(classOf[CrewRepository]).to(classOf[CouchCrewRepository])
    bind(classOf[UuidService]).to(classOf[UuidServiceImpl])
    bind(classOf[CouchDatabase]).to(classOf[CouchDatabaseImpl])
    bind(classOf[DayService]).to(classOf[CouchDayService])
    bind(classOf[ChildAttendancesService])
      .to(classOf[CouchChildAttendancesService])
    bind(classOf[ReportService]).to(classOf[ReportServiceImpl])
    bind(classOf[ConfigService]).to(classOf[CouchConfigService])
    bind(classOf[JwtVerificationService]).to(classOf[PdiJwtVerificationService])
    bind(classOf[JwtAuthorizationBuilder])
      .to(classOf[JwtAuthorizationBuilderImpl])
    bind(classOf[TenantDatabaseService]).to(classOf[CouchTenantDatabaseService])
    bind(classOf[CouchDbConfig]).to(classOf[CouchDbConfigImpl])
    bind(classOf[TenantsService]).to(classOf[CouchTenantsService])
    bind(classOf[CouchDbConfig]).to(classOf[CouchDbConfigImpl])
  }
}
