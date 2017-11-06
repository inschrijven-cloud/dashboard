package be.thomastoye.speelsysteem.dashboard.controllers.api

import javax.inject.Inject

import be.thomastoye.speelsysteem.dashboard.controllers.actions.{ DomainAction, JwtAuthorizationBuilder }

class TenantsController @Inject() (
    domainAction: DomainAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder
) extends ApiController {

  def list = TODO

  def create = TODO

  def details(tenant: String) = TODO

  def generateDesignDocs(tenant: String) = TODO

  def syncTo(tenant: String) = TODO

  def syncFrom(tenant: String) = TODO
}
