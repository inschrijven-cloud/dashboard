package be.thomastoye.speelsysteem.data.couchdb

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.{ DayService, PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat }
import upickle.default.{ Reader, Writer }
import be.thomastoye.speelsysteem.models._
import be.thomastoye.speelsysteem.models.JsonFormats._
import be.thomastoye.speelsysteem.data.util.ScalazExtensions.PimpedScalazTask
import be.thomastoye.speelsysteem.models.Day.Id
import com.ibm.couchdb.{ CouchException, MappedDocType, TypeMapping }
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.{ Future, Promise }
import scalaz.{ -\/, \/- }

object CouchDayService extends StrictLogging {
  val dayKind = "type/day/v1"

  implicit val dayReader: Reader[Day] = new PlayJsonReaderUpickleCompat[Day]
  implicit val dayWriter: Writer[Day] = new PlayJsonWriterUpickleCompat[Day]
}

class CouchDayService @Inject() (couchDatabase: CouchDatabase) extends StrictLogging with DayService {
  import CouchDayService._

  private val db = couchDatabase.getDb("days", TypeMapping(classOf[Day] -> CouchDayService.dayKind))

  override def findAll: Future[Seq[EntityWithId[Id, Day]]] = {
    db.docs.getMany
      .byType[String]("all", "days", MappedDocType(dayKind))
      .includeDocs[Day].build.query.toFuture
      .map(res => res.getDocs.map(doc => EntityWithId(doc._id, doc.doc)))
      .map(_.sortBy(x => x.entity.date).reverse)
  }

  override def insert(day: Day): Future[Unit] = db.docs.create[Day](day, day.date.getDayId).toFuture.map(_ => ())

  override def findById(id: Id): Future[Option[EntityWithId[Id, Day]]] = {
    val p: Promise[Option[EntityWithId[Id, Day]]] = Promise()

    db.docs.get[Day](id).unsafePerformAsync {
      case \/-(res) => p.success(Some(EntityWithId(res._id, res.doc)))
      case -\/(e) => e match {
        case _: CouchException[_] => p.success(None)
        case _ => p.failure(e)
      }
    }

    p.future
  }

  override def update(id: Id, day: Day): Future[Unit] = ???
}
