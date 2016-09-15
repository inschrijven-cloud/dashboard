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
          |   "attendances" : [
          |      {
          |         "shifts" : [
          |            "ea52cb07-1a90-4438-80cc-0c13a3813f7d",
          |            "9638293d-7fd7-4408-8654-b2da1632d0a2",
          |            "a52966ab-5122-40ec-bdce-53a678d5b3a3"
          |         ],
          |         "day" : "2015-08-24"
          |      },
          |      {
          |         "shifts" : [
          |            "4b6cec72-1f7c-4638-8cd6-3d952e887567"
          |         ],
          |         "day" : "2015-08-25"
          |      },
          |      {
          |         "shifts" : [
          |            "c762ddd0-6f02-4770-a4f4-65e6440b77d4"
          |         ],
          |         "day" : "2015-08-27"
          |      },
          |      {
          |         "day" : "2015-08-28",
          |         "shifts" : [
          |            "e26dd879-66de-47da-8377-2a47a1a716f6"
          |         ]
          |      }
          |   ],
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
        Some(DayDate(22, 2, 2002)),
        Seq(
          Attendance("2015-08-24", Seq("ea52cb07-1a90-4438-80cc-0c13a3813f7d", "9638293d-7fd7-4408-8654-b2da1632d0a2", "a52966ab-5122-40ec-bdce-53a678d5b3a3")),
          Attendance("2015-08-25", Seq("4b6cec72-1f7c-4638-8cd6-3d952e887567")),
          Attendance("2015-08-27", Seq("c762ddd0-6f02-4770-a4f4-65e6440b77d4")),
          Attendance("2015-08-28", Seq("e26dd879-66de-47da-8377-2a47a1a716f6"))
        )
      )
    }
  }
}
