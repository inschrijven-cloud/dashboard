import java.time.LocalDate

import be.thomastoye.speelsysteem.models.DayDate
import org.scalatest._

class DayDateSpec extends WordSpec with Matchers {

  "DayDate#toLocalDate" should {
    "work" in {
      val day = DayDate(1, 2, 2003)
      day.toLocalDate should be(LocalDate.of(2003, 2, 1))
    }
  }

  "DayDate#toString" should {
    "work for 01/02/2003" in {
      val day = DayDate(1, 2, 2003)
      day.toString should be("2003-02-01")
    }

    "work for 29/03/2003" in {
      val day = DayDate(29, 3, 2003)
      day.toString should be("2003-03-29")
    }
  }

  "DayDate#compare" should {
    "2017-08-10 should be equal to 2017-08-10" in {
      val day1 = DayDate(10, 8, 2017)
      val day2 = DayDate(10, 8, 2017)

      day1.compare(day2) should be(0)
    }

    "2017-08-10 should be smaller than 2017-08-11" in {
      val day1 = DayDate(10, 8, 2017)
      val day2 = DayDate(11, 8, 2017)

      day1.compare(day2) should be < 0
    }

    "2016-12-29 should be greater than 2014-01-01" in {
      val day1 = DayDate(29, 12, 2016)
      val day2 = DayDate(1, 1, 2014)

      day1.compare(day2) should be > 0
    }
  }
}

