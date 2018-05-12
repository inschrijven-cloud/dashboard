package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject
import cloud.speelplein.dashboard.controllers.actions.{
  AuditLoggingRequest,
  JwtAuthorizationBuilder,
  LoggingVerifyingBuilder,
  TenantAction
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
import cloud.speelplein.models.Day.Id
import cloud.speelplein.models.JsonFormats._
import cloud.speelplein.models._
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Action, ActionBuilder, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

object ChildAttendanceApiController {
  case class BindAttendanceDataIds(
      shiftIds: Seq[Shift.Id],
      ageGroup: Option[AgeGroupData]
  )

  val bindShiftIdsReads = Json.reads[BindAttendanceDataIds]

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
    tenantAction: TenantAction,
    auditAuthorizationBuilder: LoggingVerifyingBuilder
)(implicit ec: ExecutionContext)
    extends ApiController {

  private def action(
      perm: Permission,
      data: AuditLogData): ActionBuilder[AuditLoggingRequest, AnyContent] =
    Action andThen tenantAction andThen auditAuthorizationBuilder.logAndVerify(
      perm,
      data)

  private def action(
      perm: Permission): ActionBuilder[AuditLoggingRequest, AnyContent] =
    action(perm, AuditLogData.empty)

  def numberOfChildAttendances: Action[AnyContent] =
    action(childAttendanceRetrieve).async { req =>
      childAttendancesService.findNumberOfChildAttendances(req.tenant) map {
        all =>
          Ok(Json.toJson(all))
      }
    }

  def childAttendancesOnDay(id: Day.Id): Action[AnyContent] =
    action(childAttendanceRetrieve).async { req =>
      implicit val writes = new Writes[(Child.Id, Seq[SingleAttendance])] {
        override def writes(o: (Child.Id, Seq[SingleAttendance])): JsValue = {
          o match {
            case (dayId, seq) =>
              Json.obj(
                "childId" -> dayId,
                "attendances" -> seq
              )
          }
        }
      }

      childAttendancesService.findAllOnDay(id)(req.tenant).map { attendances =>
        Ok(Json.toJson(attendances))
      }
    }

  def getAttendancesForChild(id: Child.Id): Action[AnyContent] =
    action(childAttendanceRetrieve, AuditLogData.childId(id)).async { req =>
      childAttendancesService
        .findAttendancesForChild(id)(req.tenant)
        .map(att => Ok(Json.toJson(att)))
    }

  def addAttendancesForChild(childId: Child.Id,
                             dayId: Day.Id): Action[BindAttendanceDataIds] =
    action(childAttendanceCreate,
           AuditLogData.childId(childId).copy(dayId = Some(dayId)))
      .async(parse.json(bindShiftIdsReads)) { req =>
        dayService.findById(dayId)(req.tenant) flatMap { maybeEntityWithId =>
          maybeEntityWithId map { entityWithId =>
            childAttendancesService.addAttendancesForChild(
              childId,
              entityWithId.entity.date,
              req.body.shiftIds,
              req.body.ageGroup
            )(req.tenant) map (_ => Ok)
          } getOrElse Future.successful(NotFound)
        }
      }

  def deleteAttendancesForChild(childId: Child.Id,
                                dayId: Day.Id): Action[BindAttendanceDataIds] =
    action(childAttendanceDelete,
           AuditLogData.childId(childId).copy(dayId = Some(dayId)))
      .async(parse.json(bindShiftIdsReads)) { req =>
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
