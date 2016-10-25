package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.data.{ChildRepository, DayService}
import be.thomastoye.speelsysteem.models.{Child, Day, Shift}
import be.thomastoye.speelsysteem.models.JsonFormats.{dayFormat, dayWithIdWrites}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Action, AnyContent, BodyParsers}
import play.api.libs.concurrent.Execution.Implicits._

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

class ChildAttendanceApiController @Inject()(childRepository: ChildRepository, dayService: DayService) extends ApiController {
  import ChildAttendanceApiController._

  def numberOfChildAttendances: Action[AnyContent] = Action.async { req =>
    childRepository.findAll map (_.map(_.entity)) flatMap dayService.findNumberOfChildAttendances map { all =>
      Ok(Json.toJson(all))
    }
  }

  def numberOfChildAttendancesOnDay(id: Day.Id): Action[AnyContent] = TODO

  def getAttendancesForChild(id: Child.Id): Action[AnyContent] = Action.async { req =>
    dayService.findAttendancesForChild(id).map(att => Ok(Json.toJson(att)))
  }

  def addAttendancesForChild(childId: Child.Id, dayId: Day.Id): Action[BindShiftIds] = Action.async(BodyParsers.parse.json(bindShiftIdsReads)) { req =>
    childRepository.addAttendancesForChild(childId, dayId, req.body.shiftIds) map { foundOpt =>
      foundOpt.map(_ => Ok).getOrElse(NotFound)
    }
  }

  def deleteAttendancesForChild(childId: Child.Id, dayId: Day.Id): Action[AnyContent] = TODO
}
