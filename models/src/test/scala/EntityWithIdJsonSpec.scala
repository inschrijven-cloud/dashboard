import cloud.speelplein.EntityWithId
import org.scalatest._
import play.api.libs.json.{ JsError, JsSuccess, Json }
import cloud.speelplein.models.JsonFormats.entityWithIdReads
import cloud.speelplein.models.JsonFormats.entityWithIdWrites

case class Example(field1: Int, field2: String) // has to be at file level, see http://stackoverflow.com/questions/36926225/scala-play-json-no-unapply-or-unapplyseq-function-found

class EntityWithIdJsonSpec extends WordSpec with Matchers {
  "EntityWithId JSON reads" should {
    "work for a correct JSON model with a string id" in {
      implicit val jsonFormat = Json.format[Example]
      val expected = JsSuccess(EntityWithId[String, Example]("theId", Example(55, "value")))

      Json.parse(
        """
          |{
          |  "field1": 55,
          |  "field2": "value",
          |  "id": "theId"
          |}
        """.stripMargin
      ).validate[EntityWithId[String, Example]](entityWithIdReads) should be(expected)
    }

    "fail for a JSON model without an id" in {
      implicit val jsonFormat = Json.format[Example]

      Json.parse(
        """
          |{
          |  "field1": 55,
          |  "field2": "value"
          |}
        """.stripMargin
      ).validate[EntityWithId[String, Example]](entityWithIdReads) shouldBe a[JsError]
    }
  }

  "EntityWithId JSON writes" should {
    "work for a correct JSON model with a string id" in {
      implicit val jsonFormat = Json.format[Example]
      val input = EntityWithId[String, Example]("theId", Example(55, "value"))

      Json.toJson(input) should be(Json.parse(
        """
          |{
          |  "field1": 55,
          |  "field2": "value",
          |  "id": "theId"
          |}
        """.stripMargin
      ))
    }
  }
}
