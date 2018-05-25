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
) {
  def fullName: String = firstName + ' ' + lastName
}

object Crew {
  type Id = String
}
