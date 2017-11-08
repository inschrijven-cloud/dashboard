package cloud.speelplein.models

case class ContactInfo(phone: Seq[PhoneContact], email: Seq[String])

object ContactInfo {
  val empty = ContactInfo(Seq.empty, Seq.empty)
}
