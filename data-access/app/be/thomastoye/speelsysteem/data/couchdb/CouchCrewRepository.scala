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

  private val db = couchDatabase.getDb("crew", TypeMapping(classOf[Crew] -> CouchCrewRepository.crewKind))

  override def findById(id: Id): Future[Option[EntityWithId[Id, Crew]]] = findAll.map(_.find(_.id == id))

  override def findAll: Future[Seq[EntityWithId[Id, Crew]]] = {
    val p: Promise[Seq[EntityWithId[Id, Crew]]] = Promise()

    db.docs.getMany.byTypeUsingTemporaryView(MappedDocType(crewKind)).includeDocs[Crew].build.query.unsafePerformAsync {
      case \/-(res) => p.success(res.getDocs.map(doc => EntityWithId(doc._id, doc.doc)))
      case -\/(e) => p.failure(e)
    }

    p.future.map(_.sortBy(x => (x.entity.lastName, x.entity.firstName)))
  }

  override def insert(crewMember: Crew): Future[Unit] = db.docs.create(crewMember).toFuture.map(_ => ())

  override def count: Future[Int] = findAll.map(_.length)

  override def update(id: Id, crewMember: Crew): Future[Unit] = {
    for {
      currentRev <- db.docs.get[Crew](id).toFuture.map(_._rev)
      res <- db.docs.update[Crew](CouchDoc(crewMember, crewKind, _id = id, _rev = currentRev)).toFuture
    } yield { () }
  }
}
