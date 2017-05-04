import be.thomastoye.speelsysteem.EntityWithId
import be.thomastoye.speelsysteem.dashboard.controllers.api.ContactPersonApiController
import be.thomastoye.speelsysteem.data.ContactPersonRepository
import be.thomastoye.speelsysteem.models.{ Address, ContactPerson, PhoneContact }
import be.thomastoye.speelsysteem.models.JsonFormats._
import org.scalatestplus.play.PlaySpec
import org.scalamock.scalatest.MockFactory
import play.api.mvc.{ AnyContentAsJson, Request, Results }
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future

class ContactPersonApiControllerSpec extends PlaySpec with Results with MockFactory {
  "ContactPersonApiController#getById" should {
    val cp = ContactPerson("first", "last", Address.empty, Seq(PhoneContact("555 555 555")))

    "return NotFound if the contact person is not in the database" in {
      val contactPersonRepository = mock[ContactPersonRepository]
      (contactPersonRepository.findById _).expects("existing-id").returning(Future.successful(Some(EntityWithId("existing-id", cp)))).never()
      (contactPersonRepository.findById _).expects(*).returning(Future.successful(None)).once()

      val controller = new ContactPersonApiController(contactPersonRepository)
      status(controller.getById("non-existant-id").apply(FakeRequest())) mustBe NOT_FOUND
    }

    "return contact person as JSON if the contact person is in the database" in {
      val contactPersonRepository = mock[ContactPersonRepository]
      (contactPersonRepository.findById _).expects("existing-id").returning(Future.successful(Some(EntityWithId("existing-id", cp)))).once()
      (contactPersonRepository.findById _).expects(*).returning(Future.successful(None)).never()

      val controller = new ContactPersonApiController(contactPersonRepository)
      val res = controller.getById("existing-id").apply(FakeRequest())
      status(res) mustBe OK
      contentAsJson(res) mustBe Json.toJson(cp)
    }
  }

  "ContactPersonApiController#all" should {
    val person1 = ContactPerson("first", "last", Address.empty, Seq(PhoneContact("555 555 555")))
    val person2 = ContactPerson("voor", "achter", Address.empty, Seq(PhoneContact("666 666 666")))

    "return list of contact people in the database" in {
      val contactPersonRepository = mock[ContactPersonRepository]
      (contactPersonRepository.findAll _).expects().returning(Future.successful(Seq(EntityWithId("first-id", person1), EntityWithId("second-id", person2)))).once()

      val controller = new ContactPersonApiController(contactPersonRepository)
      contentAsJson(controller.all.apply(FakeRequest())) mustBe Json.arr(Json.toJson(EntityWithId("first-id", person1)), Json.toJson(EntityWithId("second-id", person2)))
    }

    "return empty JSON list if there are no contact people in the database" in {
      val contactPersonRepository = mock[ContactPersonRepository]

      (contactPersonRepository.findAll _).expects().returning(Future.successful(Seq.empty))

      val controller = new ContactPersonApiController(contactPersonRepository)
      contentAsJson(controller.all.apply(FakeRequest())) mustBe Json.arr()
    }
  }

  "ContactPersonApiController#delete" should {
    "delete a contact person by id" in {
      val contactPersonRepository = mock[ContactPersonRepository]
      (contactPersonRepository.delete _).expects("the-id-to-delete").returning(Future.successful(())).once()

      val controller = new ContactPersonApiController(contactPersonRepository)

      status(controller.delete("the-id-to-delete").apply(FakeRequest())) mustBe OK
    }
  }

  "ContactPersonApiController#update" should {
    "update a contact person" in {
      val contactPersonRepository = mock[ContactPersonRepository]
      val cp = ContactPerson("first", "last", Address.empty, Seq(PhoneContact("555 555 555")))

      (contactPersonRepository.update _).expects("the-id-to-update", cp).returning(Future.successful(())).once()
      val controller = new ContactPersonApiController(contactPersonRepository)

      val body: FakeRequest[ContactPerson] = FakeRequest().withBody[ContactPerson](cp)
      status(controller.update("the-id-to-update").apply(body)) mustBe OK
    }
  }
}
