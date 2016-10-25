package be.thomastoye.speelsysteem.data.couchdb

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.{ChildRepository, PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat}
import be.thomastoye.speelsysteem.data.util.ScalazExtensions.PimpedScalazTask
import upickle.default.{Reader, Writer}
import be.thomastoye.speelsysteem.models._
import be.thomastoye.speelsysteem.models.Child.Id
import be.thomastoye.speelsysteem.models.Day.Id
import be.thomastoye.speelsysteem.models.JsonFormats._
import be.thomastoye.speelsysteem.models.Child.Id
import com.ibm.couchdb.{CouchDoc, MappedDocType}
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.{Future, Promise}
import scalaz.{-\/, \/-}

object CouchChildRepository {
  val childKind = "type/child/v1"

  implicit val childReader: Reader[Child] = new PlayJsonReaderUpickleCompat[Child]
  implicit val childWriter: Writer[Child] = new PlayJsonWriterUpickleCompat[Child]
}

class CouchChildRepository @Inject() (couchDatabase: CouchDatabase) extends ChildRepository with StrictLogging {
  import CouchChildRepository._

  val db = couchDatabase.db

  override def findById(id: Id) = findAll.map(_.find(_.id == id))

  override def findAll: Future[Seq[EntityWithId[Id, Child]]] = {
    val p: Promise[Seq[EntityWithId[Id, Child]]] = Promise()

    db.docs.getMany.byTypeUsingTemporaryView(MappedDocType(childKind)).includeDocs[Child].build.query.unsafePerformAsync {
      case \/-(res) => p.success(res.getDocs.map(doc => EntityWithId(doc._id, doc.doc)))
      case -\/(e)   => p.failure(e)
    }

    p.future.map(_.sortBy(x => (x.entity.lastName, x.entity.firstName)))
  }

  override def insert(id: Id, child: Child): Future[Id] = db.docs.create[Child](child, id).toFuture.map(_.id)

  override def count: Future[Int] = findAll.map(_.length)

  override def update(id: Id, child: Child): Future[Unit] = {
    for{
      currentRev <- db.docs.get[Child](id).toFuture.map(_._rev)
      res        <- db.docs.update[Child](CouchDoc(child, childKind, _id = id, _rev = currentRev)).toFuture
    } yield { () }
  }

  override def addAttendancesForChild(id: Id, dayId: Id, shifts: Seq[Id]): Future[Option[Unit]] = {
    findById(id) flatMap { childWithIdOpt =>
      if(childWithIdOpt.isDefined) {
        val childWithId = childWithIdOpt.get
        val child = addShifts(childWithId.entity, dayId, shifts)
        update(childWithId.id, child).map(_ => Some( () ))
      } else Future.successful(None)
    }
  }

  override def delete(id: Id): Future[Unit] = ???

  private def addShifts(child: Child, dayId: Id, shifts: Seq[Id]) = {
    val newAttendance = child.attendances
      .find(_.day == dayId).map(att => Attendance(att.day, (att.shifts ++ shifts).distinct))
      .getOrElse(Attendance(dayId, shifts))

    val newAttendances =  child.attendances.filterNot(_.day == dayId) :+ newAttendance

    child.copy(attendances = newAttendances)
  }
}
