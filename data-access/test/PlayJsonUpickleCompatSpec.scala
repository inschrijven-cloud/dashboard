import be.thomastoye.speelsysteem.data.{PlayJsonReaderUpickleCompat, PlayJsonWriterUpickleCompat}
import org.scalatest._
import play.api.libs.json._
import play.api.libs.json.Json._
import upickle.Js

class PlayJsonUpickleCompatSpec extends WordSpec with Matchers {
  case class Test(arr: Seq[String], num: Int, str: String)

  "Play JSON uPickle converter" should {
    "work for writes" in {
      implicit val writes = Json.writes[Test]
      val compat = new PlayJsonWriterUpickleCompat[Test]

      compat.write0(Test(Seq("test1", "test2"), 3, "test4")) should be(
        Js.Obj( "arr" -> Js.Arr(Js.Str("test1"), Js.Str("test2")),  "num" -> Js.Num(3), "str" -> Js.Str("test4"))
      )
    }

    "work for reads" in {
      implicit val reads = Json.reads[Test]
      val compat = new PlayJsonReaderUpickleCompat[Test]

      val obj = Js.Obj( "arr" -> Js.Arr(Js.Str("test1"), Js.Str("test2")),  "num" -> Js.Num(3), "str" -> Js.Str("test4"))

      compat.read0(obj) should be(Test(Seq("test1", "test2"), 3, "test4"))
    }
  }
}
