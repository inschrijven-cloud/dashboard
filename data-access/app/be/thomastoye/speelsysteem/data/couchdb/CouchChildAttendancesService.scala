package be.thomastoye.speelsysteem.data.couchdb

import java.time.Instant
import javax.inject.Inject

import be.thomastoye.speelsysteem.data.ChildAttendancesService.{ AttendancesOnDay, ShiftWithAttendances }
import be.thomastoye.speelsysteem.data.{ ChildAttendancesService, PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat }
import be.thomastoye.speelsysteem.models._
import be.thomastoye.speelsysteem.data.util.ScalazExtensions._
import be.thomastoye.speelsysteem.models.Day.Id
import com.ibm.couchdb.{ CouchDbApi, MappedDocType, Res, TypeMapping }
import com.typesafe.scalalogging.StrictLogging
import upickle.default.{ Reader, Writer }

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json

import scala.concurrent.Future

object CouchChildAttendancesService {
  val childAttendanceKind = "type/childattendance/v1"
  case class ChildAttendancePersisted(registeredTimeStamp: Option[Instant], registeredByCrew: Option[Crew.Id])

  implicit val childAttendancePersistedReader: Reader[ChildAttendancePersisted] = new PlayJsonReaderUpickleCompat[ChildAttendancePersisted]()(Json.format[ChildAttendancePersisted])
  implicit val childAttendancePersistedWriter: Writer[ChildAttendancePersisted] = new PlayJsonWriterUpickleCompat[ChildAttendancePersisted]()(Json.format[ChildAttendancePersisted])
}

class CouchChildAttendancesService @Inject() (couchDatabase: CouchDatabase) extends ChildAttendancesService with StrictLogging {
  import CouchChildAttendancesService._

  private val db = couchDatabase.getDb("childattendance", TypeMapping(classOf[ChildAttendancePersisted] -> childAttendanceKind))

  override def findAttendancesForChild(childId: Child.Id): Future[Seq[DayAttendance]] = findAll map { _.getOrElse(childId, Seq.empty) }

  override def findNumberAttendancesForChild(childId: Child.Id): Future[Option[Int]] = findAll.map(_.get(childId).map(_.map(_.shifts.length).sum))

  override def findNumberOfChildAttendances: Future[Map[Day.Id, Map[Shift.Id, Int]]] = findAll map { all =>
    val groupedByDay = all.toSeq flatMap {
      case (childId, childAttendances) =>
        childAttendances.flatMap(att => att.shifts.map(shift => (att.day, shift.shiftId)))
    } groupBy (_._1)

    groupedByDay map {
      case (dayId, data) =>
        val shiftsWithLength = data.groupBy(_._2).map {
          case (shiftId, seq) =>
            (shiftId, seq.length)
        }

        (dayId, shiftsWithLength)
    }
  }

  override def findNumberOfChildAttendances(day: DayDate, shiftId: Shift.Id): Future[Int] = ???

  override def addAttendancesForChild(childId: Child.Id, day: DayDate, shifts: Seq[Shift.Id]): Future[Seq[Res.DocOk]] = {
    val many: Map[String, ChildAttendancePersisted] = Map(shifts.map { shiftId =>
      createChildAttendanceId(day.getDayId, shiftId, childId) -> ChildAttendancePersisted(Some(Instant.now), None)
    }: _*)

    // Only insert attendances that do not exist already
    // TODO what happens with deleted attendances?
    db.docs.getMany.allowMissing.withIds(many.keys.toSeq).build.query.toFuture.map(_.rows.flatMap(_.toList).map(_.id)) flatMap { existingIds =>
      db.docs.createMany(many.filterNot { case (key, value) => existingIds.contains(key) }).toFuture
    }
  }

  override def addAttendanceForChild(childId: Child.Id, day: DayDate, shift: Shift.Id): Future[Res.DocOk] = {
    db
      .docs.create(ChildAttendancePersisted(Some(Instant.now), None), id = createChildAttendanceId(day.getDayId, shift, childId))
      .toFuture
  }

  override def removeAttendancesForChild(childId: Child.Id, day: DayDate, shifts: Seq[Shift.Id]): Future[Unit] = ???

  override def findAll: Future[Map[Child.Id, Seq[DayAttendance]]] = {
    db
      .docs.getMany.byType[String]("all", "childattendance", MappedDocType(childAttendanceKind))
      .includeDocs
      .build
      .query
      .toFuture
      .map(_.rows.map(doc => createDayAttendance(doc.id, doc.doc.doc)))
      .map(_.groupBy(_._1).map {
        case (childId, dayAtts) =>
          val dayAttendancesReduced = dayAtts
            .map(_._2)
            .groupBy(dayAtt => dayAtt.day)
            .map { case (dayId, dayAttendancesOnDayId) => DayAttendance(dayId, dayAttendancesOnDayId.flatMap(_.shifts)) }

          (childId, dayAttendancesReduced.toSeq)
      })
  }

  override def findAllPerDay: Future[Map[Id, AttendancesOnDay]] = {
    db.docs.getMany.byType[String]("all", "childattendance", MappedDocType(childAttendanceKind))
      .includeDocs
      .build
      .query
      .toFuture
      .map(_.rows.map(doc => createDayAttendance(doc.id, doc.doc.doc)))
      .map {
        _.groupBy(_._2.day) map {
          case (dayId, seq) =>
            val shiftWithAttendances = seq.map(_._2).flatMap(_.shifts).map(_.shiftId).groupBy(x => x).mapValues(_.length).map {
              case (shiftId, numAtt) =>
                ShiftWithAttendances(shiftId, numAtt)
            }.toSeq

            val uniqueChildren = seq.map(_._1).distinct.length

            (dayId, AttendancesOnDay(uniqueChildren, shiftWithAttendances))
        }
      }
  }

  override def findAllRaw: Future[Seq[(Day.Id, Shift.Id, Child.Id)]] = {
    db.docs.getMany.byType[String]("all", "childattendance", MappedDocType(childAttendanceKind))
      .build
      .query
      .toFuture
      .map(x => x.rows.map(y => createFromChildAttendanceId(y.id)))
  }

  private def createDayAttendance(idFromDb: String, persisted: ChildAttendancePersisted): (Child.Id, DayAttendance) = {
    val dayId = createFromChildAttendanceId(idFromDb)._1
    val shiftId = createFromChildAttendanceId(idFromDb)._2
    val childId = createFromChildAttendanceId(idFromDb)._3

    (childId, DayAttendance(dayId, Seq(SingleAttendance(shiftId, persisted.registeredByCrew, persisted.registeredTimeStamp))))
  }

  private def createFromChildAttendanceId(id: String): (Day.Id, Shift.Id, Child.Id) = {
    (id.split("--")(0), id.split("--")(1), id.split("--")(2))
  }

  private def createChildAttendanceId(dayId: Day.Id, shiftId: Shift.Id, childId: Child.Id): String = s"$dayId--$shiftId--$childId"
}
