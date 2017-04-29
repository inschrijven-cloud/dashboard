package be.thomastoye.speelsysteem.data.couchdb

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.{ ContactPersonRepository, PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat }
import be.thomastoye.speelsysteem.data.util.ScalazExtensions.PimpedScalazTask
import be.thomastoye.speelsysteem.models.ContactPerson
import upickle.default.{ Reader, Writer }
import be.thomastoye.speelsysteem.models.JsonFormats._
import be.thomastoye.speelsysteem.models.ContactPerson.Id
import com.ibm.couchdb.{ CouchDoc, CouchException, MappedDocType, TypeMapping }
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.{ Future, Promise }
import scalaz.{ -\/, \/- }

object CouchContactPersonRepository {
  val contactPersonKind = "type/contactperson/v1"

  implicit val contactPersonReader: Reader[ContactPerson] = new PlayJsonReaderUpickleCompat[ContactPerson]
  implicit val contactPersonWriter: Writer[ContactPerson] = new PlayJsonWriterUpickleCompat[ContactPerson]
}

class CouchContactPersonRepository @Inject() (couchDatabase: CouchDatabase) extends ContactPersonRepository with StrictLogging {
  import CouchContactPersonRepository._

  private val db = couchDatabase.getDb("contactpeople", TypeMapping(classOf[ContactPerson] -> CouchContactPersonRepository.contactPersonKind))

  override def findById(id: Id): Future[Option[EntityWithId[Id, ContactPerson]]] = {
    val p: Promise[Option[EntityWithId[Id, ContactPerson]]] = Promise()

    db.docs.get[ContactPerson](id).unsafePerformAsync {
      case \/-(res) => p.success(Some(EntityWithId(res._id, res.doc)))
      case -\/(e) => e match {
        case _: CouchException[_] => p.success(None)
        case _ => p.failure(e)
      }
    }

    p.future
  }

  override def findAll: Future[Seq[EntityWithId[Id, ContactPerson]]] = {
    val p: Promise[Seq[EntityWithId[Id, ContactPerson]]] = Promise()

    db.docs.getMany.byType[String]("all", "contactpeople", MappedDocType(contactPersonKind)).includeDocs.build.query.unsafePerformAsync {
      case \/-(res) => p.success(res.getDocs.map(doc => EntityWithId(doc._id, doc.doc)))
      case -\/(e) => p.failure(e)
    }

    p.future.map(_.sortBy(x => (x.entity.lastName, x.entity.firstName)))
  }

  override def insert(id: Id, person: ContactPerson): Future[Id] = db.docs.create[ContactPerson](person, id).toFuture.map(_.id)

  override def count: Future[Int] = findAll.map(_.length)

  override def update(id: Id, person: ContactPerson): Future[Unit] = {
    for {
      currentRev <- db.docs.get[ContactPerson](id).toFuture.map(_._rev)
      res <- db.docs.update[ContactPerson](CouchDoc(person, contactPersonKind, _id = id, _rev = currentRev)).toFuture
    } yield { () }
  }

  override def delete(id: Id): Future[Unit] = ???
}
