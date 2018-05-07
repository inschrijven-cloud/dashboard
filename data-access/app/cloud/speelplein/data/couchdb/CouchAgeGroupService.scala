package cloud.speelplein.data.couchdb

import cloud.speelplein.data.AgeGroupService
import cloud.speelplein.models.{AgeGroupConfig, Child, Tenant}
import com.ibm.couchdb.{CouchDoc, TypeMapping}
import javax.inject.Inject
import cloud.speelplein.data.util.ScalazExtensions.PimpedScalazTask

import scala.concurrent.{ExecutionContext, Future}

object CouchAgeGroupService {
  val ageGroupConfigKind = "type/ageGroup/v1"
  val dbId = "age-group-config"
}

class CouchAgeGroupService @Inject()(couchDatabase: CouchDatabase,
                                     implicit val ec: ExecutionContext)
    extends AgeGroupService {
  private def db(implicit tenant: Tenant) =
    couchDatabase.getDb(
      TypeMapping(
        classOf[AgeGroupConfig] -> CouchAgeGroupService.ageGroupConfigKind
      ),
      tenant)

  override def get()(implicit tenant: Tenant): Future[AgeGroupConfig] =
    db.docs
      .get[AgeGroupConfig](CouchAgeGroupService.dbId)
      .map(_.doc)
      .toFuture
      .recover { case _ => AgeGroupConfig(Seq.empty) }

  override def updateOrCreate(newConfig: AgeGroupConfig)(
      implicit tenant: Tenant): Future[Unit] =
    db.docs
      .get[AgeGroupConfig](CouchAgeGroupService.dbId)
      .map(_._rev)
      .toFuture
      .map(Some(_))
      .recover { case _ => None }
      .map { maybeRev =>
        maybeRev.fold {
          db.docs
            .create(newConfig, CouchAgeGroupService.dbId)
            .toFuture
            .map(_ => ())
        } { rev =>
          db.docs
            .update(
              CouchDoc[AgeGroupConfig](newConfig,
                                       CouchAgeGroupService.ageGroupConfigKind,
                                       CouchAgeGroupService.dbId,
                                       rev))
            .toFuture
            .map(_ => ())
        }
      }

}
