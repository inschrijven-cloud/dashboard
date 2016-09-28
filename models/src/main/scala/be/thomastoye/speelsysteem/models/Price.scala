package be.thomastoye.speelsysteem.models

case class Price(euro: Int, cents: Int) {
  def +(that: Price): Price = {
    val resCents = this.cents + that.cents
    val carry = (resCents - (resCents % 100)) / 100
    val resEuro = this.euro + that.euro + carry
    Price(resEuro, resCents % 100)
  }

  override def toString = {
    val formattedCents = cents match {
      case 0 => "00"
      case n if n < 10 => "0" + n
      case n => n.toString
    }
    s"€$euro.$formattedCents"
  }
}
