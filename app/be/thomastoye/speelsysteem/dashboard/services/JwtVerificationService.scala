package be.thomastoye.speelsysteem.dashboard.services

import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.util.CertParser
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtOptions }
import play.api.Configuration

import scala.util.Try

trait JwtVerificationService {
  def isValid(jwt: String): Boolean
  def decode(token: String): Try[String]
}

class PdiJwtVerificationService @Inject() (config: Configuration) extends JwtVerificationService {
  private val key = CertParser.pemToPublicKey(config.get[String]("jwt.key.pem"))
  private val enableExpiration = config.getOptional[Boolean]("jwt.enableExpiration").getOrElse(true) // useful for tests

  override def isValid(token: String): Boolean = Jwt.isValid(token, key, JwtAlgorithm.allRSA(), options = JwtOptions(expiration = enableExpiration))

  override def decode(token: String): Try[String] = Jwt.decode(token, key, JwtAlgorithm.allRSA(), JwtOptions(expiration = enableExpiration))

}
