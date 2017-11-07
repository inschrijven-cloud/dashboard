import be.thomastoye.speelsysteem.models.Tenant
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results

class TenantSpec extends PlaySpec with Results with MockFactory {
  "Tenant#fromDomain" should {
    "correctly parse localhost" in {
      Tenant.fromDomain("localhost") mustBe Some(Tenant("localhost"))
      Tenant.fromDomain("localhost").get.databaseName.value mustBe "ic-localhost"
    }

    "correctly parse blah.speelplein.cloud" in {
      Tenant.fromDomain("blah.speelplein.cloud") mustBe Some(Tenant("blah"))
      Tenant.fromDomain("blah.speelplein.cloud").get.databaseName.value mustBe "ic-blah"
    }

    "not parse snth.aoeu.com" in {
      Tenant.fromDomain("snth.aoeu.com") mustBe None
    }
  }
}
