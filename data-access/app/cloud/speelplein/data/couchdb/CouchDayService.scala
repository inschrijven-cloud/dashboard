package cloud.speelplein.data.couchdb

import javax.inject.Inject

import cloud.speelplein.EntityWithId
import cloud.speelplein.data.couchdb.CouchDayService.{
  dayKind,
  dayReader,
  dayWriter
}
import cloud.speelplein.data.util.ScalazExtensions.PimpedScalazTask
import cloud.speelplein.data.{
  DayService,
  PlayJsonReaderUpickleCompat,
  PlayJsonWriterUpickleCompat
}
import cloud.speelplein.models.Day.Id
import cloud.speelplein.models.JsonFormats.dayFormat
import cloud.speelplein.models.{Day, Tenant}
import com.ibm.couchdb.{CouchException, MappedDocType, TypeMapping}
import com.typesafe.scalalogging.StrictLogging
import upickle.default.{Reader, Writer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scalaz.{-\/, \/-}

object CouchDayService extends StrictLogging {
  val dayKind = "type/day/v1"

  implicit val dayReader: Reader[Day] = new PlayJsonReaderUpickleCompat[Day]
  implicit val dayWriter: Writer[Day] = new PlayJsonWriterUpickleCompat[Day]
}

class CouchDayService @Inject()(couchDatabase: CouchDatabase)
    extends StrictLogging
    with DayService {

  private def db(tenant: Tenant) =
    couchDatabase.getDb(TypeMapping(classOf[Day] -> CouchDayService.dayKind),
                        tenant)

  override def findAll(
      implicit tenant: Tenant): Future[Seq[EntityWithId[Id, Day]]] = {
    db(tenant).docs.getMany
      .byType[String]("all-days", "default", MappedDocType(dayKind))
      .includeDocs[Day]
      .build
      .query
      .toFuture
      .map(res => res.getDocs.map(doc => EntityWithId(doc._id, doc.doc)))
      .map(_.sortBy(x => x.entity.date).reverse)
  }

  override def insert(day: Day)(implicit tenant: Tenant): Future[Unit] =
    db(tenant).docs.create[Day](day, day.date.getDayId).toFuture.map(_ => ())

  override def findById(id: Id)(
      implicit tenant: Tenant): Future[Option[EntityWithId[Id, Day]]] = {
    val p: Promise[Option[EntityWithId[Id, Day]]] = Promise()

    db(tenant).docs.get[Day](id).unsafePerformAsync {
      case \/-(res) => p.success(Some(EntityWithId(res._id, res.doc)))
      case -\/(e) =>
        e match {
          case _: CouchException[_] => p.success(None)
          case _                    => p.failure(e)
        }
    }

    p.future
  }

  override def update(id: Id, day: Day)(implicit tenant: Tenant): Future[Unit] =
    ???
}
