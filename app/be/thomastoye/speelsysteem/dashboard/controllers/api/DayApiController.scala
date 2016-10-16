package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.data.ChildRepository
import be.thomastoye.speelsysteem.data.couchdb.DayService
import be.thomastoye.speelsysteem.models.JsonFormats.{dayFormat, dayWithIdWrites}
import be.thomastoye.speelsysteem.models.Shift.Id
import be.thomastoye.speelsysteem.models.{Child, Day, Shift}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc._

class DayApiController @Inject() (dayService: DayService, childRepository: ChildRepository) extends Controller {
  protected case class BindShiftIds(shiftIds: Seq[Shift.Id])

  protected val bindShiftIdsReads = Json.reads[BindShiftIds]

  protected implicit val childAttendanceWrites = new Writes[(Day.Id, Seq[(Shift.Id, Int)])] {
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

  def all = Action.async { req => dayService.findAll.map(days => Ok(Json.toJson(days))) }

  def numberOfChildAttendances = Action.async { req =>
    childRepository.findAll map(_.map(_._2)) flatMap dayService.findNumberOfChildAttendances map { all =>
      Ok(Json.toJson(all))
    }
  }

  def getAttendancesForChild(id: Child.Id) = Action.async { req =>
    dayService.findAttendancesForChild(id).map(att => Ok(Json.toJson(att)))
  }

  def addAttendancesForChild(childId: Child.Id, dayId: Day.Id) = Action.async(BodyParsers.parse.json(bindShiftIdsReads)) { req =>
    childRepository.addAttendancesForChild(childId, dayId, req.body.shiftIds) map { foundOpt =>
      foundOpt.map(_ => Ok).getOrElse(NotFound)
    }
  }
}
