package cloud.speelplein.models

case class TenantMetadata(
    name: String,
    permissions: Seq[String], // TODO should be Seq[Permission]
    roles: Seq[String])
case class Auth0AppMetadata(tenants: Seq[TenantMetadata])
