package cloud.speelplein.data.couchdb

import java.time.Instant
import javax.inject.Inject

import cloud.speelplein.data.ChildAttendancesService.{
  AttendancesOnDay,
  ShiftWithAttendances
}
import cloud.speelplein.data.couchdb.CouchChildAttendancesService._
import cloud.speelplein.data.util.ScalazExtensions._
import cloud.speelplein.data.{
  ChildAttendancesService,
  PlayJsonReaderUpickleCompat,
  PlayJsonWriterUpickleCompat
}
import cloud.speelplein.models.Day.Id
import cloud.speelplein.models._
import com.ibm.couchdb.{
  CouchDoc,
  CouchException,
  MappedDocType,
  Res,
  TypeMapping
}
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.Json
import upickle.default.{Reader, Writer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CouchChildAttendancesService {
  val childAttendanceKind = "type/childattendance/v2"
  case class ChildAttendancePersisted(
      /** When the child was enrolled (intention to participate in an activity) */
      enrolled: Option[Instant] = None,
      /** Who registered the child's intent to participate in an activity */
      enrolledRegisteredBy: Option[Crew.Id] = None,
      /** When the child arrived to participate in an activity */
      arrived: Option[Instant] = None,
      /** Which crew member registered the child as arrived */
      arrivedRegisteredBy: Option[Crew.Id] = None,
      /** When the child left/went home after the activity */
      left: Option[Instant] = None,
      /** Who registered the child leaving */
      leftRegisteredBy: Option[Crew.Id] = None
  )

  implicit val childAttendancePersistedReader
    : Reader[ChildAttendancePersisted] =
    new PlayJsonReaderUpickleCompat[ChildAttendancePersisted]()(
      Json.format[ChildAttendancePersisted]
    )

  implicit val childAttendancePersistedWriter
    : Writer[ChildAttendancePersisted] =
    new PlayJsonWriterUpickleCompat[ChildAttendancePersisted]()(
      Json.format[ChildAttendancePersisted]
    )
}

class CouchChildAttendancesService @Inject()(couchDatabase: CouchDatabase)
    extends ChildAttendancesService
    with StrictLogging {

  private def db(implicit tenant: Tenant) =
    couchDatabase.getDb(
      TypeMapping(classOf[ChildAttendancePersisted] -> childAttendanceKind),
      tenant)

  override def findAttendancesForChild(childId: Child.Id)(
      implicit tenant: Tenant): Future[Seq[DayAttendance]] = {
    findAll map { _.getOrElse(childId, Seq.empty) }
  }

  override def findNumberAttendancesForChild(childId: Child.Id)(
      implicit tenant: Tenant): Future[Option[Int]] = {
    findAll.map(_.get(childId).map(_.map(_.shifts.length).sum))
  }

  override def findNumberOfChildAttendances(
      implicit tenant: Tenant): Future[Map[Day.Id, Map[Shift.Id, Int]]] =
    findAll map { all =>
      val groupedByDay = all.toSeq flatMap {
        case (childId, childAttendances) =>
          childAttendances.flatMap(att =>
            att.shifts.map(shift => (att.day, shift.shiftId)))
      } groupBy { case (dayId, shiftId) => dayId }

      groupedByDay map {
        case (dayId, data) =>
          val shiftsWithLength =
            data.groupBy { case (_, shiftId) => shiftId }.map {
              case (shiftId, seq) =>
                (shiftId, seq.length)
            }

          (dayId, shiftsWithLength)
      }
    }

  override def findNumberOfChildAttendances(day: DayDate, shiftId: Shift.Id)(
      implicit tenant: Tenant): Future[Int] = ???

  override def addAttendancesForChild(childId: Child.Id,
                                      day: DayDate,
                                      shifts: Seq[Shift.Id])(
      implicit tenant: Tenant): Future[Seq[Res.DocOk]] = {
    val many: Map[String, ChildAttendancePersisted] = Map(shifts.map {
      shiftId =>
        createChildAttendanceId(day.getDayId, shiftId, childId) -> ChildAttendancePersisted(
          enrolled = Some(Instant.now))
    }: _*)

    // Only insert attendances that do not exist already
    // what happens with deleted attendances?
    //  -> They still exist in the database. Getting them yields {"error":"not_found","reason":"deleted"}
    //  You can PUT deleted documents to create a new one. Just POST'ing doesn't do the trick.

    db.docs.getMany.allowMissing
      .withIds(many.keys.toSeq)
      .build
      .query
      .toFuture
      .map(_.rows.flatMap(_.toList).map(_.id)) flatMap { existingIds =>
      db.docs.createMany {
        many.filterNot { case (key, value) => existingIds.contains(key) }
      }.toFuture flatMap { seq =>
        Future.sequence(existingIds map (id => isDeleted(id) map ((id, _)))) map (_.filter {
          case (id, isDeleted) => isDeleted
        }) flatMap { list =>
          Future.sequence(
            list
              .map { case (id, _) => id }
              .map(
                id =>
                  db.docs
                    .update[ChildAttendancePersisted](
                      CouchDoc(
                        ChildAttendancePersisted(enrolled = Some(Instant.now)),
                        kind = childAttendanceKind,
                        _id = id)
                    )
                    .toFuture)
          ) map (_ ++ seq)
        }
      }
    }
  }

  override def addAttendanceForChild(
      childId: Child.Id,
      day: DayDate,
      shift: Shift.Id)(implicit tenant: Tenant): Future[Res.DocOk] = {
    addAttendancesForChild(childId, day, Seq(shift)).map(_.head)
  }

  override def removeAttendancesForChild(
      childId: Child.Id,
      day: DayDate,
      shifts: Seq[Shift.Id])(implicit tenant: Tenant): Future[Unit] = {
    db.docs
      .getMany(shifts.map(shiftId =>
        createChildAttendanceId(day.getDayId, shiftId, childId)))
      .toFuture flatMap { docs =>
      db.docs.deleteMany(docs.getDocs).toFuture.map(_ => ())
    }
  }

  override def findAll(
      implicit tenant: Tenant): Future[Map[Child.Id, Seq[DayAttendance]]] = {
    db.docs.getMany
      .byType[String]("all-child-attendances",
                      "default",
                      MappedDocType(childAttendanceKind))
      .includeDocs
      .build
      .query
      .toFuture
      .map(_.rows.map(doc => createDayAttendance(doc.id, doc.doc.doc)))
      .map(_.groupBy { case (childId, dayAtt) => childId }.map {
        case (childId, dayAtts) =>
          val dayAttendancesReduced = dayAtts
            .map { case (childId, dayAtt) => dayAtt }
            .groupBy(dayAtt => dayAtt.day)
            .map {
              case (dayId, dayAttendancesOnDayId) =>
                DayAttendance(dayId, dayAttendancesOnDayId.flatMap(_.shifts))
            }

          (childId, dayAttendancesReduced.toSeq)
      })
  }

  override def findAllPerDay(
      implicit tenant: Tenant): Future[Map[Id, AttendancesOnDay]] = {
    db.docs.getMany
      .byType[String]("all-child-attendances",
                      "default",
                      MappedDocType(childAttendanceKind))
      .includeDocs
      .build
      .query
      .toFuture
      .map(_.rows.map(doc => createDayAttendance(doc.id, doc.doc.doc)))
      .map {
        _.groupBy { case (childId, dayAtt) => dayAtt.day } map {
          case (dayId, seq) =>
            val shiftWithAttendances = seq
              .map { case (childId, dayAtt) => dayAtt }
              .flatMap(_.shifts)
              .map(_.shiftId)
              .groupBy(x => x)
              .mapValues(_.length)
              .map {
                case (shiftId, numAtt) =>
                  ShiftWithAttendances(shiftId, numAtt)
              }
              .toSeq

            val uniqueChildren =
              seq.map { case (dayAtt, childId) => childId }.distinct.length

            (dayId, AttendancesOnDay(uniqueChildren, shiftWithAttendances))
        }
      }
  }

  override def findAllRaw(
      implicit tenant: Tenant): Future[Seq[(Day.Id, Shift.Id, Child.Id)]] = {
    db.docs.getMany
      .byType[String]("all-child-attendances",
                      "default",
                      MappedDocType(childAttendanceKind))
      .build
      .query
      .toFuture
      .map(x => x.rows.map(y => createFromChildAttendanceId(y.id)))
  }

  private def isDeleted(attendanceId: String)(
      implicit tenant: Tenant): Future[Boolean] =
    db.docs
      .get(attendanceId)
      .toFuture
      .map(_ => false) // exists
      .recover {
        case e: CouchException[Res.Error] =>
          e.content.reason == "deleted" // either "deleted" or "missing"
        case _ => false // other error
      }

  private def createDayAttendance(idFromDb: String,
                                  persisted: ChildAttendancePersisted)(
      implicit tenant: Tenant): (Child.Id, DayAttendance) = {

    val (dayId, shiftId, childId) = createFromChildAttendanceId(idFromDb)

    (childId,
     DayAttendance(
       dayId,
       Seq(
         SingleAttendance(
           shiftId,
           persisted.enrolled,
           persisted.enrolledRegisteredBy,
           persisted.arrived,
           persisted.arrivedRegisteredBy,
           persisted.left,
           persisted.leftRegisteredBy
         ))
     ))
  }

  override def count(implicit tenant: Tenant): Future[Int] =
    db.query
      .view[String, Int]("default", "all-child-attendances-count")
      .get
      .group(true)
      .reduce[Int]
      .build
      .query
      .toFuture
      .map(_.rows.head.value)

  private def createFromChildAttendanceId(id: String)(
      implicit tenant: Tenant): (Day.Id, Shift.Id, Child.Id) = {
    (id.split("--")(0), id.split("--")(1), id.split("--")(2))
  }

  private def createChildAttendanceId(
      dayId: Day.Id,
      shiftId: Shift.Id,
      childId: Child.Id)(implicit tenant: Tenant): String =
    s"$dayId--$shiftId--$childId"
}
