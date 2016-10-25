package be.thomastoye.speelsysteem

case class EntityWithId[ID, T](id: ID, entity: T)
