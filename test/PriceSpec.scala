import be.thomastoye.speelsysteem.models.Price
import org.scalatestplus.play.PlaySpec

class PriceSpec extends PlaySpec {
  "The price class" should {
    "correctly add 2.00 and 3.00 together" in {
      Price(2, 0) + Price(3, 0) must be(Price(5, 0))
    }

    "correctly add 2.11 and 3.23 together" in {
      Price(2, 11) + Price(3, 23) must be(Price(5, 34))
    }

    "correctly add 2.66 and 3.56 together" in {
      Price(2, 66) + Price(3, 56) must be(Price(6, 22))
    }

    "correctly add 0.01, 0.87, 5.66, 8.99 and 4.55 together" in {
      val res = Price(0, 1) + Price(0, 87) + Price(5, 66) + Price(8, 99) + Price(4, 55)
      res must be(Price(20, 8))
    }
  }

  "Price#toString" should {
    "format 2.00" in {
      Price(2, 0).toString must be("€2.00")
    }

    "format 2.05" in {
      Price(2, 5).toString must be("€2.05")
    }

    "format 2.09" in {
      Price(2, 9).toString must be("€2.09")
    }

    "format 2.10" in {
      Price(2, 10).toString must be("€2.10")
    }

    "format 2.11" in {
      Price(2, 11).toString must be("€2.11")
    }
  }
}
