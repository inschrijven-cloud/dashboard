package cloud.speelplein.models

import java.time.Instant

// TODO merge with TenantMetadata
case class TenantUserData(name: String,
                          roles: Seq[String],
                          permissions: Seq[String])

case class User(id: String,
                name: String,
                email: String,
                tenants: Seq[TenantUserData],
                loginsCount: Int,
                lastLogin: Instant,
                picture: Option[String],
                pictureLarge: Option[String])
