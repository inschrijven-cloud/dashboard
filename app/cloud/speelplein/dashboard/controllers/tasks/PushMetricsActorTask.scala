package cloud.speelplein.dashboard.controllers.tasks

import javax.inject.{Inject, Named}
import akka.actor.{ActorRef, ActorSystem}
import play.api.Configuration

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class PushMetricsActorTask @Inject()(
    actorSystem: ActorSystem,
    @Named("metrics-actor") metricsActor: ActorRef,
    config: Configuration)(implicit executionContext: ExecutionContext) {

  private val cancellable = actorSystem.scheduler.schedule(
    initialDelay = 0.microseconds,
    interval = config
      .getOptional[FiniteDuration]("metrics.interval")
      .getOrElse(10.minutes),
    receiver = metricsActor,
    message = "tick"
  )

}
