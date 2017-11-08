import cloud.speelplein.models._
import cloud.speelplein.models.{Address, ContactInfo, PhoneContact}
import cloud.speelplein.models.JsonFormats.childFormat
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
          |   "legacyAddress" : {
          |      "zipCode" : 6666,
          |      "city" : "Some-City",
          |      "street" : "Street",
          |      "number" : "55X"
          |   },
          |   "legacyContact" : {
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
          |   "gender": "male",
          |   "lastName" : "Doe",
          |   "firstName" : "John",
          |   "birthDate": {
          |     "day": 22,
          |     "month": 2,
          |     "year": 2002
          |   },
          |   "medicalInformation": {
          |     "familyDoctor": "Dr. XYZZ",
          |     "allergies": { "allergies": ["dust", "paint" ], "extraInformation": "Sometimes water too" },
          |     "conditions": { "conditions": ["ADHD"], "extraInformation": "Extra information on conditions" },
          |     "otherShouldBeAwareOf": "Stuff you should be aware of",
          |     "tetanusLastVaccinationYear": 2015,
          |     "otherRemarks": "Other remarks"
          |   },
          |   "contactPeople" : [
          |     { "contactPersonId": "contact-person-a", "relationship": "mother" },
          |     { "contactPersonId": "contact-person-b", "relationship": "father" }
          |   ],
          |   "remarks": "aoeu"
          |}
        """.stripMargin
      )

      val res = json.validate[Child]

      res.isSuccess mustBe true
      res.get mustBe Child(
        "John",
        "Doe",
        Address(Some("Street"), Some("55X"), Some(6666), Some("Some-City")),
        ContactInfo(
          Seq(
            PhoneContact(kind = Some("mobile"), phoneNumber = "0478 78 78 78"),
            PhoneContact(kind = Some("landline"),
                         comment =
                           Some("work phone, call this during business hours"),
                         phoneNumber = "055 55 55 55")
          ),
          Seq("john.smith@example.com", "test@example.com")
        ),
        Some("male"),
        Seq(ContactPersonRelationship("contact-person-a", "mother"),
            ContactPersonRelationship("contact-person-b", "father")),
        Some(DayDate(22, 2, 2002)),
        MedicalInformation(
          Some("Dr. XYZZ"),
          Some(Allergies(Seq("dust", "paint"), Some("Sometimes water too"))),
          Some(
            Conditions(Seq("ADHD"), Some("Extra information on conditions"))),
          Some("Stuff you should be aware of"),
          Some(2015),
          Some("Other remarks")
        ),
        Some("aoeu")
      )
    }

    "work for a serialized child with incomplete address" in {
      val json = Json.parse(
        """
          |{
          |   "legacyAddress" : {
          |      "zipCode" : 6666,
          |      "street" : "Street",
          |      "number" : "55X"
          |   },
          |   "legacyContact" : {
          |      "email" : [],
          |      "phone" : []
          |   },
          |   "lastName" : "Doe",
          |   "firstName" : "John",
          |   "medicalInformation": {},
          |   "contactPeople" : []
          |}
        """.stripMargin
      )

      val res = json.validate[Child]

      res.isSuccess mustBe true
      res.get mustBe Child(
        "John",
        "Doe",
        Address(Some("Street"), Some("55X"), Some(6666), None),
        ContactInfo(Seq.empty, Seq.empty),
        None,
        Seq.empty,
        None,
        MedicalInformation.empty,
        None)
    }
  }
}
