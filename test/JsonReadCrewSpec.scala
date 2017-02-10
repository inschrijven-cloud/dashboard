import be.thomastoye.speelsysteem.models._

import be.thomastoye.speelsysteem.models.JsonFormats.crewFormat
import scala.concurrent.Future
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class JsonReadCrewSpec extends PlaySpec {
  "Reading crew JSON" should {
    "work for a serialized complete crew member" in {
      val json = Json.parse(
        """
          |{
          |  "firstName": "John",
          |  "lastName": "Doe",
          |  "contact": {
          |    "phone": [
          |      {
          |        "kind": "mobile",
          |        "phoneNumber": "0478 78 78 78"
          |      },
          |      {
          |        "kind": "landline",
          |        "phoneNumber": "055 55 55 55",
          |        "comment": "work phone, call this during business hours"
          |      }
          |    ],
          |    "email": [
          |      "john.smith@example.com",
          |      "test@example.com"
          |    ]
          |  },
          |  "address": {
          |    "number": "55X",
          |    "street": "Street",
          |    "zipCode": 6666,
          |    "city": "Some-City"
          |  },
          |  "birthDate": {
          |    "day": 22,
          |    "month": 2,
          |    "year": 2002
          |  },
          |  "bankAccount": "BE66 6666 6666 6666",
          |  "yearStarted": 2016
          |}
        """.stripMargin
      )

      val res = json.validate[Crew]

      res.isSuccess mustBe true
      res.get mustBe Crew(
        "John", "Doe",
        Address(Some("Street"), Some("55X"), Some(6666), Some("Some-City")),
        Some("BE66 6666 6666 6666"),
        ContactInfo(Seq(
          PhoneContact("0478 78 78 78", Some("mobile"), None),
          PhoneContact("055 55 55 55", Some("landline"), Some("work phone, call this during business hours"))
        ), Seq("john.smith@example.com", "test@example.com")),
        Some(2016),
        Some(DayDate(22, 2, 2002))
      )
    }
  }
}
