package be.thomastoye.speelsysteem.data.couchdb

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.{ ChildRepository, PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat }
import be.thomastoye.speelsysteem.data.util.ScalazExtensions.PimpedScalazTask
import upickle.default.{ Reader, Writer }
import be.thomastoye.speelsysteem.models._
import be.thomastoye.speelsysteem.models.JsonFormats._
import be.thomastoye.speelsysteem.models.Child.Id
import com.ibm.couchdb.{ CouchDoc, CouchException, MappedDocType, TypeMapping }
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Promise }
import scalaz.{ -\/, \/- }

object CouchChildRepository {
  val childKind = "type/child/v1"

  implicit val childReader: Reader[Child] = new PlayJsonReaderUpickleCompat[Child]
  implicit val childWriter: Writer[Child] = new PlayJsonWriterUpickleCompat[Child]
}

class CouchChildRepository @Inject() (couchDatabase: CouchDatabase) extends ChildRepository with StrictLogging {
  import CouchChildRepository._

  private val db = couchDatabase.getDb("children", TypeMapping(classOf[Child] -> CouchChildRepository.childKind))

  override def findById(id: Id): Future[Option[EntityWithId[Id, Child]]] = {
    val p: Promise[Option[EntityWithId[Id, Child]]] = Promise()

    db.docs.get[Child](id).unsafePerformAsync {
      case \/-(res) => p.success(Some(EntityWithId(res._id, res.doc)))
      case -\/(e) => e match {
        case _: CouchException[_] => p.success(None)
        case _ => p.failure(e)
      }
    }

    p.future
  }

  override def findAll: Future[Seq[EntityWithId[Id, Child]]] = {
    val p: Promise[Seq[EntityWithId[Id, Child]]] = Promise()

    db.docs.getMany.byType[String]("all", "children", MappedDocType(childKind)).includeDocs.build.query.unsafePerformAsync {
      case \/-(res) => p.success(res.getDocs.map(doc => EntityWithId(doc._id, doc.doc)))
      case -\/(e) => p.failure(e)
    }

    p.future.map(_.sortBy(x => (x.entity.lastName, x.entity.firstName)))
  }

  override def insert(id: Id, child: Child): Future[Id] = db.docs.create[Child](child, id).toFuture.map(_.id)

  override def count: Future[Int] = findAll.map(_.length)

  override def update(id: Id, child: Child): Future[Unit] = {
    for {
      currentRev <- db.docs.get[Child](id).toFuture.map(_._rev)
      res <- db.docs.update[Child](CouchDoc(child, childKind, _id = id, _rev = currentRev)).toFuture
    } yield { () }
  }

  override def delete(id: Id): Future[Unit] = ???
}
