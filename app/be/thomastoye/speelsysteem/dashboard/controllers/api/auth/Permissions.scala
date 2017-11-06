package be.thomastoye.speelsysteem.dashboard.controllers.api.auth

object Permissions {
  object Child {
    val retrieve = "child:retrieve"
    val update = "child:update"
    val create = "child:create"
    val delete = "child:delete"
    val merge = "child:merge"
  }

  object ChildAttendance {
    val numatt = "child-attendance:numatt"
    val retrieve = "child-attendance:retrieve"
    val create = "child-attendance:create"
  }

  object ContactPerson {
    val retrieve = "contactperson:retrieve"
    val create = "contactperson:create"
    val update = "contactperson:update"
    val delete = "contactperson:delete"
  }

  object Crew {
    val create = "crew:create"
    val retrieve = "crew:retrieve"
    val update = "crew:update"
    val delete = "crew:delete"
  }

  object Day {
    val create = "day:create"
    val retrieve = "day:retrieve"
    val update = "day:update"
    val delete = "day:delete"
  }

  object Export {
    val children = "export:children"
    val crew = "export:crew"
    val fiscalCert = "export:fiscalcert"
    val childrenPerDay = "report:children-per-day"
  }
}
