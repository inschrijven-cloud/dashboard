package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.actions.{
  DomainAction,
  JwtAuthorizationBuilder
}
import cloud.speelplein.dashboard.controllers.api.ChildAttendanceApiController._
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.ChildAttendancesService.{
  AttendancesOnDay,
  ShiftWithAttendances
}
import cloud.speelplein.data.{
  ChildAttendancesService,
  ChildRepository,
  DayService
}
import cloud.speelplein.models.JsonFormats._
import cloud.speelplein.models.{Child, Day, DayDate, Shift}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

object ChildAttendanceApiController {
  case class BindShiftIds(shiftIds: Seq[Shift.Id])

  val bindShiftIdsReads = Json.reads[BindShiftIds]

  implicit val childAttendanceWrites =
    new Writes[(Day.Id, Seq[(Shift.Id, Int)])] {
      implicit val shiftsAttendanceWrites = new Writes[(Shift.Id, Int)] {
        override def writes(o: (Shift.Id, Int)): JsValue = o match {
          case (id, numChildAttendances) =>
            Json.obj(
              "id" -> id,
              "numChildAttendances" -> numChildAttendances
            )
        }
      }

      override def writes(o: (Day.Id, Seq[(Shift.Id, Int)])): JsValue =
        o match {
          case (dayId, shifts) =>
            Json.obj(
              "dayId" -> dayId,
              "shifts" -> shifts
            )
        }
    }
}

class ChildAttendanceApiController @Inject()(
    childRepository: ChildRepository,
    dayService: DayService,
    childAttendancesService: ChildAttendancesService,
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
)(implicit ec: ExecutionContext)
    extends ApiController {
  private def action(perm: Permission) =
    Action andThen domainAction andThen jwtAuthorizationBuilder.authenticate(
      perm)

  def numberOfChildAttendances: Action[AnyContent] =
    action(childAttendanceRetrieve).async { req =>
      childAttendancesService.findNumberOfChildAttendances(req.tenant) map {
        all =>
          Ok(Json.toJson(all))
      }
    }

  def childAttendancesOnDay(id: Day.Id): Action[AnyContent] = TODO

  def getAttendancesForChild(id: Child.Id): Action[AnyContent] =
    action(childAttendanceRetrieve).async { req =>
      childAttendancesService
        .findAttendancesForChild(id)(req.tenant)
        .map(att => Ok(Json.toJson(att)))
    }

  def addAttendancesForChild(childId: Child.Id,
                             dayId: Day.Id): Action[BindShiftIds] =
    action(childAttendanceCreate).async(parse.json(bindShiftIdsReads)) { req =>
      dayService.findById(dayId)(req.tenant) flatMap { maybeEntityWithId =>
        maybeEntityWithId map { entityWithId =>
          childAttendancesService.addAttendancesForChild(
            childId,
            entityWithId.entity.date,
            req.body.shiftIds)(req.tenant) map (_ => Ok)
        } getOrElse Future.successful(NotFound)
      }
    }

  def deleteAttendancesForChild(childId: Child.Id,
                                dayId: Day.Id): Action[BindShiftIds] =
    action(childAttendanceDelete).async(parse.json(bindShiftIdsReads)) { req =>
      DayDate.createFromDayId(dayId) map { day =>
        childAttendancesService
          .removeAttendancesForChild(childId, day, req.body.shiftIds)(
            req.tenant)
          .map(_ => NoContent)
      } getOrElse Future.successful(BadRequest("Could not parse day id"))
    }

  def findAllPerChild: Action[AnyContent] =
    action(childAttendanceRetrieve).async { req =>
      childAttendancesService
        .findAll(req.tenant)
        .map(all => Ok(Json.toJson(all)))
    }

  def findAllPerDay: Action[AnyContent] =
    action(childAttendanceRetrieve).async { req =>
      implicit val shiftWithAttendancesFormat =
        Json.format[ShiftWithAttendances]
      implicit val attendancesOnDayFormat = Json.format[AttendancesOnDay]
      childAttendancesService
        .findAllPerDay(req.tenant)
        .map(all => Ok(Json.toJson(all)))
    }

  def findAllRaw: Action[AnyContent] = action(childAttendanceRetrieve).async {
    req =>
      implicit val writes = new Writes[(Day.Id, Shift.Id, Child.Id)] {
        override def writes(o: (Day.Id, Shift.Id, Child.Id)): JsValue =
          o match {
            case (dayId, shiftId, childId) =>
              Json.obj(
                "dayId" -> dayId,
                "shiftId" -> shiftId,
                "childId" -> childId
              )
          }
      }

      childAttendancesService
        .findAllRaw(req.tenant)
        .map(all => Ok(Json.toJson(all)))
  }
}
