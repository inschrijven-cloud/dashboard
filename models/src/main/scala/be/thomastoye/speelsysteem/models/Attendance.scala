package be.thomastoye.speelsysteem.models

import java.time.Instant

case class SingleAttendance(
  /** The shift this attendance is about */
  shiftId: Shift.Id,
  /** When the child was enrolled (intention to participate in an activity) */
  enrolled: Option[Instant] = None,
  /** Who registered the child's intent to participate in an activity */
  enrolledRegisteredBy: Option[Crew.Id] = None,
  /** When the child arrived to participate in an activity */
  arrived: Option[Instant] = None,
  /** Which crew member registered the child as arrived */
  arrivedRegisteredBy: Option[Crew.Id] = None,
  /** When the child left/went home after the activity */
  left: Option[Instant] = None,
  /** Who registered the child leaving */
  leftRegisteredBy: Option[Crew.Id] = None
)

case class DayAttendance(day: Day.Id, shifts: Seq[SingleAttendance])
