import cloud.speelplein.data.couchdb.CouchTenantsService
import cloud.speelplein.models.Tenant
import cloud.speelplein.data.TenantDatabaseService
import cloud.speelplein.models.Tenant
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global

class TenantsServiceSpec extends PlaySpec with Results with MockFactory with ScalaFutures {
  "The tenants service" should {
    "correcly get all tenants from the database service" in {
      val databaseService: TenantDatabaseService = mock[TenantDatabaseService]
      (databaseService.all _).expects().returning(Future.successful(
        Seq(
          DbName.create("test").get,
          DbName.create("sometestdb").get,
          DbName.create("ic-aoeu").get,
          DbName.create("icsth-test").get,
          DbName.create("ic-snth").get
        )
      ))

      val tenantsService = new CouchTenantsService(databaseService)
      whenReady(tenantsService.all) { tenants =>
        tenants must have size 2
        tenants must contain(Tenant("snth"))
        tenants must contain(Tenant("aoeu"))
      }

    }
  }
}
