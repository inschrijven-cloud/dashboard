package be.thomastoye.speelsysteem.models

import java.time.Instant

import be.thomastoye.speelsysteem.models.Shift.ShiftKind

object Day {
  type Id = String
}

case class Day(
  date: DayDate,
  shifts: Seq[Shift]
)

object Shift {
  type Id = String

  sealed trait ShiftKind { val mnemonic: String }

  /** Common shift kinds */
  object ShiftKind {
    case object Early extends ShiftKind { override val mnemonic  = "VRO"}
    case object Morning extends ShiftKind { override val mnemonic = "VM" }
    case object Noon extends ShiftKind { override val mnemonic = "MID" }
    case object Afternoon extends ShiftKind { override val mnemonic = "NM" }
    case object Evening extends ShiftKind { override val mnemonic = "AV" }
    case object External extends ShiftKind { override val mnemonic = "EXT" }
    case object CrewActivity extends ShiftKind { override val mnemonic = "LEI" }

    def apply(mnemonic: String): ShiftKind = mnemonic match {
      case "VRO" => Early
      case "VM"  => Morning
      case "MID" => Noon
      case "NM"  => Afternoon
      case "AV"  => Evening
      case "EXT" => External
      case "LEI" => CrewActivity
    }
  }
}

case class Shift(
  id: Shift.Id,
  price: Price,
  childrenCanBePresent: Boolean,
  crewCanBePresent: Boolean,
  kind: ShiftKind,
  location: Option[String],
  desciption: Option[String],
  startAndEnd: Option[StartAndEndTime]
)

case class RelativeTime(hour: Int, minute: Int)

case class StartAndEndTime(start: RelativeTime, end: RelativeTime)
