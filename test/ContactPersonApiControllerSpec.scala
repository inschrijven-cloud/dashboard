import cloud.speelplein.EntityWithId
import cloud.speelplein.models.{Address, ContactPerson, PhoneContact, Tenant}
import cloud.speelplein.models.JsonFormats._
import cloud.speelplein.dashboard.controllers.actions.TenantAction
import cloud.speelplein.dashboard.controllers.api.ContactPersonApiController
import cloud.speelplein.data.ContactPersonRepository
import helpers.StubJwtAuthorizationBuilder
import org.scalatestplus.play.PlaySpec
import org.scalamock.scalatest.MockFactory
import play.api.mvc.{AnyContentAsJson, BodyParsers, Request, Results}
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ContactPersonApiControllerSpec
    extends PlaySpec
    with Results
    with MockFactory {
  val tenantAction = new TenantAction(
    new BodyParsers.Default(stubControllerComponents().parsers))
  val fakeReq = FakeRequest("GET", "/blah?tenant=blah")
  val authBuilder = new StubJwtAuthorizationBuilder()

  "ContactPersonApiController#getById" should {
    val cp = ContactPerson("first",
                           "last",
                           Address.empty,
                           Seq(PhoneContact("555 555 555")))

    "return NotFound if the contact person is not in the database" in {
      val contactPersonRepository = mock[ContactPersonRepository]
      (contactPersonRepository
        .findById(_: String)(_: Tenant))
        .expects("existing-id", *)
        .returning(Future.successful(Some(EntityWithId("existing-id", cp))))
        .never()
      (contactPersonRepository
        .findById(_: String)(_: Tenant))
        .expects(*, *)
        .returning(Future.successful(None))
        .once()

      val controller = new ContactPersonApiController(contactPersonRepository,
                                                      tenantAction,
                                                      authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      status(controller.getById("non-existant-id").apply(fakeReq)) mustBe NOT_FOUND
    }

    "return contact person as JSON if the contact person is in the database" in {
      val contactPersonRepository = mock[ContactPersonRepository]
      (contactPersonRepository
        .findById(_: String)(_: Tenant))
        .expects("existing-id", *)
        .returning(Future.successful(Some(EntityWithId("existing-id", cp))))
        .once()
      (contactPersonRepository
        .findById(_: String)(_: Tenant))
        .expects(*, *)
        .returning(Future.successful(None))
        .never()

      val controller = new ContactPersonApiController(contactPersonRepository,
                                                      tenantAction,
                                                      authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      val res = controller.getById("existing-id").apply(fakeReq)
      status(res) mustBe OK
      contentAsJson(res) mustBe Json.toJson(cp)
    }
  }

  "ContactPersonApiController#all" should {
    val person1 = ContactPerson("first",
                                "last",
                                Address.empty,
                                Seq(PhoneContact("555 555 555")))
    val person2 = ContactPerson("voor",
                                "achter",
                                Address.empty,
                                Seq(PhoneContact("666 666 666")))

    "return list of contact people in the database" in {
      val contactPersonRepository = mock[ContactPersonRepository]
      (contactPersonRepository
        .findAll(_: Tenant))
        .expects(*)
        .returning(Future.successful(Seq(EntityWithId("first-id", person1),
                                         EntityWithId("second-id", person2))))
        .once()

      val controller = new ContactPersonApiController(contactPersonRepository,
                                                      tenantAction,
                                                      authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      contentAsJson(controller.all.apply(fakeReq)) mustBe Json.arr(
        Json.toJson(EntityWithId("first-id", person1)),
        Json.toJson(EntityWithId("second-id", person2)))
    }

    "return empty JSON list if there are no contact people in the database" in {
      val contactPersonRepository = mock[ContactPersonRepository]

      (contactPersonRepository
        .findAll(_: Tenant))
        .expects(*)
        .returning(Future.successful(Seq.empty))

      val controller = new ContactPersonApiController(contactPersonRepository,
                                                      tenantAction,
                                                      authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      contentAsJson(controller.all.apply(fakeReq)) mustBe Json.arr()
    }
  }

  "ContactPersonApiController#delete" should {
    "delete a contact person by id" in {
      val contactPersonRepository = mock[ContactPersonRepository]
      (contactPersonRepository
        .delete(_: String)(_: Tenant))
        .expects("the-id-to-delete", *)
        .returning(Future.successful(()))
        .once()

      val controller = new ContactPersonApiController(contactPersonRepository,
                                                      tenantAction,
                                                      authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      status(controller.delete("the-id-to-delete").apply(fakeReq)) mustBe OK
    }
  }

  "ContactPersonApiController#update" should {
    "update a contact person" in {
      val contactPersonRepository = mock[ContactPersonRepository]
      val cp = ContactPerson("first",
                             "last",
                             Address.empty,
                             Seq(PhoneContact("555 555 555")))

      (contactPersonRepository
        .update(_: String, _: ContactPerson)(_: Tenant))
        .expects("the-id-to-update", cp, *)
        .returning(Future.successful(()))
        .once()
      val controller = new ContactPersonApiController(contactPersonRepository,
                                                      tenantAction,
                                                      authBuilder)
      controller.setControllerComponents(stubControllerComponents())

      val body: FakeRequest[ContactPerson] =
        fakeReq.withMethod("POST").withBody[ContactPerson](cp)
      status(controller.update("the-id-to-update").apply(body)) mustBe OK
    }
  }
}
