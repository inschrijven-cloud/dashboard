package cloud.speelplein.models

case class Address(
  /** The street component of the address. E.g.: "Straatlaan" */
  street: Option[String] = None,
  /** The house number. E.g. "55A bus 2" */
  number: Option[String] = None,
  /** The zip code of the city. E.g. "5555" */
  zipCode: Option[Int] = None,
  /** The city. E.g. "Wevelgem" */
  city: Option[String] = None
)

object Address {
  val empty = Address(None, None, None, None)
}
