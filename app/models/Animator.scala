package models

import play.api.db.slick.Config.driver.simple._
import org.joda.time.LocalDate

import scala.slick.lifted.ProvenShape

case class Animator(
  id: Option[Long] = None,
  firstName: String,
  lastName: String,
  mobilePhone: Option[String],
  landline: Option[String],
  email: Option[String],

  street: Option[String],
  city: Option[String],
  bankAccount: Option[String],
  yearStartedVolunteering: Option[Int],
  isPartOfCore: Boolean = false,
  //attest: Option[Attest] = None,
  birthDate: Option[LocalDate]
)

private[models] class Animators(tag: Tag) extends Table[Animator](tag, "animator") {
  import helpers.Db.jodaDatetimeToSqldateMapper

  private[models] def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  private[models] def firstName = column[String]("first_name")
  private[models] def lastName = column[String]("last_name")
  private[models] def mobilePhone = column[String]("mobile_phone", O.Nullable)
  private[models] def landline = column[String]("landline", O.Nullable)
  private[models] def email = column[String]("email", O.Nullable)

  private[models] def street = column[String]("street", O.Nullable)
  private[models] def city = column[String]("city", O.Nullable)

  private[models] def bankAccount = column[String]("bank_account", O.Nullable)
  private[models] def yearStartedVolunteering = column[Int]("year_started_volunteering", O.Nullable)
  private[models] def isPartOfCore = column[Boolean]("is_core")

  private[models] def birthDate = column[LocalDate]("birthdate", O.Nullable)

  def * : ProvenShape[Animator] = (id.?, firstName, lastName, mobilePhone.?, landline.?, email.?,
    street.?, city.?, bankAccount.?, yearStartedVolunteering.?, isPartOfCore, birthDate.?) <>
    (Animator.tupled, Animator.unapply)
}
object Animators {
  val animators = TableQuery[Animators]

  def findById(id: Long)(implicit s: Session): Option[Animator] = animators.filter(_.id === id).firstOption
  def findAll(implicit s: Session): List[Animator] = animators.list
  def insert(animator: Animator)(implicit s: Session): Unit = animators.insert(animator)
  def count(implicit s: Session): Int = animators.length.run
  def update(animator: Animator)(implicit s: Session): Unit = {
    animator.id match {
      case Some(id) => animators.filter(_.id === id).update(animator)
      case _ =>
    }
  }
}

object AnimatorVals {
  val minimumYearStartedVolunteering: Int = 2000
  val maximumYearStartedVolunteering: Int = 2030
}
