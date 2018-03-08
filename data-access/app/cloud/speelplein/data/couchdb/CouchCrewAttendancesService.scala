package cloud.speelplein.data.couchdb

import java.time.Instant
import javax.inject.Inject

import cloud.speelplein.data.CrewAttendancesService.{
  AttendancesOnDay,
  ShiftWithAttendances
}
import cloud.speelplein.data.couchdb.CouchCrewAttendancesService._
import cloud.speelplein.data.util.ScalazExtensions._
import cloud.speelplein.data.{
  CrewAttendancesService,
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

object CouchCrewAttendancesService {
  val crewAttendanceKind = "type/crewattendance/v2"
  case class CrewAttendancePersisted(
      /** When the crew member was enrolled (intention to participate in an activity) */
      enrolled: Option[Instant] = None,
      /** Who registered the crew member's intent to participate in an activity */
      enrolledRegisteredBy: Option[Crew.Id] = None,
      /** When the crew member arrived to participate in an activity */
      arrived: Option[Instant] = None,
      /** Which crew member registered the crew member as arrived */
      arrivedRegisteredBy: Option[Crew.Id] = None,
      /** When the crew member left/went home after the activity */
      left: Option[Instant] = None,
      /** Who registered the crew member leaving */
      leftRegisteredBy: Option[Crew.Id] = None
  )

  implicit val crewAttendancePersistedReader: Reader[CrewAttendancePersisted] =
    new PlayJsonReaderUpickleCompat[CrewAttendancePersisted]()(
      Json.format[CrewAttendancePersisted]
    )

  implicit val crewAttendancePersistedWriter: Writer[CrewAttendancePersisted] =
    new PlayJsonWriterUpickleCompat[CrewAttendancePersisted]()(
      Json.format[CrewAttendancePersisted]
    )
}

class CouchCrewAttendancesService @Inject()(couchDatabase: CouchDatabase)
    extends CrewAttendancesService
    with StrictLogging {

  private def db(implicit tenant: Tenant) =
    couchDatabase.getDb(
      TypeMapping(classOf[CrewAttendancePersisted] -> crewAttendanceKind),
      tenant)

  override def findAttendancesForCrew(crewId: Crew.Id)(
      implicit tenant: Tenant): Future[Seq[DayAttendance]] = {
    findAll map { _.getOrElse(crewId, Seq.empty) }
  }

  override def findNumberAttendancesForCrew(crewId: Crew.Id)(
      implicit tenant: Tenant): Future[Option[Int]] = {
    findAll.map(_.get(crewId).map(_.map(_.shifts.length).sum))
  }

  override def findNumberOfCrewAttendances(
      implicit tenant: Tenant): Future[Map[Day.Id, Map[Shift.Id, Int]]] =
    findAll map { all =>
      val groupedByDay = all.toSeq flatMap {
        case (crewId, crewAttendances) =>
          crewAttendances.flatMap(att =>
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

  override def findNumberOfCrewAttendances(day: DayDate, shiftId: Shift.Id)(
      implicit tenant: Tenant): Future[Int] = ???

  override def addAttendancesForCrew(crewId: Crew.Id,
                                     day: DayDate,
                                     shifts: Seq[Shift.Id])(
      implicit tenant: Tenant): Future[Seq[Res.DocOk]] = {
    val many: Map[String, CrewAttendancePersisted] = Map(shifts.map { shiftId =>
      createCrewAttendanceId(day.getDayId, shiftId, crewId) -> CrewAttendancePersisted(
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
                    .update[CrewAttendancePersisted](
                      CouchDoc(
                        CrewAttendancePersisted(enrolled = Some(Instant.now)),
                        kind = crewAttendanceKind,
                        _id = id)
                    )
                    .toFuture)
          ) map (_ ++ seq)
        }
      }
    }
  }

  override def addAttendanceForCrew(
      crewId: Crew.Id,
      day: DayDate,
      shift: Shift.Id)(implicit tenant: Tenant): Future[Res.DocOk] = {
    addAttendancesForCrew(crewId, day, Seq(shift)).map(_.head)
  }

  override def removeAttendancesForCrew(
      crewId: Crew.Id,
      day: DayDate,
      shifts: Seq[Shift.Id])(implicit tenant: Tenant): Future[Unit] = {
    db.docs
      .getMany(shifts.map(shiftId =>
        createCrewAttendanceId(day.getDayId, shiftId, crewId)))
      .toFuture flatMap { docs =>
      db.docs.deleteMany(docs.getDocs).toFuture.map(_ => ())
    }
  }

  override def findAll(
      implicit tenant: Tenant): Future[Map[Crew.Id, Seq[DayAttendance]]] = {
    db.docs.getMany
      .byType[String]("all-crew-attendances",
                      "default",
                      MappedDocType(crewAttendanceKind))
      .includeDocs
      .build
      .query
      .toFuture
      .map(_.rows.map(doc => createDayAttendance(doc.id, doc.doc.doc)))
      .map(_.groupBy { case (crewId, dayAtt) => crewId }.map {
        case (crewId, dayAtts) =>
          val dayAttendancesReduced = dayAtts
            .map { case (crewId, dayAtt) => dayAtt }
            .groupBy(dayAtt => dayAtt.day)
            .map {
              case (dayId, dayAttendancesOnDayId) =>
                DayAttendance(dayId, dayAttendancesOnDayId.flatMap(_.shifts))
            }

          (crewId, dayAttendancesReduced.toSeq)
      })
  }

  override def findAllPerDay(
      implicit tenant: Tenant): Future[Map[Id, AttendancesOnDay]] = {
    db.docs.getMany
      .byType[String]("all-crew-attendances",
                      "default",
                      MappedDocType(crewAttendanceKind))
      .includeDocs
      .build
      .query
      .toFuture
      .map(_.rows.map(doc => createDayAttendance(doc.id, doc.doc.doc)))
      .map {
        _.groupBy { case (crewId, dayAtt) => dayAtt.day } map {
          case (dayId, seq) =>
            val shiftWithAttendances = seq
              .map { case (crewId, dayAtt) => dayAtt }
              .flatMap(_.shifts)
              .map(_.shiftId)
              .groupBy(x => x)
              .mapValues(_.length)
              .map {
                case (shiftId, numAtt) =>
                  ShiftWithAttendances(shiftId, numAtt)
              }
              .toSeq

            val uniqueCrew =
              seq.map { case (dayAtt, crewId) => crewId }.distinct.length

            (dayId, AttendancesOnDay(uniqueCrew, shiftWithAttendances))
        }
      }
  }

  override def findAllRaw(
      implicit tenant: Tenant): Future[Seq[(Day.Id, Shift.Id, Crew.Id)]] = {
    db.docs.getMany
      .byType[String]("all-crew-attendances",
                      "default",
                      MappedDocType(crewAttendanceKind))
      .build
      .query
      .toFuture
      .map(x => x.rows.map(y => createFromCrewAttendanceId(y.id)))
  }

  override def count(implicit tenant: Tenant): Future[Int] =
    db.query
      .view[String, Int]("default", "all-crew-attendances-count")
      .get
      .group(true)
      .reduce[Int]
      .build
      .query
      .toFuture
      .map(_.rows.head.value)

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
                                  persisted: CrewAttendancePersisted)(
      implicit tenant: Tenant): (Crew.Id, DayAttendance) = {

    val (dayId, shiftId, crewId) = createFromCrewAttendanceId(idFromDb)

    (crewId,
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

  private def createFromCrewAttendanceId(id: String)(
      implicit tenant: Tenant): (Day.Id, Shift.Id, Crew.Id) = {
    (id.split("--")(0), id.split("--")(1), id.split("--")(2))
  }

  private def createCrewAttendanceId(
      dayId: Day.Id,
      shiftId: Shift.Id,
      crewId: Crew.Id)(implicit tenant: Tenant): String =
    s"$dayId--$shiftId--$crewId"
}
