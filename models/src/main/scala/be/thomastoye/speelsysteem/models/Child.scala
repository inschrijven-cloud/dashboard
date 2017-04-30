package be.thomastoye.speelsysteem.models

case class Child(
    /**
     * The first name of the child
     */
    firstName: String,

    /**
     * The last name of the child
     */
    lastName: String,

    /**
     * The address on which the child lives.
     *
     * Deprecated: Use contact people instead.
     */
    legacyAddress: Address,

    /**
     * Ways to contact this child
     *
     * Deprecated: Use contact people instead
     */
    legacyContact: ContactInfo,

    /**
     * "male", "female", "other"
     */
    gender: Option[String],

    /**
     * Ids of people that can be contacted. First one is the primary contact person.
     */
    contactPeople: Seq[ContactPerson.Id], // TODO should be a tuple: (contact person id, relationship [e.g. father])

    /**
     * Date on which the child was born
     */
    birthDate: Option[DayDate],

    /**
     * Medical information about the child
     */
    medicalInformation: MedicalInformation,

    /**
     * Other remarks
     */
    remarks: Option[String]
) {
  def primaryContactPersonId: Option[ContactPerson.Id] = contactPeople.headOption
}

object Child {
  type Id = String
}
