package cloud.speelplein.dashboard.controllers.api.auth

import play.api.libs.json.Json

/**
  * @param id Id of the permission. This is used to identify the permission. It's also what's inside a JWT.
  * @param description (Dutch) description of what the permission does
  */
case class Permission(
    id: String,
    description: String
)

object Permission {
  val childRetrieve = Permission(
    "child:retrieve",
    "Oplijsten van alle kinderen en individuele kinderen ophalen")
  val childUpdate =
    Permission("child:update", "Gevegevens van kinderen aanpassen")
  val childCreate = Permission("child:create", "Nieuwe kinderen aanmaken")
  val childDelete = Permission("child:delete", "Kinderen verwijderen")
  val childMerge = Permission("child:merge", "Kinderen samenvoegen")

  val childAttendanceRetrieve = Permission(
    "child-attendance:retrieve",
    "Oplijsten van aanwezigheden van kinderen")
  val childAttendanceCreate =
    Permission("child-attendance:create", "Aanwezigheden van kinderen aanmaken")
  val childAttendanceDelete =
    Permission("child-attendance:delete",
               "Aanwezigheden van kinderen verwijderen")

  val crewAttendanceRetrieve = Permission(
    "crew-attendance:retrieve",
    "Oplijsten van aanwezigheden van animatoren")
  val crewAttendanceCreate =
    Permission("crew-attendance:create",
               "Aanwezigheden van animatoren aanmaken")
  val crewAttendanceDelete =
    Permission("crew-attendance:delete",
               "Aanwezigheden van animatoren verwijderen")

  val contactPersonRetrieve = Permission(
    "contactperson:retrieve",
    "Oplijsten van alle contactpersonen en individuele contactpersonen ophalen")
  val contactPersonUpdate =
    Permission("contactperson:update", "Gegevens van kinderen aanpassen")
  val contactPersonCreate =
    Permission("contactperson:create", "Contactpersonen aanmaken")
  val contactPersonDelete =
    Permission("contactperson:delete", "Contactpersonen verwijderen")

  val crewRetrieve = Permission(
    "crew:retrieve",
    "Oplijsten van alle animatoren en individuele animatoren ophalen")
  val crewUpdate =
    Permission("crew:update", "Gevegevens van animatoren aanpassen")
  val crewCreate = Permission("crew:create", "Animatoren aanmaken")
  val crewDelete = Permission("crew:delete", "Animatoren verwijderen")
  val crewMerge = Permission("crew:merge", "Animatoren samenvoegen")

  val dayCreate = Permission("day:create", "Dagen aanmaken")
  val dayRetrieve = Permission(
    "day:retrieve",
    "Oplijsten van alle dagen en individuele dagen ophalen")
  val dayUpdate = Permission("day:update", "Dagen aanpassen")
  val dayDelete = Permission("day:delete", "Dagen verwijderen")

  val exportChildren =
    Permission("export:children", "Een lijst exporteren van alle kinderen")
  val exportCrew =
    Permission("export:crew", "Een lijst exporteren van alle animatoren")
  val exportFiscalCert = Permission(
    "export:fiscalcert",
    "Een lijst exporteren met data voor fiscale attesten")
  val exportCrewCompensation = Permission(
    "export:crewpcomensation",
    "Een lijst exporteren met wanneer animatoren aanwezig waren.")
  val exportChildrenPerDay = Permission(
    "report:children-per-day",
    "Een lijst exporteren met hoeveel kinderen er per dag aanwezig waren")

  val listDatabases =
    Permission("superuser:list-databases", "Toon een lijst met alle databases")

  val listTenants =
    Permission("superuser:list-tenants", "Toon alle organisaties")
  val initTenantDbs = Permission(
    "superuser:init-dbs",
    "Initialiseer de databases van een organisatie")
  val syncTenantDb = Permission(
    "superuser:sync-db",
    "Synchroniseer databases met een externe server")
  val createTenant =
    Permission("superuser:create-tenant", "Nieuwe organisaties aanmaken")

  val listAllConfig =
    Permission("superuser:list-all-config", "Alle configuratie oplijsten")

  val initializeAllConfigDb =
    Permission("superuser:init-config-db",
               "De configuratiedatabase initialiseren")

  val createConfig =
    Permission(
      "superuser:create-config",
      "Nieuwe configuratiedocumenten voor gebruikers aanmaken en updaten")

  val userRetrieve =
    Permission("user:list", "Alle gebruikers en hun gebruikersdata oplijsten")

  val userPutTenantData = Permission(
    "users:put-data",
    "Gebruikersdata voor de huidige organisatie aanmaken en aanpassen")

  val userPutTenantDataAnyTenant = Permission(
    "users:put-data",
    "Gebruikersdata voor een willekeurige organisatie aanmaken en aanpassen")

  val auditLogAddEntry = Permission(
    "audit-log:add-entry",
    "Data toevoegen aan het audit-logboek (wie wat heeft aangemaakt en opgevraagd)")
  val auditLogRead = Permission(
    "audit-log:read",
    "Data in het audit-logboek bekijken (wie wat heeft aangemaakt en opgevraagd)")

  val ageGroupsRead =
    Permission("age-groups:retrieve", "Leeftijdsgroepen ophalen en bekijken")
  val ageGroupsCreateAndUpdate =
    Permission("age-groups:retrieve", "Leeftijdsgroepen aanmaken en updaten")

  val all: Map[String, Seq[Permission]] = Map(
    "Kinderen" -> Seq(childRetrieve,
                      childUpdate,
                      childCreate,
                      childDelete,
                      childMerge),
    "Aanwezigheden van kinderen" -> Seq(childAttendanceRetrieve,
                                        childAttendanceCreate,
                                        childAttendanceDelete),
    "Aanwezigheden van animatoren" -> Seq(crewAttendanceRetrieve,
                                          crewAttendanceCreate,
                                          crewAttendanceDelete),
    "Contactpersonen" -> Seq(contactPersonRetrieve,
                             contactPersonUpdate,
                             contactPersonCreate,
                             contactPersonDelete),
    "Animatoren" -> Seq(crewRetrieve,
                        crewUpdate,
                        crewCreate,
                        crewDelete,
                        crewMerge),
    "Dagen" -> Seq(dayCreate, dayRetrieve, dayUpdate, dayDelete),
    "Leeftijdsgroepen" -> Seq(ageGroupsRead, ageGroupsCreateAndUpdate),
    "Exporteren van lijsten" -> Seq(exportChildren,
                                    exportCrew,
                                    exportFiscalCert,
                                    exportChildrenPerDay,
                                    exportCrewCompensation),
    "Platformbeheer" -> Seq(listDatabases,
                            listTenants,
                            initTenantDbs,
                            syncTenantDb,
                            createTenant,
                            listAllConfig,
                            initializeAllConfigDb,
                            createConfig,
                            userPutTenantDataAnyTenant),
    "Gebruikers" -> Seq(
      userRetrieve,
      userPutTenantData
    ),
    "Audit logboek" -> Seq(
      auditLogAddEntry,
      auditLogRead
    )
  )

  val allFlat
    : Seq[Permission] = all.flatMap { case (key, value) => value }.toSeq

  implicit val permissionFormat = Json.format[Permission]
}
