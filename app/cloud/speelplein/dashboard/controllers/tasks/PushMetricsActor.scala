package cloud.speelplein.dashboard.controllers.tasks

import akka.actor._
import cloud.speelplein.data.metrics.MetricsService
import cloud.speelplein.data._
import javax.inject._
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext.Implicits.global

class PushMetricsActor @Inject()(
    metricsService: MetricsService,
    tenantsService: TenantsService,
    childRepository: ChildRepository,
    crewRepository: CrewRepository,
    contactPersonRepository: ContactPersonRepository,
    dayService: DayService,
    childAttendancesService: ChildAttendancesService,
    crewAttendancesService: CrewAttendancesService,
    configuration: Configuration)
    extends Actor {

  private val metricsEnabled = configuration.get[Boolean]("metrics.enabled")

  @SuppressWarnings(
    Array("org.wartremover.warts.Any",
          "org.wartremover.warts.NonUnitStatements"))
  def receive = {
    case "tick" =>
      if (metricsEnabled) {
        Logger.info("Sending metrics...")

        tenantsService.all.map { tenants =>
          tenants.map(implicit tenant => {
            // count children
            childRepository.count
              .map(count => metricsService.storeChildCount(tenant, count))

            // count crew
            crewRepository.count
              .map(count => metricsService.storeCrewCount(tenant, count))

            // count contact people
            contactPersonRepository.count.map(count =>
              metricsService.storeContactPersonCount(tenant, count))

            // count days
            dayService.count.map(count =>
              metricsService.storeDayCount(tenant, count))

            // count child attendances
            childAttendancesService.count.map(count =>
              metricsService.storeChildAttendanceCount(tenant, count))

            // count crew attendances
            crewAttendancesService.count.map(count =>
              metricsService.storeCrewAttendanceCount(tenant, count))
          })
        }

      }
  }
}
