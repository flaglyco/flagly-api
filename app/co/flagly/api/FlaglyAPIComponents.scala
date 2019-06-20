package co.flagly.api

import co.flagly.api.controllers.{FlagController, RootController}
import co.flagly.api.repositories.FlagRepository
import co.flagly.api.services.FlagService
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.{DBComponents, Database, HikariCPComponents}
import play.api.http.HttpErrorHandler
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import router.Routes

class FlaglyAPIComponents(ctx: Context) extends BuiltInComponentsFromContext(ctx)
                                           with DBComponents
                                           with EvolutionsComponents
                                           with HikariCPComponents
                                           with HttpFiltersComponents {
  // Touch the lazy val so database migrations are run on startup
  applicationEvolutions

  override lazy val httpErrorHandler: HttpErrorHandler = new FlaglyAPIErrorHandler

  override def httpFilters: Seq[EssentialFilter] = Seq.empty

  lazy val database: Database = dbApi.database("default")

  lazy val flagRepository: FlagRepository = new FlagRepository(database)

  lazy val flagService: FlagService = new FlagService(flagRepository)

  lazy val rootController: RootController = new RootController(controllerComponents)
  lazy val flagController: FlagController = new FlagController(flagService, controllerComponents)

  override def router: Router =
    new Routes(
      httpErrorHandler,
      rootController,
      flagController
    )
}
