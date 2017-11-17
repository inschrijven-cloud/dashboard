import java.time.Instant

import cloud.speelplein.models.{TenantUserData, User}
import cloud.speelplein.models.JsonFormats.auth0UserReads
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsSuccess, Json}

class UserJsonSpec extends WordSpec with MustMatchers {
  val data: String =
    """
      |[
      |  {
      |    "name": "John Doe",
      |    "email": "john@doe.io",
      |    "given_name": "John",
      |    "family_name": "Doe",
      |    "gender": "male",
      |    "picture": "https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/11111111_11111111111111111_1111111111111111111_n.jpg?oh=c1111111111111111111111111111111&oe=11111111",
      |    "picture_large": "https://scontent.xx.fbcdn.net/v/t1.0-1/11111111_11111111111111111_1111111111111111111_n.jpg?oh=11111111111111111111111111111111&oe=11111111",
      |    "age_range": {
      |      "min": 21
      |    },
      |    "context": {
      |      "mutual_likes": {
      |        "data": [],
      |        "summary": {
      |          "total_count": 100
      |        }
      |      },
      |      "id": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
      |    },
      |    "cover": {
      |      "id": "11111111111111111",
      |      "offset_x": 0,
      |      "offset_y": 49,
      |      "source": "https://scontent.xx.fbcdn.net/v/t31.0-8/s720x720/11111111111111111111111111_1111111111111111111_o.jpg?oh=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx&oe=11111111"
      |    },
      |    "devices": [
      |      {
      |        "os": "Android"
      |      }
      |    ],
      |    "updated_time": "2017-11-09T14:54:00+0000",
      |    "installed": true,
      |    "is_verified": false,
      |    "link": "https://www.facebook.com/app_scoped_user_id/11111111111111111/",
      |    "locale": "en_GB",
      |    "name_format": "{first} {last}",
      |    "timezone": 1,
      |    "third_party_id": "xxxxxxxxxxxxxxxxxxxxxxxxxxx",
      |    "verified": true,
      |    "email_verified": true,
      |    "updated_at": "2017-11-16T11:53:44.198Z",
      |    "user_id": "facebook|11556916548941111",
      |    "nickname": "johnny",
      |    "identities": [
      |      {
      |        "user_id": "11111111111111111",
      |        "provider": "facebook",
      |        "connection": "facebook",
      |        "isSocial": true,
      |        "access_token": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
      |      }
      |    ],
      |    "created_at": "2017-10-31T09:12:26.415Z",
      |    "last_ip": "157.111.222.333",
      |    "last_login": "2017-11-16T11:53:44.198Z",
      |    "logins_count": 40,
      |    "app_metadata": {
      |      "tenants": [
      |        {
      |          "name": "example",
      |          "permissions": [
      |            "child:retrieve"
      |          ],
      |          "roles": [
      |            "admin"
      |          ]
      |        },
      |        {
      |          "name": "despeelberg",
      |          "permissions": [
      |            "child:retrieve"
      |          ],
      |          "roles": [
      |            "admin"
      |          ]
      |        },
      |        {
      |          "name": "global",
      |          "permissions": [
      |            "child:retrieve",
      |            "superuser:list-databases",
      |            "superuser:list-tenants",
      |            "superuser:list-all-config",
      |            "superuser:init-config-db",
      |            "superuser:create-config"
      |          ],
      |          "roles": [
      |            "admin",
      |            "superuser"
      |          ]
      |        }
      |      ]
      |    }
      |  }
      |]
    """.stripMargin

  "User JSON reads" should {
    "correctly read Auth0 JSON" in {
      val userOpt = Json.parse(data).validate[Seq[User]]

      userOpt must be(
        JsSuccess(List(User(
          "facebook|11556916548941111",
          "John Doe",
          "john@doe.io",
          Seq(
            TenantUserData("example", Seq("admin"), Seq("child:retrieve")),
            TenantUserData("despeelberg", Seq("admin"), Seq("child:retrieve")),
            TenantUserData(
              "global",
              Seq("admin", "superuser"),
              Seq("child:retrieve",
                  "superuser:list-databases",
                  "superuser:list-tenants",
                  "superuser:list-all-config",
                  "superuser:init-config-db",
                  "superuser:create-config")
            )
          ),
          40,
          Instant.ofEpochMilli(1510833224198L),
          Some("https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/11111111_11111111111111111_1111111111111111111_n.jpg?oh=c1111111111111111111111111111111&oe=11111111"),
          Some("https://scontent.xx.fbcdn.net/v/t1.0-1/11111111_11111111111111111_1111111111111111111_n.jpg?oh=11111111111111111111111111111111&oe=11111111")
        ))))
    }
  }
}
