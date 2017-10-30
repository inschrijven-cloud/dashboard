package be.thomastoye.speelsysteem.models

object Tenant {
  def fromDomain(domain: String): Option[Tenant] = domain match {
    case l if l.startsWith("localhost") => Some(Tenant("localhost"))
    case dom if dom.endsWith(".speelplein.cloud") => Some(Tenant(dom.split("\\.").head))
    case _ => None
  }
}

case class Tenant(name: String) {
  def dbName: String = "ic-" + name
}
