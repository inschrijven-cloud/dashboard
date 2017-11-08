package cloud.speelplein.models

case class Crew(
  firstName: String,
  lastName: String,
  address: Address,
  active: Boolean = true,
  bankAccount: Option[String] = None,
  contact: ContactInfo,
  yearStarted: Option[Int] = None,
  birthDate: Option[DayDate],
  remarks: String = ""
)

object Crew {
  type Id = String
}
