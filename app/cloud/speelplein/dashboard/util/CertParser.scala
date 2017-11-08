package cloud.speelplein.dashboard.util

import java.io.{ ByteArrayInputStream, StringReader }
import java.security.PublicKey
import java.security.cert.CertificateFactory

import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.openssl.PEMParser

object CertParser {
  def pemToPublicKey(pem: String): PublicKey = {
    val parser = new PEMParser(new StringReader(pem))

    val certificateHolder = parser.readObject().asInstanceOf[X509CertificateHolder]

    val certificateFactory = CertificateFactory.getInstance("X.509")
    certificateFactory.generateCertificate(new ByteArrayInputStream(certificateHolder.getEncoded)).getPublicKey
  }
}
