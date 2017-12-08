import cloud.speelplein.EntityWithId
import cloud.speelplein.dashboard.controllers.api.ChildAttendanceApiController
import cloud.speelplein.data.ChildRepository
import cloud.speelplein.models.Day.Id
import cloud.speelplein.models.Shift.ShiftKind
import cloud.speelplein.models._
import cloud.speelplein.dashboard.controllers.actions.TenantAction
import cloud.speelplein.dashboard.controllers.api.ChildAttendanceApiController
import cloud.speelplein.data.{ChildAttendancesService, ChildRepository}
import cloud.speelplein.models._
import helpers.{StubJwtAuthorizationBuilder, UnimplementedDayService}
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class ChildAttendanceApiControllerSpec
    extends PlaySpec
    with Results
    with MockFactory {
  val tenantAction = new TenantAction(
    new BodyParsers.Default(stubControllerComponents().parsers))
  val fakeReq = FakeRequest("GET", "/blah?tenant=blah")
  val authBuilder = new StubJwtAuthorizationBuilder()

  "ChildAttendanceApiController#numberOfChildAttendances" should {
    "return a list with days and the number of children attending each shift" in {

      // Currently can't be mocked, relies on functionality in the trait
      val dayServiceStub = new UnimplementedDayService {
        def shift: (String => Shift) =
          Shift(_,
                Price(2, 0),
                true,
                true,
                ShiftKind.Afternoon,
                None,
                None,
                None)

        override def findAll(
            implicit tenant: Tenant): Future[Seq[EntityWithId[Id, Day]]] =
          Future.successful(
            Seq(
              EntityWithId(
                "2016-11-25",
                Day(
                  DayDate(25, 11, 2016),
                  Seq(
                    Shift("shift1",
                          Price(1, 0),
                          true,
                          true,
                          ShiftKind.Morning,
                          None,
                          None,
                          None),
                    Shift("shift2",
                          Price(2, 0),
                          true,
                          true,
                          ShiftKind.Afternoon,
                          None,
                          None,
                          None)
                  )
                )
              ),
              EntityWithId(
                "2016-02-01",
                Day(
                  DayDate(1, 2, 2016),
                  Seq(
                    Shift(
                      "shift3",
                      Price(2, 0),
                      false,
                      false,
                      ShiftKind.Afternoon,
                      Some("location"),
                      Some("description"),
                      Some(StartAndEndTime(RelativeTime(13, 0),
                                           RelativeTime(17, 30)))
                    )
                  )
                )
              )
            )
          )
      }

      val childRepo = mock[ChildRepository]

      val childAttendanceService = mock[ChildAttendancesService]

      (childAttendanceService
        .findNumberOfChildAttendances(_: Tenant))
        .expects(*)
        .returning(
          Future.successful(
            Map(
              "2016-11-25" -> Map("shift1" -> 2, "shift2" -> 1),
              "2016-02-01" -> Map("shift3" -> 1)
            )
          ))
        .once()

      val controller = new ChildAttendanceApiController(childRepo,
                                                        dayServiceStub,
                                                        childAttendanceService,
                                                        tenantAction,
                                                        authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      val body =
        contentAsJson(controller.numberOfChildAttendances.apply(fakeReq))

      body mustBe Json.obj(
        "2016-11-25" -> Json.obj("shift1" -> 2, "shift2" -> 1),
        "2016-02-01" -> Json.obj("shift3" -> 1)
      )
    }
  }
}
