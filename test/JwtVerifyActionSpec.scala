import be.thomastoye.speelsysteem.dashboard.controllers.actions.{ DomainAction, JwtVerifyAction }
import be.thomastoye.speelsysteem.dashboard.services.PdiJwtVerificationService
import be.thomastoye.speelsysteem.models.TenantMetadata
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class JwtVerifyActionSpec extends PlaySpec with Results with MockFactory with EitherValues {
  val domainAction = new DomainAction(new BodyParsers.Default(stubControllerComponents().parsers))
  val jwtVerificationService = new PdiJwtVerificationService(Configuration.from(Map(
    "jwt.key.pem" ->
      """-----BEGIN CERTIFICATE-----
      |MIIDFzCCAf+gAwIBAgIJZ2zaPGdshtNTMA0GCSqGSIb3DQEBCwUAMCkxJzAlBgNV
      |BAMTHmluc2NocmlqdmVuLWNsb3VkLmV1LmF1dGgwLmNvbTAeFw0xNzEwMjMwOTMy
      |MjJaFw0zMTA3MDIwOTMyMjJaMCkxJzAlBgNVBAMTHmluc2NocmlqdmVuLWNsb3Vk
      |LmV1LmF1dGgwLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMsE
      |IYhzhtUr4LppDzekekY2fexogVePrbiyxX3jNndA/QD8VE19nMqM1lHx89OTpkWP
      |z8mwCuCvz0VZXbgF2QVvQLjx6IlFcwK/XYtvlUlrdsRi/2kalFtiXEP2gCYsGA17
      |hH0Z2dCEow1jU43QlTXFn+act835xjkjcRa9Dk9jaNxpWi+2e1tCKth2dgcFMb1r
      |IdEt235s40UHUX0G90sEsqj9LMWrAJEdgffC6Dmpy4cO87zbYh305PDuEfLn8wqy
      |OabUEwxUCYoGy1WxToHYC0i0ZwshisNhxzkQWGI1uM8AwcBJSlz+lD/9uY786Vez
      |CGUkwZT4yfrPCJNqZ6MCAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4E
      |FgQUY+zEzi6Ps3cp8y5bp856+YZYJ60wDgYDVR0PAQH/BAQDAgKEMA0GCSqGSIb3
      |DQEBCwUAA4IBAQChlTaijsTim1xiWzA5Jmgbh4GzLXZ9M4OssO7vDqrqZtQXVMY8
      |8LSIXiSLAwL8CViCQtVZrUwQgvCJVwTmv8P4NwVkib6kqQ0v1litqnJEPBuYN3+r
      |4aFkB/GhaDCuEoD4daoRF7oWimZHvcGkSbxrdEstH/1UnNHGEJFLAI8liLEiJKPm
      |c4PCzxSpjso/7BMmhGuNmMv8PEZm5p5VqbzP4MsbmAiRQa0yJUEGexo95WXLCRpr
      |Eq6RNijWhwuGNEL2u9yQ3NAQZC12nIAPwBuV8vgTNDyVTJXS3mVMxMnaG4f1MOvA
      |veJkq1/PdrnmGL+WQiJqYGGQ0hnQd1ypeRyp
      |-----END CERTIFICATE-----""".stripMargin,
    "jwt.enableExpiration" -> false
  )))

  val jwtVerifyAction = new JwtVerifyAction(jwtVerificationService, new BodyParsers.Default(stubControllerComponents().parsers))

  "JwtVerifyAction" should {
    "succeed when JWT is valid" in {
      val domainRes = Await.result(
        domainAction.refine(
          FakeRequest("GET", "/blah?domain=example.speelplein.cloud").withHeaders(Headers("Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlFURkdSVGN6TlVJeVFrTTNNRE5DUWpnNU5VTXdPVFkzUlVNNU16a3dRalV6UmtaRFFUQTBNdyJ9.eyJodHRwczovL2luc2NocmlqdmVuLmNsb3VkL2FwcF9tZXRhZGF0YSI6eyJ0ZW5hbnRzIjpbeyJuYW1lIjoiZXhhbXBsZSIsInBlcm1pc3Npb25zIjpbImJsYWgiXSwicm9sZXMiOlsiYWRtaW4iXX1dfSwiaXNzIjoiaHR0cHM6Ly9pbnNjaHJpanZlbi1jbG91ZC5ldS5hdXRoMC5jb20vIiwic3ViIjoiZmFjZWJvb2t8MTAyMTQwNjc4ODk5MjIwOTkiLCJhdWQiOiIzaUw4bWZzQkN1aG8xZnBDeURRNHlhNW9pUlBuSG95aSIsImlhdCI6MTUwOTk4MTE1NSwiZXhwIjoxNTEwMDE3MTU1LCJhdF9oYXNoIjoiZ0E5RFpXS1dwRHhlTzhSeU13TWZ3dyIsIm5vbmNlIjoiaWZkbXNTeWJld21DSzBnQ2pUaVRDWU9YZGFxMzJic3gifQ.sSgq_Jz9kcr4GKgm-e2ygvSRaT3p04xcG-hpIXCxAedjH7wiDDbedEt75dYVg8fMrwCToYF2aEnBYjJU_3JjLAufcL1zKG1GDtb9k6mowoHz92IG7ibbVOyjwbpRTdMuD7t82JWdpAPuhhmtilNUeYLbIqM5Tzc1ZUbrBiniF1ylYa2js_-wLYYITyNER_5Vv9oTYoy9wD8qdneS9__4GPVRBtAQdZTKOqHo3R6yfhDkbQuRpOw8zw8GdBHiYC3LJoNfW7TyeJezQvsTAVA8T4HifRMLrlotoDwp4tAJlQXHWOqYoEymUlPWrLV_1aI7sO_3pk687OWSTmm7eH6NYQ"))
        ), 2.seconds
      )

      val jwtRes = Await.result(jwtVerifyAction.refine(domainRes.right.value), 2.seconds)

      jwtRes.isRight mustBe true
      jwtRes.right.value.tenantData mustBe TenantMetadata("example", Seq("blah"), Seq("admin"))
    }

    "fail when JWT is invalid" in {
      val domainRes = Await.result(
        domainAction.refine(
          FakeRequest("GET", "/blah?domain=example.speelplein.cloud").withHeaders(Headers("Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlFURkdSVGN6TlVJeVFrTTNNRE5DUWpnNU5VTXdPVFkzUlVNNU16a3dRalV6UmtaRFFUQTBNdyJ9.eyJodHRwczovL2luc2NocmlqdmVuLmNsb3VkL2FwcF9tZXRhZGF0YSI6eyJ0ZW5hbnRzIjpbeyJleGFtcGxlIjp7InBlcm1pc3Npb25zIjpbImJsYWgiXX19XX0sImlzcyI6Imh0dHBzOi8vaW5zY2hyaWp2ZW4tY2xvdWQuZXUuYXV0aDAuY29tLyIsInN1YiI6ImZhY2Vib29rfDEwMjE0MDY3ODg5OTIyMDk5IiwiYXVkIjoiM2lMOG1mc0JDdWhvMWZwQ3lEUTR5YTVvaVJQbkhveWkiLCJpYXQiOjE1MDk5MjI3MjksImV4cCI6MTUwOTk1ODcyOSwiYXRfaGFzaCI6Ik5xMzV0eThRTlVkaDF3V0FaNmI5RkEiLCJub25jZSI6IjlKMVUzOXQyWkh0eDM1Mkx5XzA5ekVNTlN5a35SVVhqIn0.YN73hNt4m3lb6YKcVoEnK0q1vRPuld0CwoYakIV93GEG7elsDquJVXwIZox1eK1QS9-hW3TSdaK0_BZg3aOf_dcurOH5dsyOQKQaoowXd9fWDaheHYiMQPnNSgCIjmAgevgqjuJmP-aWL5kHYay0RxLo95MI4ZQPrJ5UCX1sv8JORwo6wN3Q-4F4UQ3LV8RQK7KlDql_jKXT3FsSNm0vmgaHL6-2wU7AFUjnCKClJXtQDr84XocSntTGDJ4ea2AXnqLuurisF4ILdC7rZT5rBj_ZWAazW5BoWWmJFp23kof3VixQk2IJfzpBSlTXRKiD4ISaz3vSjIZs-kO9IhrTp"))
        ), 2.seconds
      )

      val jwtRes = Await.result(jwtVerifyAction.refine(domainRes.right.value), 2.seconds)

      jwtRes.isLeft mustBe true
      jwtRes.left.value.header.status mustBe 400
    }
  }
}
