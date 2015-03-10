package models

import org.joda.time.LocalDate
import play.api.db.slick.Config.driver.simple._

import scala.slick.lifted.{ForeignKeyQuery, ProvenShape}

case class Activity(id: Option[Long] = None, date: LocalDate, place: String, actNum: Long)
case class ActivityType(id: Option[Long], mnemonic: String, description: String)


private[models] class Activities(tag: Tag) extends Table[Activity](tag, "ACTIVITY") {
  import helpers.Db.jodaDatetimeToSqldateMapper

  private[models] def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  private[models] def date = column[LocalDate]("DATE", O.Nullable)
  private[models] def place = column[String]("PLACE", O.Nullable)
  private[models] def actNum = column[Long]("ACT_TYPE_NUM", O.NotNull)

  def * : ProvenShape[Activity] = (id.?, date, place, actNum) <> (Activity.tupled, Activity.unapply)

  def activityType: ForeignKeyQuery[ActivityTypes, ActivityType] = {
    foreignKey("FK_ACT_TYPE", actNum, TableQuery[ActivityTypes])(_.id)
  }
  def activityTypeJoin: Query[ActivityTypes, ActivityTypes#TableElementType, Seq] = {
    TableQuery[ActivityTypes].filter(_.id === actNum)
  }

  def children: Query[Children, Child, Seq] = {
    TableQuery[ChildrenToActivities].filter(_.activityId === id).flatMap(_.childFK)
  }
}

private[models] class ActivityTypes(tag: Tag) extends Table[ActivityType](tag, "ACTIVITY_TYPE") {
  private[models] def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  private[models] def mnemonic = column[String]("MNEMONIC", O.NotNull)
  private[models] def description = column[String]("DESCRIPTION", O.NotNull)

  def * : ProvenShape[ActivityType] = (id.?, mnemonic, description) <>
    (ActivityType.tupled, ActivityType.unapply)
}

object Activities {
  import helpers.Db.jodaDatetimeToSqldateMapper

  val activities = TableQuery[Activities]

  def findAll(implicit s: Session): List[Activity] = activities.list
  def findById(id: Long)(implicit s: Session): Seq[Activity] = activities.filter(_.id === id).run
  def insert(activity: Activity)(implicit s: Session): Unit = activities.insert(activity)
  def count(implicit s: Session): Int = activities.length.run

  def findByDate(date: LocalDate)(implicit s: Session): Seq[Activity] = activities.filter(_.date === date).run

  def findAllWithType(implicit s: Session): Seq[(ActivityType, Activity)] = (for {
    act <- activities
    t <- act.activityTypeJoin} yield {
    (t, act)
  }).run

  def findByIds(ids: List[Long])(implicit s: Session): Seq[Activity] = activities.filter(_.id inSet ids).run
}

object ActivityTypes {
  val types = TableQuery[ActivityTypes]

  def findAll(implicit s: Session): List[ActivityType] = types.list
  def findById(id: Long)(implicit s: Session): Option[ActivityType] = types.filter(_.id === id).firstOption
  def findByMnemonic(mnemonic: String)(implicit s: Session): Option[ActivityType] = {
    types.filter(_.mnemonic === mnemonic).firstOption
  }
  def insert(actType: ActivityType)(implicit s: Session): Unit = types.insert(actType)
  def count(implicit s: Session): Int = types.length.run
}
