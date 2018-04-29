import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cloud.speelplein.dashboard.controllers.actions._
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.data.{AuditLogService, PdiJwtVerificationService}
import cloud.speelplein.models._
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatestplus.play.PlaySpec
import play.api
import play.api.{Configuration, mvc}
import play.api.mvc.{BodyParsers, Headers, Results}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import play.api.test.Helpers.stubControllerComponents

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class LoggingVerifyingBuilderSpec
    extends PlaySpec
    with Results
    with MockFactory
    with EitherValues
    with FutureAwaits
    with DefaultAwaitTimeout {
  val tenantAction = new TenantAction(
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

  "LoggingVerifyingBuilderImpl" should {
    "succeed when JWT is valid" in {
      val jwtString =
        """eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlFURkdSVGN6TlVJeVFrTTNNRE5DUWpnNU5VTXdPVFkzUlVNNU16a3dRalV6UmtaRFFUQTBNdyJ9.eyJodHRwczovL2luc2NocmlqdmVuLmNsb3VkL2FwcF9tZXRhZGF0YSI6eyJ0ZW5hbnRzIjpbeyJuYW1lIjoiZXhhbXBsZSIsInBlcm1pc3Npb25zIjpbImNoaWxkOnJldHJpZXZlIiwic3VwZXJ1c2VyOmxpc3QtZGF0YWJhc2VzIl0sInJvbGVzIjpbImFkbWluIl19LHsibmFtZSI6Imdsb2JhbCIsInBlcm1pc3Npb25zIjpbImNoaWxkOnJldHJpZXZlIiwic3VwZXJ1c2VyOmxpc3QtZGF0YWJhc2VzIiwic3VwZXJ1c2VyOmxpc3QtdGVuYW50cyJdLCJyb2xlcyI6WyJhZG1pbiIsInN1cGVydXNlciJdfV19LCJpc3MiOiJodHRwczovL2luc2NocmlqdmVuLWNsb3VkLmV1LmF1dGgwLmNvbS8iLCJzdWIiOiJmYWNlYm9va3wxMDIxNDA2Nzg4OTkyMjA5OSIsImF1ZCI6IjNpTDhtZnNCQ3VobzFmcEN5RFE0eWE1b2lSUG5Ib3lpIiwiaWF0IjoxNTEwMTcxNzA0LCJleHAiOjE1MTAyMDc3MDQsImF0X2hhc2giOiJ1bk1UR0Z5MkQ5SmxhVkx1U3dwSWFRIiwibm9uY2UiOiIyOU84aFVmVkg3c2FFYVgwa1I2VU83TUFvSzdlZXFWNCJ9.dlFms9TDw9vzR-Y56UsEoCxZHOXSnWnBN4I3FuXZ_WzG_G5kuTr2vI29nrEngt0aUB7wqZJjICiAOxufWy7mvnMqey7tYFDCSybPlEhLi8lkNeXRgKAP_CFDguVhrP3lpHVlYavuOuz-xJJz-Sz9F5kgr7Pou5OwZL1n0mEtwbkSSDRJIUuRYvWOjTRfJF6za_Nn0gE1h59koByQZMKDewDhFvq0r-6qwRV8WsuSyzq2vPcgaa2HsmSTDEPMzegKKpvCOPpRpvtvA2PLkyu2s8_4xCoCNjxYlgbRmPPlB5dK9u11Y2A8iVJPerN1-29p5g-SWe7c9gs6ak3-jA3aQQ"""

      val auditLogService = mock[AuditLogService]

      (auditLogService
        .registerAuditLogEntry(_: AuditLogEntry)(_: Tenant))
        .expects(where { (data: AuditLogEntry, tenant: Tenant) =>
          {
            data.eventId == Permission.childRetrieve.id && data.data == AuditLogData
              .childId("child-id-example") && data.loggedBy == "backend" && data.triggeredBy == AuditLogTriggeredBy(
              None,
              Some("facebook|10214067889922099"),
              "global",
              jwtString)
          }
        })
        .returning(Future.successful("log-entry-id"))
        .once()

      val builder = new LoggingVerifyingBuilderImpl(
        jwtVerificationService,
        new mvc.BodyParsers.Default(stubControllerComponents().parsers),
        jwtVerifyAction,
        new JwtAuthorizationBuilderImpl(
          jwtVerificationService,
          new BodyParsers.Default(stubControllerComponents().parsers),
          jwtVerifyAction),
        auditLogService
      )

      val request = FakeRequest("GET", "/blah?tenant=example")
        .withHeaders(Headers("Authorization" -> ("Bearer " + jwtString)))

      val res = await(
        builder
          .logAndVerify(Permission.childRetrieve,
                        AuditLogData.childId("child-id-example"))
          .invokeBlock(new TenantRequest(Tenant("global"), request), {
            req: AuditLoggingRequest[_] =>
              Future.successful(Ok("Request succeeded"))
          }))

      implicit val actorSystem = ActorSystem()
      implicit val materializer = ActorMaterializer()
      await(res.body.consumeData(materializer)).utf8String mustBe ("Request succeeded")

      await(actorSystem.terminate())
    }
  }
}
