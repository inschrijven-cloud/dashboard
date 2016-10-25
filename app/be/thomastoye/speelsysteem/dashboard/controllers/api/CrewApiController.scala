package be.thomastoye.speelsysteem.dashboard.controllers.api

import be.thomastoye.speelsysteem.models.Crew
import play.api.mvc.{Action, AnyContent}

class CrewApiController extends ApiController {
  def create: Action[AnyContent] = TODO
  def all: Action[AnyContent] = TODO
  def getById(id: Crew.Id): Action[AnyContent] = TODO
  def update: Action[AnyContent] = TODO
  def delete(id: Crew.Id): Action[AnyContent] = TODO
}
