package cloud.speelplein.dashboard.controllers.api.auth

import cloud.speelplein.dashboard.controllers.api.auth.Permission._
import play.api.libs.json.{Json, OFormat}

case class Role(
    id: String,
    description: String,
    longerDescription: String,
    impliedPermissions: Seq[Permission]
)

object Role {
  val crewRole =
    Role(
      "animator",
      "Animator: Laat toe om de basis te doen",
      "Nieuwe kinderen aanmaken en weergeven, contactpersonen beheren, kinderen, inschrijven...",
      Seq(
        childRetrieve,
        childUpdate,
        childCreate,
        childDelete,
        childMerge,
        childAttendanceRetrieve,
        childAttendanceCreate,
        childAttendanceDelete,
        crewAttendanceRetrieve,
        crewAttendanceCreate,
        crewAttendanceDelete,
        contactPersonRetrieve,
        contactPersonUpdate,
        contactPersonCreate,
        contactPersonDelete,
        crewRetrieve,
        dayCreate,
        dayRetrieve,
        dayUpdate,
        dayDelete,
        ageGroupsRead,
        exportChildren,
        exportCrew,
        exportFiscalCert,
        exportChildrenPerDay,
        exportCrewCompensation,
        auditLogAddEntry
      )
    )

  val leaderCrewRole =
    Role(
      "hoofdanimator",
      "Hoofdanimator/verantwoordelijk: Animator + Laat toe om animatoren te beheren",
      "Naast wat een animator kan, kan een hoofdanimator ook nieuwe animatoren aanmaken en bewerken",
      crewRole.impliedPermissions ++ Seq(crewUpdate,
                                         crewCreate,
                                         crewDelete,
                                         crewMerge,
                                         ageGroupsCreateAndUpdate,
                                         auditLogRead)
    )

  val superuser = Role(
    "superuser",
    "Platformadministratie",
    "Deze rol laat toe om nieuwe organisaties aan te maken en platformconfiguratie aan te maken",
    Permission.allFlat
  )

  val all = Seq("global" -> Seq(superuser),
                "organisation" -> Seq(crewRole, leaderCrewRole))

  val allFlat: Seq[Role] = all flatMap { case (key, value) => value }

  implicit val roleFormat: OFormat[Role] = Json.format[Role]

  def parseRoleName(roleName: String): Option[Role] =
    allFlat.find(_.id == roleName)
}
