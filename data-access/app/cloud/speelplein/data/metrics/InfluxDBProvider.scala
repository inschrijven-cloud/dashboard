package cloud.speelplein.data.metrics

import io.waylay.influxdb.InfluxDB
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global

class InfluxDBProvider @Inject()(configuration: Configuration, ws: WSClient) {
  private val host = configuration.get[String]("metrics.influxdb.host")
  private val port = configuration.get[Int]("metrics.influxdb.port")

  val db: String = configuration.get[String]("metrics.influxdb.db")
  val client: InfluxDB = new InfluxDB(ws, host, port)
}
