package cloud.speelplein

case class EntityWithId[ID, T](id: ID, entity: T)
