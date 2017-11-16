package cloud.speelplein.data.couchdb

import javax.inject.Inject

import com.ibm.couchdb.{
  CouchDesign,
  CouchDoc,
  CouchException,
  CouchView,
  MappedDocType,
  TypeMapping
}
import cloud.speelplein.data.util.ScalazExtensions._
import upickle.default.{Reader, Writer}
import CouchConfigService._
import cloud.speelplein.EntityWithId
import cloud.speelplein.models.JsonFormats.configFormat
import cloud.speelplein.models.ConfigWrapper
import cloud.speelplein.data.{
  ConfigService,
  PlayJsonReaderUpickleCompat,
  PlayJsonWriterUpickleCompat
}
import com.ibm.couchdb.Res.DocOk
import play.api.Logger
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.{ExecutionContext, Future}

object CouchConfigService {
  val kind = "type/domainConfig/v1"
  val configDbName = "ic-config"

  implicit val configReader: Reader[ConfigWrapper] =
    new PlayJsonReaderUpickleCompat[ConfigWrapper]
  implicit val configWriter: Writer[ConfigWrapper] =
    new PlayJsonWriterUpickleCompat[ConfigWrapper]
}

class CouchConfigService @Inject()(
    couchDatabase: CouchDatabase,
    couchdbConfig: CouchDbConfig)(implicit executionContext: ExecutionContext)
    extends ConfigService {

  private val db = couchDatabase.getDb(TypeMapping(
                                         classOf[ConfigWrapper] -> kind
                                       ),
                                       configDbName)

  override def getConfig(domain: String): Future[Option[ConfigWrapper]] =
    db.docs
      .get[ConfigWrapper](id = domain)
      .toFuture
      .map(d => Some(d.doc))
      .recoverWith {
        case e: CouchException[_] => Future.successful(None)
      }

  override def insert(id: String, config: ConfigWrapper): Future[Unit] =
    db.docs.create[ConfigWrapper](config, id).toFuture.map(_ => ())

  override def update(id: String, config: ConfigWrapper): Future[Unit] =
    for {
      currentRev <- db.docs.get[ConfigWrapper](id).toFuture.map(_._rev)
      res <- db.docs
        .update[ConfigWrapper](
          CouchDoc(config, kind, _id = id, _rev = currentRev))
        .toFuture
    } yield { () }

  override def getAllConfig: Future[Seq[EntityWithId[String, ConfigWrapper]]] =
    db.docs.getMany
      .byType[String]("all-config", "default", MappedDocType(kind))
      .includeDocs
      .build
      .query
      .toFuture
      .map(_.getDocs.map(entity => EntityWithId(entity._id, entity.doc)))

  override def insertDesignDocs: Future[JsObject] = {
    def viewAll(kind: String): CouchView = {
      CouchView(map = s"""
                         |function(doc) {
                         |  if(doc.kind === '$kind') {
                         |    emit([doc.kind, doc._id], doc._id);
                         |  }
                         |}
        """.stripMargin)
    }

    Logger.info(s"Initializing config database with design doc")

    val name = "default"
    val exists = designDocExists(configDbName, "default")

    exists.flatMap { rev =>
      createDesignDoc(configDbName,
                      CouchDesign(name,
                                  _rev = rev.getOrElse(""),
                                  views = Map("all-config" -> viewAll(kind))))
    } map { docok =>
      Json.obj("id" -> docok.id, "rev" -> docok.rev, "ok" -> docok.ok)
    }
  }

  private def designDocExists(dbName: String,
                              designName: String): Future[Option[String]] = {
    couchdbConfig.client
      .db(dbName, TypeMapping.empty)
      .design
      .get(designName)
      .toFuture
      .map(t => Some(t._rev))
      .recover {
        case e: CouchException[_] => None
      }
  }

  private def createDesignDoc(dbName: String,
                              couchDesign: CouchDesign): Future[DocOk] = {
    couchdbConfig.client
      .db(dbName, TypeMapping.empty)
      .design
      .create(couchDesign)
      .toFuture
  }
}
