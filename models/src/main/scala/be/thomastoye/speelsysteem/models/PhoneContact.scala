package be.thomastoye.speelsysteem.models

case class PhoneContact(phoneNumber: String, kind: Option[String] = None, comment: Option[String] = None)
