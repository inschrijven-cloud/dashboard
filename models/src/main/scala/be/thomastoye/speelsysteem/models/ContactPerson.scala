package be.thomastoye.speelsysteem.models

object ContactPerson {
  type Id = String
}

case class ContactPerson(
  firstName: String,
  lastName: String,
  address: Address,
  phone: Seq[PhoneContact]
)
