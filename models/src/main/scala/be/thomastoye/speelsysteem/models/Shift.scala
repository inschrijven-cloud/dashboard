package be.thomastoye.speelsysteem.models

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

  sealed trait ShiftKind {
    val mnemonic: String
    val description: String
  }

  /** Common shift kinds */
  object ShiftKind {
    case object Early extends ShiftKind {
      override val mnemonic = "VRO"
      override val description: String = "Vroeg"
    }

    case object Morning extends ShiftKind {
      override val mnemonic = "VM"
      override val description: String = "Voormiddag"
    }

    case object Noon extends ShiftKind {
      override val mnemonic = "MID"
      override val description: String = "Middag"
    }

    case object Afternoon extends ShiftKind {
      override val mnemonic = "NM"
      override val description: String = "Namiddag"
    }

    case object Evening extends ShiftKind {
      override val mnemonic = "AV"
      override val description: String = "Avond"
    }

    case object External extends ShiftKind {
      override val mnemonic = "EXT"
      override val description: String = "Externe activiteit"
    }

    case object CrewActivity extends ShiftKind {
      override val mnemonic = "LEI"
      override val description: String = "Leidingsactiviteit"
    }

    def apply(mnemonic: String): ShiftKind = mnemonic match {
      case "VRO" => Early
      case "VM" => Morning
      case "MID" => Noon
      case "NM" => Afternoon
      case "AV" => Evening
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
    kind: ShiftKind, // refactor out ShiftKind eventually
    location: Option[String],
    desciption: Option[String],
    startAndEnd: Option[StartAndEndTime]
) extends Ordered[Shift] {

  override def compare(that: Shift): Int = {
    val defaultStartEnd = StartAndEndTime(RelativeTime(23, 59), RelativeTime(23, 59))
    this.startAndEnd.getOrElse(defaultStartEnd).start.compare(that.startAndEnd.getOrElse(defaultStartEnd).start)
  }
}

case class RelativeTime(hour: Int, minute: Int) extends Ordered[RelativeTime] {
  override def compare(that: RelativeTime): Int = this.hour * 60 + this.minute - (that.hour * 60 + that.minute)
}

case class StartAndEndTime(start: RelativeTime, end: RelativeTime)
