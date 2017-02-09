package be.thomastoye.speelsysteem.models

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import scala.util.Try

object DayDate {
  def createFromDayId(dayId: Day.Id): Try[DayDate] = Try(LocalDate.parse(dayId, DateTimeFormatter.ISO_LOCAL_DATE)).map(createFromLocalDate)

  def createFromLocalDate(date: LocalDate): DayDate = DayDate(date.getDayOfMonth, date.getMonth.getValue, date.getYear)
}

/**
 * This class represents a date. It's analogous to java.time.LocalDate, but it's implemented here as a simple case
 * class for easy JSON (de)serialisation.
 *
 * @param day   Day of the month, 1-based (first day of the month == 1)
 * @param month Month of the year, 1-based (January == 1, December == 12)
 * @param year  Year with 4 digits (e.g. 1990)
 */
case class DayDate(day: Int, month: Int, year: Int) extends Ordered[DayDate] {
  def toLocalDate: LocalDate = LocalDate.of(year, month, day)

  override def toString: String = toLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

  override def compare(that: DayDate): Int = this.toLocalDate.compareTo(that.toLocalDate)

  def getDayId: Day.Id = toString
}
