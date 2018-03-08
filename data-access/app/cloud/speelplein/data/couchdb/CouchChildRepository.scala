package cloud.speelplein.data.couchdb

import javax.inject.Inject

import cloud.speelplein.EntityWithId
import cloud.speelplein.data.couchdb.CouchChildRepository._
import cloud.speelplein.data.util.ScalazExtensions.PimpedScalazTask
import cloud.speelplein.data.{
  ChildRepository,
  PlayJsonReaderUpickleCompat,
  PlayJsonWriterUpickleCompat
}
import cloud.speelplein.models.Child.Id
import cloud.speelplein.models.JsonFormats._
import cloud.speelplein.models._
import com.ibm.couchdb.{CouchDoc, CouchException, MappedDocType, TypeMapping}
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.{Json, OWrites}
import upickle.default.{Reader, Writer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scalaz.{-\/, \/-}

object CouchChildRepository {
  val childKind = "type/child/v1"
  val mergedChildKind = "type/mergedchild/v1"

  implicit val childReader: Reader[Child] =
    new PlayJsonReaderUpickleCompat[Child]
  implicit val childWriter: Writer[Child] =
    new PlayJsonWriterUpickleCompat[Child]

  case class MergedChild(firstName: String,
                         lastName: String,
                         legacyAddress: Address,
                         legacyContact: ContactInfo,
                         gender: Option[String],
                         contactPeople: Seq[ContactPersonRelationship],
                         birthDate: Option[DayDate],
                         medicalInformation: MedicalInformation,
                         remarks: Option[String],
                         mergedWith: Child.Id)

  implicit val mergedChildWrites: OWrites[MergedChild] =
    Json.writes[MergedChild]
  implicit val mergedChildWriter: Writer[MergedChild] =
    new PlayJsonWriterUpickleCompat[MergedChild]

}

class CouchChildRepository @Inject()(couchDatabase: CouchDatabase)
    extends ChildRepository
    with StrictLogging {

  private def db(implicit tenant: Tenant) =
    couchDatabase.getDb(
      TypeMapping(
        classOf[Child] -> CouchChildRepository.childKind,
        classOf[MergedChild] -> CouchChildRepository.mergedChildKind
      ),
      tenant)

  override def findById(id: Id)(
      implicit tenant: Tenant): Future[Option[EntityWithId[Id, Child]]] = {
    val p: Promise[Option[EntityWithId[Id, Child]]] = Promise()

    db.docs.get[Child](id).unsafePerformAsync {
      case \/-(res) => p.success(Some(EntityWithId(res._id, res.doc)))
      case -\/(e) =>
        e match {
          case _: CouchException[_] => p.success(None)
          case _                    => p.failure(e)
        }
    }

    p.future
  }

  override def findAll(
      implicit tenant: Tenant): Future[Seq[EntityWithId[Id, Child]]] = {
    val p: Promise[Seq[EntityWithId[Id, Child]]] = Promise()

    db.docs.getMany
      .byType[String]("all-children", "default", MappedDocType(childKind))
      .includeDocs
      .build
      .query
      .unsafePerformAsync {
        case \/-(res) =>
          p.success(res.getDocs.map(doc => EntityWithId(doc._id, doc.doc)))
        case -\/(e) => p.failure(e)
      }

    p.future.map(_.sortBy(x => (x.entity.lastName, x.entity.firstName)))
  }

  override def insert(id: Id, child: Child)(
      implicit tenant: Tenant): Future[Id] =
    db.docs.create[Child](child, id).toFuture.map(_.id)

  override def count(implicit tenant: Tenant): Future[Int] =
    db(tenant).query
      .view[String, Int]("default", "all-children-count")
      .get
      .group(true)
      .reduce[Int]
      .build
      .query
      .toFuture
      .map(_.rows.head.value)

  override def update(id: Id, child: Child)(
      implicit tenant: Tenant): Future[Unit] = {
    for {
      currentRev <- db.docs.get[Child](id).toFuture.map(_._rev)
      res <- db.docs
        .update[Child](CouchDoc(child, childKind, _id = id, _rev = currentRev))
        .toFuture
    } yield { () }
  }

  override def delete(id: Id)(implicit tenant: Tenant): Future[Unit] =
    db.docs.get[Child](id).toFuture flatMap { doc =>
      db.docs.delete[Child](doc).toFuture.map(_ => ())
    }

  override def setMerged(retiredId: Id, absorpedIntoId: Id)(
      implicit tenant: Tenant): Future[Unit] = {
    db.docs.get[Child](retiredId).toFuture flatMap { childDoc =>
      val child = childDoc.doc
      val mergedChild = MergedChild(
        child.firstName,
        child.lastName,
        child.legacyAddress,
        child.legacyContact,
        child.gender,
        child.contactPeople,
        child.birthDate,
        child.medicalInformation,
        child.remarks,
        absorpedIntoId
      )

      db.docs
        .update[MergedChild](
          CouchDoc(mergedChild, mergedChildKind, childDoc._id, childDoc._rev))
        .toFuture
        .map(_ => ())
    }
  }
}
