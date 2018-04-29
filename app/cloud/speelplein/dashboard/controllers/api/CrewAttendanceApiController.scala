package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject
import cloud.speelplein.dashboard.controllers.actions.{
  AuditLoggingRequest,
  JwtAuthorizationBuilder,
  LoggingVerifyingBuilder,
  TenantAction
}
import cloud.speelplein.dashboard.controllers.api.CrewAttendanceApiController.BindShiftIds
import cloud.speelplein.dashboard.controllers.api.CrewAttendanceApiController.bindShiftIdsReads
import cloud.speelplein.dashboard.controllers.api.auth.Permission
import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import cloud.speelplein.data.CrewAttendancesService.{
  AttendancesOnDay,
  ShiftWithAttendances
}
import cloud.speelplein.data.{
  CrewAttendancesService,
  CrewRepository,
  DayService
}
import cloud.speelplein.models.JsonFormats._
import cloud.speelplein.models._
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Action, ActionBuilder, AnyContent}

import scala.concurrent.{ExecutionContext, Future}

object CrewAttendanceApiController {
  case class BindShiftIds(shiftIds: Seq[Shift.Id])

  val bindShiftIdsReads = Json.reads[BindShiftIds]

  implicit val crewAttendanceWrites =
    new Writes[(Day.Id, Seq[(Shift.Id, Int)])] {
      implicit val shiftsAttendanceWrites = new Writes[(Shift.Id, Int)] {
        override def writes(o: (Shift.Id, Int)): JsValue = o match {
          case (id, numCrewAttendances) =>
            Json.obj(
              "id" -> id,
              "numCrewAttendances" -> numCrewAttendances
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

class CrewAttendanceApiController @Inject()(
    crewRepository: CrewRepository,
    dayService: DayService,
    crewAttendancesService: CrewAttendancesService,
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

  def numberOfCrewAttendances: Action[AnyContent] =
    action(crewAttendanceRetrieve).async { req =>
      crewAttendancesService.findNumberOfCrewAttendances(req.tenant) map {
        all =>
          Ok(Json.toJson(all))
      }
    }

  def crewAttendancesOnDay(id: Day.Id): Action[AnyContent] = TODO

  def getAttendancesForCrew(id: Crew.Id): Action[AnyContent] =
    action(crewAttendanceRetrieve, AuditLogData.crewId(id)).async { req =>
      crewAttendancesService
        .findAttendancesForCrew(id)(req.tenant)
        .map(att => Ok(Json.toJson(att)))
    }

  def addAttendancesForCrew(crewId: Crew.Id,
                            dayId: Day.Id): Action[BindShiftIds] =
    action(crewAttendanceCreate,
           AuditLogData.crewId(crewId).copy(dayId = Some(dayId)))
      .async(parse.json(bindShiftIdsReads)) { req =>
        dayService.findById(dayId)(req.tenant) flatMap { maybeEntityWithId =>
          maybeEntityWithId map { entityWithId =>
            crewAttendancesService.addAttendancesForCrew(
              crewId,
              entityWithId.entity.date,
              req.body.shiftIds)(req.tenant) map (_ => Ok)
          } getOrElse Future.successful(NotFound)
        }
      }

  def deleteAttendancesForCrew(crewId: Crew.Id,
                               dayId: Day.Id): Action[BindShiftIds] =
    action(crewAttendanceDelete,
           AuditLogData.crewId(crewId).copy(dayId = Some(dayId)))
      .async(parse.json(bindShiftIdsReads)) { req =>
        DayDate.createFromDayId(dayId) map { day =>
          crewAttendancesService
            .removeAttendancesForCrew(crewId, day, req.body.shiftIds)(
              req.tenant)
            .map(_ => NoContent)
        } getOrElse Future.successful(BadRequest("Could not parse day id"))
      }

  def findAllPerCrew: Action[AnyContent] =
    action(crewAttendanceRetrieve).async { req =>
      crewAttendancesService
        .findAll(req.tenant)
        .map(all => Ok(Json.toJson(all)))
    }

  def findAllPerDay: Action[AnyContent] =
    action(crewAttendanceRetrieve).async { req =>
      implicit val shiftWithAttendancesFormat =
        Json.format[ShiftWithAttendances]
      implicit val attendancesOnDayFormat = Json.format[AttendancesOnDay]
      crewAttendancesService
        .findAllPerDay(req.tenant)
        .map(all => Ok(Json.toJson(all)))
    }

  def findAllRaw: Action[AnyContent] = action(crewAttendanceRetrieve).async {
    req =>
      implicit val writes = new Writes[(Day.Id, Shift.Id, Crew.Id)] {
        override def writes(o: (Day.Id, Shift.Id, Crew.Id)): JsValue =
          o match {
            case (dayId, shiftId, crewId) =>
              Json.obj(
                "dayId" -> dayId,
                "shiftId" -> shiftId,
                "crewId" -> crewId
              )
          }
      }

      crewAttendancesService
        .findAllRaw(req.tenant)
        .map(all => Ok(Json.toJson(all)))
  }
}
