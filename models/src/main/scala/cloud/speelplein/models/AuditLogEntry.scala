package cloud.speelplein.models

import java.time.Instant

import play.api.libs.json.JsObject

/**
  * Information about a person that caused an event
  *
  * @param name Full name of the person
  * @param id Id of the person that triggered the event (Auth0 id)
  * @param tenantId Organisation that the crew member belongs to
  * @param jwt The JWT used to submit the audit log entry
  */
case class AuditLogTriggeredBy(
    name: Option[String],
    id: Option[String],
    tenantId: String,
    jwt: String
)

/**
  * An entry in the audit log. The audit log system helps to see which crew members took certain actions
  *
  * @param receivedTimestamp Time on which this entry was received by the backend
  * @param timestamp Time on which the event happened
  * @param eventId Id of the event (= permission id, e.g. "child:retrieve")
  * @param data Other data relevant to the event
  * @param loggedBy By which system the event was logged ("frontend", "backend", ...)
  * @param triggeredBy Information about who triggered the event
  */
case class AuditLogEntry(
    receivedTimestamp: Instant,
    timestamp: Instant,
    triggeredBy: AuditLogTriggeredBy,
    eventId: String,
    data: AuditLogData,
    loggedBy: String
)

/**
  * Extra information about the logged action
  */
case class AuditLogData(
    childId: Option[Child.Id],
    childRetiredId: Option[Child.Id],
    childAbsorbedIntoId: Option[Child.Id],
    dayId: Option[Day.Id],
    contactPersonId: Option[ContactPerson.Id],
    crewId: Option[Crew.Id],
    tenantName: Option[String],
    year: Option[Int],
    userId: Option[String]
)

object AuditLogData {
  val empty = AuditLogData(None, None, None, None, None, None, None, None, None)

  def childId(id: Child.Id) = empty.copy(childId = Some(id))

  def mergedChild(childRetiredId: Child.Id, childAbsorbedIntoId: Child.Id) =
    empty.copy(childRetiredId = Some(childRetiredId),
               childAbsorbedIntoId = Some(childAbsorbedIntoId))

  def contactPersonId(id: ContactPerson.Id) =
    empty.copy(contactPersonId = Some(id))

  def crewId(id: Crew.Id) = empty.copy(crewId = Some(id))

  def dayId(id: Day.Id) = empty.copy(dayId = Some(id))

  def tenantName(name: String) = empty.copy(tenantName = Some(name))

  def year(year: Int) = empty.copy(year = Some(year))

  def userId(id: String) = empty.copy(userId = Some(id))
}
