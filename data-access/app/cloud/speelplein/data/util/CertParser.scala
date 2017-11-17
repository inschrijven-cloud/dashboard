package cloud.speelplein.dashboard.util

import java.io.{ByteArrayInputStream, StringReader}
import java.security.PublicKey
import java.security.cert.CertificateFactory

import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.openssl.PEMParser

object CertParser {
  def pemToPublicKey(pem: String): PublicKey = {
    val parser = new PEMParser(new StringReader(pem))

    @SuppressWarnings(Array("org.wartremover.warts.Throw"))
    val certificateHolder =
      parser.readObject() match {
        case holder: X509CertificateHolder => holder
        case invalid =>
          throw new Exception(
            s"Tried to get parser, expected X509CertificateHolder but got $invalid")
      }

    val certificateFactory = CertificateFactory.getInstance("X.509")

    certificateFactory
      .generateCertificate(
        new ByteArrayInputStream(certificateHolder.getEncoded))
      .getPublicKey
  }
}
