package cloud.speelplein.data.util

import java.util.UUID

trait UuidService {
  def random: String
}

class UuidServiceImpl() extends UuidService {
  override def random: String = UUID.randomUUID().toString
}
