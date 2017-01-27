package be.thomastoye.speelsysteem.models

import java.time.Instant

case class SingleAttendance(shift: Shift.Id, registeredBy: Option[Crew.Id], registered: Option[Instant])

case class DayAttendance(day: Day.Id, shifts: Seq[SingleAttendance])
