package cloud.speelplein.data.metrics

import java.time.Instant

import cloud.speelplein.models.Tenant
import io.waylay.influxdb.Influx.{IInteger, IPoint}
import javax.inject.Inject
import play.api.Logger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MetricsService {
  def storeChildCount(tenant: Tenant, count: Int): Future[Unit]
  def storeCrewCount(tenant: Tenant, count: Int): Future[Unit]
  def storeContactPersonCount(tenant: Tenant, count: Int): Future[Unit]
  def storeDayCount(tenant: Tenant, count: Int): Future[Unit]
  def storeChildAttendanceCount(tenant: Tenant, count: Int): Future[Unit]
  def storeCrewAttendanceCount(tenant: Tenant, count: Int): Future[Unit]
}

class InfluxDBMetricsService @Inject()(influxProvider: InfluxDBProvider)
    extends MetricsService {
  private val client = influxProvider.client
  private val db = influxProvider.db

  override def storeChildCount(tenant: Tenant, count: Int): Future[Unit] =
    storeCount(tenant, "child-count", count)

  override def storeCrewCount(tenant: Tenant, count: Int): Future[Unit] =
    storeCount(tenant, "crew-count", count)

  override def storeContactPersonCount(tenant: Tenant,
                                       count: Int): Future[Unit] =
    storeCount(tenant, "contactperson-count", count)

  override def storeChildAttendanceCount(tenant: Tenant,
                                         count: Int): Future[Unit] =
    storeCount(tenant, "childattendance-count", count)

  override def storeCrewAttendanceCount(tenant: Tenant,
                                        count: Int): Future[Unit] =
    storeCount(tenant, "crewattendance-count", count)

  override def storeDayCount(tenant: Tenant, count: Int): Future[Unit] =
    storeCount(tenant, "day-count", count)

  private def storeCount(tenant: Tenant,
                         name: String,
                         value: Int): Future[Unit] = {
    val point = IPoint(name,
                       Seq("tenant" -> tenant.name),
                       Seq("value" -> IInteger(value)),
                       Instant.now)

    client.storeAndMakeDbIfNeeded(db, Seq(point)) recover {
      case e: Throwable =>
        Logger.error("Error while storing metrics: " + e.toString)
    }
  }
}
