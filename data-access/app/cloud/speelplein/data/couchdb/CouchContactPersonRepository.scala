package cloud.speelplein.data.couchdb

import javax.inject.Inject

import cloud.speelplein.EntityWithId
import cloud.speelplein.data.{ ContactPersonRepository, PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat }
import cloud.speelplein.data.util.ScalazExtensions.PimpedScalazTask
import cloud.speelplein.models.Tenant
import upickle.default.{ Reader, Writer }
import cloud.speelplein.models.JsonFormats._
import cloud.speelplein.models.ContactPerson.Id
import cloud.speelplein.data.{ ContactPersonRepository, PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat }
import cloud.speelplein.models.{ ContactPerson, Tenant }
import com.ibm.couchdb.{ CouchDoc, CouchException, MappedDocType, TypeMapping }
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Promise }
import scalaz.{ -\/, \/- }

object CouchContactPersonRepository {
  val contactPersonKind = "type/contactperson/v1"

  implicit val contactPersonReader: Reader[ContactPerson] = new PlayJsonReaderUpickleCompat[ContactPerson]
  implicit val contactPersonWriter: Writer[ContactPerson] = new PlayJsonWriterUpickleCompat[ContactPerson]
}

class CouchContactPersonRepository @Inject() (couchDatabase: CouchDatabase) extends ContactPersonRepository with StrictLogging {
  import CouchContactPersonRepository._

  private def db(tenant: Tenant) = couchDatabase.getDb(TypeMapping(classOf[ContactPerson] -> CouchContactPersonRepository.contactPersonKind), tenant)

  override def findById(id: Id)(implicit tenant: Tenant): Future[Option[EntityWithId[Id, ContactPerson]]] = {
    val p: Promise[Option[EntityWithId[Id, ContactPerson]]] = Promise()

    db(tenant).docs.get[ContactPerson](id).unsafePerformAsync {
      case \/-(res) => p.success(Some(EntityWithId(res._id, res.doc)))
      case -\/(e) => e match {
        case _: CouchException[_] => p.success(None)
        case _ => p.failure(e)
      }
    }

    p.future
  }

  override def findAll(implicit tenant: Tenant): Future[Seq[EntityWithId[Id, ContactPerson]]] = {
    val p: Promise[Seq[EntityWithId[Id, ContactPerson]]] = Promise()

    db(tenant).docs.getMany.byType[String]("all-contactperson", "default", MappedDocType(contactPersonKind)).includeDocs.build.query.unsafePerformAsync {
      case \/-(res) => p.success(res.getDocs.map(doc => EntityWithId(doc._id, doc.doc)))
      case -\/(e) => p.failure(e)
    }

    p.future.map(_.sortBy(x => (x.entity.lastName, x.entity.firstName)))
  }

  override def insert(id: Id, person: ContactPerson)(implicit tenant: Tenant): Future[Id] = db(tenant).docs.create[ContactPerson](person, id).toFuture.map(_.id)

  override def count(implicit tenant: Tenant): Future[Int] = findAll.map(_.length)

  override def update(id: Id, person: ContactPerson)(implicit tenant: Tenant): Future[Unit] = {
    for {
      currentRev <- db(tenant).docs.get[ContactPerson](id).toFuture.map(_._rev)
      res <- db(tenant).docs.update[ContactPerson](CouchDoc(person, contactPersonKind, _id = id, _rev = currentRev)).toFuture
    } yield { () }
  }

  override def delete(id: Id)(implicit tenant: Tenant): Future[Unit] = ???
}
