package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.controllers.actions.{ DomainAction, JwtAuthorizationBuilder }
import be.thomastoye.speelsysteem.data.ChildAttendancesService.{ AttendancesOnDay, ShiftWithAttendances }
import be.thomastoye.speelsysteem.data.{ ChildAttendancesService, ChildRepository, DayService }
import be.thomastoye.speelsysteem.models.{ Child, Day, DayDate, Shift }
import be.thomastoye.speelsysteem.models.JsonFormats._
import play.api.libs.json.{ JsValue, Json, Writes }
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.{ ExecutionContext, Future }

object ChildAttendanceApiController {
  case class BindShiftIds(shiftIds: Seq[Shift.Id])

  val bindShiftIdsReads = Json.reads[BindShiftIds]

  implicit val childAttendanceWrites = new Writes[(Day.Id, Seq[(Shift.Id, Int)])] {
    implicit val shiftsAttendanceWrites = new Writes[(Shift.Id, Int)] {
      override def writes(o: (Shift.Id, Int)): JsValue = Json.obj(
        "id" -> o._1,
        "numChildAttendances" -> o._2
      )
    }

    override def writes(o: (Day.Id, Seq[(Shift.Id, Int)])): JsValue = Json.obj(
      "dayId" -> o._1,
      "shifts" -> o._2
    )
  }
}

class ChildAttendanceApiController @Inject() (
    childRepository: ChildRepository,
    dayService: DayService,
    childAttendancesService: ChildAttendancesService,
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext) extends ApiController {
  import ChildAttendanceApiController._

  def numberOfChildAttendances: Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("child-attendance:numatt")).async { req =>
    childAttendancesService.findNumberOfChildAttendances(req.tenant) map { all =>
      Ok(Json.toJson(all))
    }
  }

  def childAttendancesOnDay(id: Day.Id): Action[AnyContent] = TODO

  def getAttendancesForChild(id: Child.Id): Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("child-attendance:retrieve")).async { req =>
    childAttendancesService.findAttendancesForChild(id)(req.tenant).map(att => Ok(Json.toJson(att)))
  }

  def addAttendancesForChild(childId: Child.Id, dayId: Day.Id): Action[BindShiftIds] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("child-attendance:create")).async(parse.json(bindShiftIdsReads)) { req =>
    dayService.findById(dayId)(req.tenant) flatMap { maybeEntityWithId =>
      maybeEntityWithId map { entityWithId =>
        childAttendancesService.addAttendancesForChild(childId, entityWithId.entity.date, req.body.shiftIds)(req.tenant) map (_ => Ok)
      } getOrElse Future.successful(NotFound)
    }
  }

  def deleteAttendancesForChild(childId: Child.Id, dayId: Day.Id): Action[BindShiftIds] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("child-attendance:delete")).async(parse.json(bindShiftIdsReads)) { req =>
    DayDate.createFromDayId(dayId) map { day =>
      childAttendancesService.removeAttendancesForChild(childId, day, req.body.shiftIds)(req.tenant).map(_ => NoContent)
    } getOrElse Future.successful(BadRequest("Could not parse day id"))
  }

  def findAllPerChild: Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("child-attendance:retrieve")).async { req =>
    childAttendancesService.findAll(req.tenant).map(all => Ok(Json.toJson(all)))
  }

  def findAllPerDay: Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("child-attendance:retrieve")).async { req =>
    implicit val shiftWithAttendancesFormat = Json.format[ShiftWithAttendances]
    implicit val attendancesOnDayFormat = Json.format[AttendancesOnDay]
    childAttendancesService.findAllPerDay(req.tenant).map(all => Ok(Json.toJson(all)))
  }

  def findAllRaw: Action[AnyContent] = (Action andThen domainAction andThen jwtAuthorizationBuilder.authenticatePermission("child-attendance:retrieve")).async { req =>
    implicit val writes = new Writes[(Day.Id, Shift.Id, Child.Id)] {
      override def writes(o: (Day.Id, Shift.Id, Child.Id)): JsValue = Json.obj(
        "dayId" -> o._1,
        "shiftId" -> o._2,
        "childId" -> o._3
      )
    }

    childAttendancesService.findAllRaw(req.tenant).map(all => Ok(Json.toJson(all)))
  }
}
