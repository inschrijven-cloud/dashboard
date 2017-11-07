import be.thomastoye.speelsysteem.models.Tenant
import org.scalatest._

class TenantValiditySpec extends WordSpec with MustMatchers {
  "Tenant#isValidNewTenantName" should {
    "disallow words in the forbidden list" in {
      Tenant.isValidNewTenantName("www") mustBe false
      Tenant.isValidNewTenantName("global") mustBe false
      Tenant.isValidNewTenantName("localhost") mustBe false
      Tenant.isValidNewTenantName("docs") mustBe false
    }

    "disallow tenant names containing special characters" in {
      Tenant.isValidNewTenantName("{)(*&^%$") mustBe false
      Tenant.isValidNewTenantName("some-tenant-}{)(*&^%$#@!") mustBe false
    }

    "disallow capitalization" in {
      Tenant.isValidNewTenantName("myNamE") mustBe false
      Tenant.isValidNewTenantName("myName") mustBe false
      Tenant.isValidNewTenantName("Myname") mustBe false
      Tenant.isValidNewTenantName("my_name5") mustBe false
      Tenant.isValidNewTenantName("my_name)5") mustBe false
    }

    "allow normal tenant names" in {
      Tenant.isValidNewTenantName("my-name") mustBe true
      Tenant.isValidNewTenantName("myname") mustBe true
      Tenant.isValidNewTenantName("myname5") mustBe true
    }
  }
}

