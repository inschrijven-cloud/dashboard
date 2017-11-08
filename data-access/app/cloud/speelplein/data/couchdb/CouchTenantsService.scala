package cloud.speelplein.data.couchdb

import javax.inject.{ Inject, Singleton }

import cloud.speelplein.data.TenantsService
import cloud.speelplein.data.{ TenantDatabaseService, TenantsService }
import cloud.speelplein.models.Tenant
import com.ibm.couchdb.{ CouchDesign, CouchView }
import com.ibm.couchdb.Res.Ok
import play.api.Logger
import play.api.libs.json.{ JsObject, JsValue, Json }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CouchTenantsService @Inject() (databaseService: TenantDatabaseService)(implicit val ec: ExecutionContext) extends TenantsService {
  // TODO tenants can be looked up from config db? Need to insert into ic-config to create a tenant?
  override def all: Future[Seq[Tenant]] = {
    databaseService.all map { dbs =>
      dbs
        .map(_.value)
        .filter(name => {
          name.startsWith("ic-")
        })
        .map(_.drop("ic-".length)) // remove prefix
        .filter(_ != "config") // config database is not a tenant
        .map(Tenant(_))
    }
  }

  override def create(tenant: Tenant): Future[Ok] = {
    databaseService.create(tenant.databaseName)
  }

  override def details(tenant: Tenant): Future[Unit] = Future.successful(())

  override def initializeDatabase(tenant: Tenant): Future[JsObject] = {
    def viewAll(kind: String): CouchView = {
      CouchView(map =
        s"""
           |function(doc) {
           |  if(doc.kind === '$kind') {
           |    emit([doc.kind, doc._id], doc._id);
           |  }
           |}
        """.stripMargin)
    }

    val designDocs: Map[String, CouchView] = Map(
      "all-children" -> viewAll("type/child/v1"),
      "all-crew" -> viewAll("type/crew/v1"),
      "all-child-attendances" -> viewAll("type/childattendance/v2"),
      "all-days" -> viewAll("type/day/v1"),
      "all-contactperson" -> viewAll("type/contactperson/v1")
    )

    case class Revs(childRev: String, crewRev: String)

    Logger.info(s"Initializing database ${tenant.databaseName.value} for tenant ${tenant.name} with design doc ")

    val name = "default"
    val exists = databaseService.designDocExists(tenant.databaseName, name)
    val design = designDocs.get(tenant.databaseName.value)

    exists.flatMap { rev =>
      databaseService.createDesignDoc(tenant.databaseName, CouchDesign(name, _rev = rev.getOrElse(""), views = designDocs))
    } map { docok => Json.obj("id" -> docok.id, "rev" -> docok.rev, "ok" -> docok.ok) }
  }

  override def syncTo(tenant: Tenant, remote: CouchDbConfig): Future[JsValue] = {
    databaseService.startReplicationToRemote(tenant.databaseName, remote)
  }

  override def syncFrom(tenant: Tenant, remote: CouchDbConfig): Future[JsValue] = {
    databaseService.startReplicationFromRemote(remote, tenant.databaseName)
  }
}
