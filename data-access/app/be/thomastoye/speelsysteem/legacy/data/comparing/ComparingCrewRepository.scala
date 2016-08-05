package be.thomastoye.speelsysteem.legacy.data.comparing

import javax.inject.Inject

import be.thomastoye.speelsysteem.legacy.data.CrewRepository
import be.thomastoye.speelsysteem.legacy.data.couchdb.CouchCrewRepository
import be.thomastoye.speelsysteem.legacy.data.slick.SlickCrewRepository
import be.thomastoye.speelsysteem.models.Crew
import be.thomastoye.speelsysteem.models.Crew.Id

import scala.concurrent.Future

class ComparingCrewRepository @Inject() (couchCrewRepository: CouchCrewRepository, slickCrewRepository: SlickCrewRepository)
extends CrewRepository with ComparingRepository
{
  override def findById(id: Id): Future[Option[(Crew.Id, Crew)]] = doCompare("CrewRepository#findById", slickCrewRepository.findById(id), couchCrewRepository.findById(id))

  override def findAll: Future[Seq[(Crew.Id, Crew)]] = doCompare("CrewRepository#findAll", slickCrewRepository.findAll, couchCrewRepository.findAll, Some("Crew"))

  override def insert(crewMember: Crew): Future[Unit] = doCompare("CrewRepository#insert", slickCrewRepository.insert(crewMember), couchCrewRepository.insert(crewMember))

  override def count: Future[Int] = doCompare("CrewRepository#count", slickCrewRepository.count, couchCrewRepository.count)

  override def update(id: Id, crewMember: Crew): Future[Unit] = doCompare("CrewRepository#update", slickCrewRepository.update(id, crewMember), couchCrewRepository.update(id, crewMember))
}
