import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.actions.{
  DomainAction,
  JwtAuthorizationBuilderImpl,
  JwtRequest,
  JwtVerifyAction
}
import cloud.speelplein.data.PdiJwtVerificationService
import cloud.speelplein.models.TenantMetadata
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatestplus.play.PlaySpec
import play.api
import play.api.{Configuration, mvc}
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class JwtAuthorizationSpec
    extends PlaySpec
    with Results
    with MockFactory
    with EitherValues {
  val domainAction = new DomainAction(
    new BodyParsers.Default(stubControllerComponents().parsers))
  val jwtVerificationService = new PdiJwtVerificationService(
    Configuration.from(Map(
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

  val jwtVerifyAction = new JwtVerifyAction(
    jwtVerificationService,
    new BodyParsers.Default(stubControllerComponents().parsers))
  val jwtAuthorizationBuilder = new JwtAuthorizationBuilderImpl(
    jwtVerificationService,
    new api.mvc.BodyParsers.Default(stubControllerComponents().parsers),
    jwtVerifyAction)

  "JwtAuthorizationBuilder" should {
    "authorize when user has a required permission in their JWT" in {
      val domainRes = Await.result(
        domainAction.refine(
          FakeRequest("GET", "/blah?domain=example.speelplein.cloud")
            .withHeaders(Headers(
              "Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlFURkdSVGN6TlVJeVFrTTNNRE5DUWpnNU5VTXdPVFkzUlVNNU16a3dRalV6UmtaRFFUQTBNdyJ9.eyJodHRwczovL2luc2NocmlqdmVuLmNsb3VkL2FwcF9tZXRhZGF0YSI6eyJ0ZW5hbnRzIjpbeyJuYW1lIjoiZXhhbXBsZSIsInBlcm1pc3Npb25zIjpbImJsYWgiXSwicm9sZXMiOlsiYWRtaW4iXX1dfSwiaXNzIjoiaHR0cHM6Ly9pbnNjaHJpanZlbi1jbG91ZC5ldS5hdXRoMC5jb20vIiwic3ViIjoiZmFjZWJvb2t8MTAyMTQwNjc4ODk5MjIwOTkiLCJhdWQiOiIzaUw4bWZzQkN1aG8xZnBDeURRNHlhNW9pUlBuSG95aSIsImlhdCI6MTUwOTk4MTE1NSwiZXhwIjoxNTEwMDE3MTU1LCJhdF9oYXNoIjoiZ0E5RFpXS1dwRHhlTzhSeU13TWZ3dyIsIm5vbmNlIjoiaWZkbXNTeWJld21DSzBnQ2pUaVRDWU9YZGFxMzJic3gifQ.sSgq_Jz9kcr4GKgm-e2ygvSRaT3p04xcG-hpIXCxAedjH7wiDDbedEt75dYVg8fMrwCToYF2aEnBYjJU_3JjLAufcL1zKG1GDtb9k6mowoHz92IG7ibbVOyjwbpRTdMuD7t82JWdpAPuhhmtilNUeYLbIqM5Tzc1ZUbrBiniF1ylYa2js_-wLYYITyNER_5Vv9oTYoy9wD8qdneS9__4GPVRBtAQdZTKOqHo3R6yfhDkbQuRpOw8zw8GdBHiYC3LJoNfW7TyeJezQvsTAVA8T4HifRMLrlotoDwp4tAJlQXHWOqYoEymUlPWrLV_1aI7sO_3pk687OWSTmm7eH6NYQ"))
        ),
        2.seconds
      )

      val permission = Permission("blah", "Permission used in test")

      val jwtRes = Await.result(
        jwtAuthorizationBuilder
          .authenticate(permission)
          .invokeBlock(domainRes.right.value,
                       (req: JwtRequest[_]) => Future.successful(Ok("ok"))),
        2.seconds
      )

      jwtRes.header.status mustBe 200
    }

    "authorize when user has one of the required permissions in their JWT" in {
      val domainRes = Await.result(
        domainAction.refine(
          FakeRequest("GET", "/blah?domain=example.speelplein.cloud")
            .withHeaders(Headers(
              "Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlFURkdSVGN6TlVJeVFrTTNNRE5DUWpnNU5VTXdPVFkzUlVNNU16a3dRalV6UmtaRFFUQTBNdyJ9.eyJodHRwczovL2luc2NocmlqdmVuLmNsb3VkL2FwcF9tZXRhZGF0YSI6eyJ0ZW5hbnRzIjpbeyJuYW1lIjoiZXhhbXBsZSIsInBlcm1pc3Npb25zIjpbImJsYWgiXSwicm9sZXMiOlsiYWRtaW4iXX1dfSwiaXNzIjoiaHR0cHM6Ly9pbnNjaHJpanZlbi1jbG91ZC5ldS5hdXRoMC5jb20vIiwic3ViIjoiZmFjZWJvb2t8MTAyMTQwNjc4ODk5MjIwOTkiLCJhdWQiOiIzaUw4bWZzQkN1aG8xZnBDeURRNHlhNW9pUlBuSG95aSIsImlhdCI6MTUwOTk4MTE1NSwiZXhwIjoxNTEwMDE3MTU1LCJhdF9oYXNoIjoiZ0E5RFpXS1dwRHhlTzhSeU13TWZ3dyIsIm5vbmNlIjoiaWZkbXNTeWJld21DSzBnQ2pUaVRDWU9YZGFxMzJic3gifQ.sSgq_Jz9kcr4GKgm-e2ygvSRaT3p04xcG-hpIXCxAedjH7wiDDbedEt75dYVg8fMrwCToYF2aEnBYjJU_3JjLAufcL1zKG1GDtb9k6mowoHz92IG7ibbVOyjwbpRTdMuD7t82JWdpAPuhhmtilNUeYLbIqM5Tzc1ZUbrBiniF1ylYa2js_-wLYYITyNER_5Vv9oTYoy9wD8qdneS9__4GPVRBtAQdZTKOqHo3R6yfhDkbQuRpOw8zw8GdBHiYC3LJoNfW7TyeJezQvsTAVA8T4HifRMLrlotoDwp4tAJlQXHWOqYoEymUlPWrLV_1aI7sO_3pk687OWSTmm7eH6NYQ"))
        ),
        2.seconds
      )

      val permissions =
        Seq(Permission("blah", "Permission used in test"),
            Permission("example perm", "Permission used in test"))

      val jwtRes = Await.result(
        jwtAuthorizationBuilder
          .authenticate(permissions)
          .invokeBlock(domainRes.right.value,
                       (req: JwtRequest[_]) => Future.successful(Ok("ok"))),
        2.seconds
      )

      jwtRes.header.status mustBe 200
    }

    "return unauthorized when user does not have a required permission in their JWT" in {
      val domainRes = Await.result(
        domainAction.refine(
          FakeRequest("GET", "/blah?domain=example.speelplein.cloud")
            .withHeaders(Headers(
              "Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlFURkdSVGN6TlVJeVFrTTNNRE5DUWpnNU5VTXdPVFkzUlVNNU16a3dRalV6UmtaRFFUQTBNdyJ9.eyJodHRwczovL2luc2NocmlqdmVuLmNsb3VkL2FwcF9tZXRhZGF0YSI6eyJ0ZW5hbnRzIjpbeyJuYW1lIjoiZXhhbXBsZSIsInBlcm1pc3Npb25zIjpbImJsYWgiXSwicm9sZXMiOlsiYWRtaW4iXX1dfSwiaXNzIjoiaHR0cHM6Ly9pbnNjaHJpanZlbi1jbG91ZC5ldS5hdXRoMC5jb20vIiwic3ViIjoiZmFjZWJvb2t8MTAyMTQwNjc4ODk5MjIwOTkiLCJhdWQiOiIzaUw4bWZzQkN1aG8xZnBDeURRNHlhNW9pUlBuSG95aSIsImlhdCI6MTUwOTk4MTE1NSwiZXhwIjoxNTEwMDE3MTU1LCJhdF9oYXNoIjoiZ0E5RFpXS1dwRHhlTzhSeU13TWZ3dyIsIm5vbmNlIjoiaWZkbXNTeWJld21DSzBnQ2pUaVRDWU9YZGFxMzJic3gifQ.sSgq_Jz9kcr4GKgm-e2ygvSRaT3p04xcG-hpIXCxAedjH7wiDDbedEt75dYVg8fMrwCToYF2aEnBYjJU_3JjLAufcL1zKG1GDtb9k6mowoHz92IG7ibbVOyjwbpRTdMuD7t82JWdpAPuhhmtilNUeYLbIqM5Tzc1ZUbrBiniF1ylYa2js_-wLYYITyNER_5Vv9oTYoy9wD8qdneS9__4GPVRBtAQdZTKOqHo3R6yfhDkbQuRpOw8zw8GdBHiYC3LJoNfW7TyeJezQvsTAVA8T4HifRMLrlotoDwp4tAJlQXHWOqYoEymUlPWrLV_1aI7sO_3pk687OWSTmm7eH6NYQ"))
        ),
        2.seconds
      )

      val permission = Permission("other permission", "Permission used in test")

      val jwtRes = Await.result(
        jwtAuthorizationBuilder
          .authenticate(permission)
          .invokeBlock(domainRes.right.value,
                       (req: JwtRequest[_]) => Future.successful(Ok("ok"))),
        2.seconds
      )

      jwtRes.header.status mustBe 401
    }

    "authorize when user has a role that implies a permission in their JWT" in {
      val domainRes = Await.result(
        domainAction.refine(
          FakeRequest("GET", "/blah?domain=example.speelplein.cloud")
            .withHeaders(Headers(
              "Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlFURkdSVGN6TlVJeVFrTTNNRE5DUWpnNU5VTXdPVFkzUlVNNU16a3dRalV6UmtaRFFUQTBNdyJ9.eyJodHRwczovL2luc2NocmlqdmVuLmNsb3VkL2FwcF9tZXRhZGF0YSI6eyJ0ZW5hbnRzIjpbeyJuYW1lIjoiZXhhbXBsZSIsInJvbGVzIjpbImFuaW1hdG9yIiwiaG9vZmRhbmltYXRvciJdLCJwZXJtaXNzaW9ucyI6W119LHsibmFtZSI6Imdsb2JhbCIsInJvbGVzIjpbInN1cGVydXNlciJdLCJwZXJtaXNzaW9ucyI6W119LHsibmFtZSI6ImRlc3BlZWxiZXJnIiwicm9sZXMiOlsiYW5pbWF0b3IiLCJob29mZGFuaW1hdG9yIl0sInBlcm1pc3Npb25zIjpbXX1dfSwiaXNzIjoiaHR0cHM6Ly9pbnNjaHJpanZlbi1jbG91ZC5ldS5hdXRoMC5jb20vIiwic3ViIjoiZmFjZWJvb2t8MTAyMTQwNjc4ODk5MjIwOTkiLCJhdWQiOiIzaUw4bWZzQkN1aG8xZnBDeURRNHlhNW9pUlBuSG95aSIsImlhdCI6MTUxMTEyNTE0NywiZXhwIjoxNTExMTYxMTQ3LCJhdF9oYXNoIjoib3Y0dmhVLXQ2bmk4d1RIWDFaSHNFQSIsIm5vbmNlIjoiblhWSURrejd4YWNCLnV5VklOYk1aeEVPSGI0UDhRTmEifQ.WzYZclv6UyOJ_9-s4f_8VFFW7sTXLLxyLYy4c8g3aGrCqKCkeFR9RmYUWikWFa6NIyPjl4cEQxauLVJFmHBFLIeexkXSt2Q0EOLWFh5OMei3cFPpb85aMsa8s95W0NAZc4gK4x3p-cnYWxdwb_fYGNE2JV9qbXEM5ZEvUpN9Zxsuf-ftpFQ7OfZsd6RyOiT8IIrOAMuqvFYXMyPGkQS2QSR5jp3bj6Eidrs-y7xJnkoM-8h3Bx9rGkfptqZEqE81eAc99EC6Cmf-JwDu0RIz7NZTCy-Lsp5rps9iY05VBq4HWSCT2LvL6Mp-Ug3MmvM_Wy9cPJOrzGztgjsuOpVr_g"))
        ),
        2.seconds
      )

      val jwtRes = Await.result(
        jwtAuthorizationBuilder
          .authenticate(Permission.childRetrieve)
          .invokeBlock(domainRes.right.value,
                       (req: JwtRequest[_]) => Future.successful(Ok("ok"))),
        2.seconds
      )

      jwtRes.header.status mustBe 200
    }

    "return unauthorized when user does not have a required role in their JWT" in {
      val domainRes = Await.result(
        domainAction.refine(
          FakeRequest("GET", "/blah?domain=example.speelplein.cloud")
            .withHeaders(Headers(
              "Authorization" -> "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlFURkdSVGN6TlVJeVFrTTNNRE5DUWpnNU5VTXdPVFkzUlVNNU16a3dRalV6UmtaRFFUQTBNdyJ9.eyJodHRwczovL2luc2NocmlqdmVuLmNsb3VkL2FwcF9tZXRhZGF0YSI6eyJ0ZW5hbnRzIjpbeyJuYW1lIjoiZXhhbXBsZSIsInBlcm1pc3Npb25zIjpbImJsYWgiXSwicm9sZXMiOlsiYWRtaW4iXX1dfSwiaXNzIjoiaHR0cHM6Ly9pbnNjaHJpanZlbi1jbG91ZC5ldS5hdXRoMC5jb20vIiwic3ViIjoiZmFjZWJvb2t8MTAyMTQwNjc4ODk5MjIwOTkiLCJhdWQiOiIzaUw4bWZzQkN1aG8xZnBDeURRNHlhNW9pUlBuSG95aSIsImlhdCI6MTUwOTk4MTE1NSwiZXhwIjoxNTEwMDE3MTU1LCJhdF9oYXNoIjoiZ0E5RFpXS1dwRHhlTzhSeU13TWZ3dyIsIm5vbmNlIjoiaWZkbXNTeWJld21DSzBnQ2pUaVRDWU9YZGFxMzJic3gifQ.sSgq_Jz9kcr4GKgm-e2ygvSRaT3p04xcG-hpIXCxAedjH7wiDDbedEt75dYVg8fMrwCToYF2aEnBYjJU_3JjLAufcL1zKG1GDtb9k6mowoHz92IG7ibbVOyjwbpRTdMuD7t82JWdpAPuhhmtilNUeYLbIqM5Tzc1ZUbrBiniF1ylYa2js_-wLYYITyNER_5Vv9oTYoy9wD8qdneS9__4GPVRBtAQdZTKOqHo3R6yfhDkbQuRpOw8zw8GdBHiYC3LJoNfW7TyeJezQvsTAVA8T4HifRMLrlotoDwp4tAJlQXHWOqYoEymUlPWrLV_1aI7sO_3pk687OWSTmm7eH6NYQ"))
        ),
        2.seconds
      )

      val permission = Permission("other permission", "Permission used in test")

      val jwtRes = Await.result(
        jwtAuthorizationBuilder
          .authenticate(permission)
          .invokeBlock(domainRes.right.value,
                       (req: JwtRequest[_]) => Future.successful(Ok("ok"))),
        2.seconds
      )

      jwtRes.header.status mustBe 401
    }
  }

}
