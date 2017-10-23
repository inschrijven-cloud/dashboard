package be.thomastoye.speelsysteem.models

case class ContactPersonRelationship(
  contactPersonId: ContactPerson.Id,
  /** The relationship between the person and the contact person. E.g. "Father", "Grandparent" */
  relationship: String
)
