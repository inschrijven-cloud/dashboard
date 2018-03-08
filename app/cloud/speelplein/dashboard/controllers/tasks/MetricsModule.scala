package cloud.speelplein.dashboard.controllers.tasks

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class MetricsModule extends AbstractModule with AkkaGuiceSupport {

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def configure = {
    bind(classOf[PushMetricsActorTask]).asEagerSingleton()
    bindActor[PushMetricsActor]("metrics-actor")
  }
}
