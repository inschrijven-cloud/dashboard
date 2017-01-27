import be.thomastoye.speelsysteem.models._

import be.thomastoye.speelsysteem.models.JsonFormats.childFormat
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class JsonReadChildSpec extends PlaySpec {
  "Reading child JSON" should {
    "work for a serialized complete child" in {
      val json = Json.parse(
        """
          |{
          |   "address" : {
          |      "zipCode" : 6666,
          |      "city" : "Some-City",
          |      "street" : "Street",
          |      "number" : "55X"
          |   },
          |   "contact" : {
          |      "email" : [
          |        "john.smith@example.com",
          |        "test@example.com"
          |      ],
          |      "phone" : [
          |         {
          |            "kind" : "mobile",
          |            "phoneNumber" : "0478 78 78 78"
          |         },
          |         {
          |            "kind" : "landline",
          |            "phoneNumber" : "055 55 55 55",
          |            "comment": "work phone, call this during business hours"
          |         }
          |      ]
          |   },
          |   "lastName" : "Doe",
          |   "firstName" : "John",
          |   "birthDate": {
          |     "day": 22,
          |     "month": 2,
          |     "year": 2002
          |   }
          |}
        """.stripMargin)

      val res = json.validate[Child]

      res.isSuccess mustBe true
      res.get mustBe Child("John", "Doe",
        Address(Some("Street"), Some("55X"), Some(6666), Some("Some-City")),
        ContactInfo(Seq(
          PhoneContact(kind = Some("mobile"), phoneNumber = "0478 78 78 78"),
          PhoneContact(kind = Some("landline"), comment = Some("work phone, call this during business hours"), phoneNumber = "055 55 55 55")),
          Seq("john.smith@example.com", "test@example.com")
        ),
        Some(DayDate(22, 2, 2002))
      )
    }
  }
}
