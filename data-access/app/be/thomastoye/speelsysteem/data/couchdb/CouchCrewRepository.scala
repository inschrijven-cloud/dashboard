package be.thomastoye.speelsysteem.data.couchdb

import javax.inject.Inject

import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.data.{ CrewRepository, PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat }
import be.thomastoye.speelsysteem.data.util.ScalazExtensions.PimpedScalazTask
import upickle.default.{ Reader, Writer }
import be.thomastoye.speelsysteem.models._
import be.thomastoye.speelsysteem.models.Crew.Id
import be.thomastoye.speelsysteem.models.JsonFormats._
import com.ibm.couchdb.{ CouchDoc, MappedDocType, TypeMapping }
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Promise }
import scalaz.{ -\/, \/- }

object CouchCrewRepository {
  val crewKind = "type/crew/v1"

  implicit val crewReader: Reader[Crew] = new PlayJsonReaderUpickleCompat[Crew]
  implicit val crewWriter: Writer[Crew] = new PlayJsonWriterUpickleCompat[Crew]
}

class CouchCrewRepository @Inject() (couchDatabase: CouchDatabase) extends CrewRepository with StrictLogging {
  import CouchCrewRepository._

  private def db(tenant: Tenant) = couchDatabase.getDb(TypeMapping(classOf[Crew] -> CouchCrewRepository.crewKind), tenant)

  override def findById(id: Id)(implicit tenant: Tenant): Future[Option[EntityWithId[Id, Crew]]] = findAll.map(_.find(_.id == id))

  override def findAll(implicit tenant: Tenant): Future[Seq[EntityWithId[Id, Crew]]] = {
    db(tenant).docs.getMany
      .byType[String]("all-contactperson", "default", MappedDocType(crewKind))
      .includeDocs[Crew].build.query.toFuture
      .map(res => res.getDocs.map(doc => EntityWithId(doc._id, doc.doc)))
  }

  override def insert(id: Crew.Id, crewMember: Crew)(implicit tenant: Tenant): Future[Crew.Id] = db(tenant).docs.create(crewMember, id).toFuture.map(_.id)

  override def count(implicit tenant: Tenant): Future[Int] = findAll.map(_.length)

  override def update(id: Id, crewMember: Crew)(implicit tenant: Tenant): Future[Unit] = {
    for {
      currentRev <- db(tenant).docs.get[Crew](id).toFuture.map(_._rev)
      res <- db(tenant).docs.update[Crew](CouchDoc(crewMember, crewKind, _id = id, _rev = currentRev)).toFuture
    } yield { () }
  }

  override def delete(id: Id)(implicit tenant: Tenant) = ???
}
