package cloud.speelplein.models

import cloud.speelplein.EntityWithId
import cloud.speelplein.models.Shift.ShiftKind
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

object JsonFormats {
  val emptyJsonObject = Json.obj()

  implicit val dayDateFormat = Json.format[DayDate]
  implicit val singleAttendanceFormat = Json.format[SingleAttendance]
  implicit val contactPersonRelationshipFormat =
    Json.format[ContactPersonRelationship]
  implicit val dayAttendancesFormat = Json.format[DayAttendance]
  implicit val relativeTimeFormat = Json.format[RelativeTime]
  implicit val addressFormat = Json.format[Address]
  implicit val phoneContactFormat = Json.format[PhoneContact]
  implicit val crewContactFormat = Json.format[ContactInfo]
  implicit val crewFormat = Json.format[Crew]
  implicit val allergiesFormat = Json.format[Allergies]
  implicit val conditionsFormat = Json.format[Conditions]
  implicit val childMedicalInformationFormat = Json.format[MedicalInformation]
  implicit val childFormat = Json.format[Child]
  implicit val contactPersonFormat = Json.format[ContactPerson]
  implicit val priceFormat = Json.format[Price]
  implicit val startAndEndTimeFormat = Json.format[StartAndEndTime]
  implicit val configFormat = Json.format[ConfigWrapper]
  implicit val dbNameWrites: Writes[DbName] = new Writes[DbName] {
    override def writes(o: DbName): JsValue = JsString(o.value)
  }
  implicit val tenantFormat = Json.format[Tenant]

  implicit val appMetadataTenant = Json.format[TenantMetadata]
  implicit val auth0AppMetadata = Json.format[Auth0AppMetadata]

  implicit val shiftKindFormat: Format[ShiftKind] = new Format[ShiftKind] {
    override def writes(o: ShiftKind): JsValue = JsString(o.mnemonic)

    override def reads(json: JsValue): JsResult[ShiftKind] =
      json.validate[String].map(ShiftKind.apply)
  }

  private val shiftWrites: Writes[Shift] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "price").write[Price] and
      (JsPath \ "childrenCanBePresent").write[Boolean] and
      (JsPath \ "crewCanBePresent").write[Boolean] and
      (JsPath \ "kind").write[ShiftKind] and
      (JsPath \ "location").writeNullable[String] and
      (JsPath \ "description").writeNullable[String] and
      (JsPath \ "startAndEnd").writeNullable[StartAndEndTime]
  )(unlift(Shift.unapply))

  private val shiftReads: Reads[Shift] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "price").read[Price] and
      (JsPath \ "childrenCanBePresent").read[Boolean] and
      (JsPath \ "crewCanBePresent").read[Boolean] and
      (JsPath \ "kind").read[ShiftKind] and
      (JsPath \ "location").readNullable[String] and
      (JsPath \ "description").readNullable[String] and
      (JsPath \ "startAndEnd").readNullable[StartAndEndTime]
  )(Shift.apply _)

  implicit val shiftFormat: Format[Shift] = Format(shiftReads, shiftWrites)

  implicit val dayFormat = Json.format[Day]

  implicit def entityWithIdWrites[ID, T](
      implicit idWrites: Writes[ID],
      entityWrites: Writes[T]): Writes[EntityWithId[ID, T]] = {
    new Writes[EntityWithId[ID, T]] {
      override def writes(o: EntityWithId[ID, T]): JsValue = {
        Json.obj("id" -> Json.toJson(o.id)(idWrites)) ++ Json
          .toJson(o.entity)(entityWrites)
          .as[JsObject]
      }
    }
  }

  implicit def entityWithIdReads[ID, T](implicit idReads: Reads[ID],
                                        entityReads: Reads[T]) =
    new Reads[EntityWithId[ID, T]] {
      override def reads(json: JsValue): JsResult[EntityWithId[ID, T]] = {
        val idResult = (json \ "id").validate[ID]
        val entityResult = json
          .validate[JsObject]
          .map(_ - "id")
          .flatMap(_.validate[T](entityReads))

        for {
          id <- idResult
          entity <- entityResult
        } yield EntityWithId(id, entity)
      }
    }

  implicit val dayWithIdWrites = entityWithIdWrites[Day.Id, Day]
  implicit val childWithIdWrites = entityWithIdWrites[Child.Id, Child]
  implicit val contactPersonWithIdWrites =
    entityWithIdWrites[ContactPerson.Id, ContactPerson]
  implicit val crewWithIdWrites = entityWithIdWrites[Crew.Id, Crew]
  implicit val configWithIdWrites = entityWithIdWrites[String, ConfigWrapper]
}
