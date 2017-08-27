package be.thomastoye.speelsysteem.models

case class Crew(
  firstName: String,
  lastName: String,
  address: Address,
  bankAccount: Option[String] = None,
  contact: ContactInfo,
  yearStarted: Option[Int] = None,
  birthDate: Option[DayDate] // TODO add "active" field (boolean)
)

object Crew {
  type Id = String
}
