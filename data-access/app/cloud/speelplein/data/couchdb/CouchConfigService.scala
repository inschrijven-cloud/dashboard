package cloud.speelplein.data.couchdb

import javax.inject.Inject

import cloud.speelplein.data.{ ConfigService, PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat }
import com.ibm.couchdb.{ CouchDoc, CouchException, TypeMapping }
import cloud.speelplein.data.util.ScalazExtensions._
import upickle.default.{ Reader, Writer }
import CouchConfigService._
import cloud.speelplein.models.JsonFormats.configFormat
import cloud.speelplein.models.ConfigWrapper
import cloud.speelplein.data.{ ConfigService, PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat }

import scala.concurrent.{ ExecutionContext, Future }

object CouchConfigService {
  val kind = "type/domainConfig/v1"
  val configDbName = "ic-config"

  implicit val configReader: Reader[ConfigWrapper] = new PlayJsonReaderUpickleCompat[ConfigWrapper]
  implicit val configWriter: Writer[ConfigWrapper] = new PlayJsonWriterUpickleCompat[ConfigWrapper]
}

class CouchConfigService @Inject() (couchDatabase: CouchDatabase)(implicit executionContext: ExecutionContext) extends ConfigService {

  private val db = couchDatabase.getDb(TypeMapping(
    classOf[ConfigWrapper] -> CouchChildRepository.childKind
  ), configDbName)

  override def getConfig(domain: String): Future[Option[ConfigWrapper]] = db.docs.get[ConfigWrapper](id = domain).toFuture.map(d => Some(d.doc)).recoverWith {
    case e: CouchException[_] => Future.successful(None)
  }

  override def insert(id: String, config: ConfigWrapper): Future[Unit] = db.docs.create[ConfigWrapper](config, id).toFuture.map(_ => ())

  override def update(id: String, config: ConfigWrapper): Future[Unit] = for {
    currentRev <- db.docs.get[ConfigWrapper](id).toFuture.map(_._rev)
    res <- db.docs.update[ConfigWrapper](CouchDoc(config, kind, _id = id, _rev = currentRev)).toFuture
  } yield { () }
}
