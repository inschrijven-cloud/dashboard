package cloud.speelplein.models

object MedicalInformation {
  val empty = MedicalInformation(None, None, None, None, None, None)
}

case class MedicalInformation(
  familyDoctor: Option[String],
  allergies: Option[Allergies],
  conditions: Option[Conditions],
  otherShouldBeAwareOf: Option[String],
  tetanusLastVaccinationYear: Option[Int],
  otherRemarks: Option[String]
)

case class Allergies(allergies: Seq[String], extraInformation: Option[String])

case class Conditions(conditions: Seq[String], extraInformation: Option[String])
