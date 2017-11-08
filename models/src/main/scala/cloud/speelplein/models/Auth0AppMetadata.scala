package cloud.speelplein.models

case class TenantMetadata(name: String,
                          permissions: Seq[String],
                          roles: Seq[String])
case class Auth0AppMetadata(tenants: Seq[TenantMetadata])
