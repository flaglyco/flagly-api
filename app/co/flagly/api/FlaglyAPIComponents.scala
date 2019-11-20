package co.flagly.api

import co.flagly.api.account.{AccountController, AccountRepository, AccountService}
import co.flagly.api.application.{ApplicationController, ApplicationRepository, ApplicationService}
import co.flagly.api.flag.{FlagController, FlagRepository, FlagService}
import co.flagly.api.session.SessionRepository
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.{DBComponents, Database, HikariCPComponents}
import play.api.http.HttpErrorHandler
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import play.filters.cors.{CORSConfig, CORSFilter}
import router.Routes

class FlaglyAPIComponents(ctx: Context) extends BuiltInComponentsFromContext(ctx)
                                           with DBComponents
                                           with EvolutionsComponents
                                           with HikariCPComponents
                                           with HttpFiltersComponents {
  // Touch the lazy val so database migrations are run on startup
  applicationEvolutions

  override lazy val httpErrorHandler: HttpErrorHandler = new FlaglyAPIErrorHandler

  override def httpFilters: Seq[EssentialFilter] = Seq(new CORSFilter(CORSConfig.fromConfiguration(configuration), httpErrorHandler))

  lazy val database: Database = dbApi.database("default")

  lazy val accountRepository: AccountRepository         = new AccountRepository
  lazy val applicationRepository: ApplicationRepository = new ApplicationRepository
  lazy val flagRepository: FlagRepository               = new FlagRepository
  lazy val sessionRepository: SessionRepository         = new SessionRepository

  lazy val accountService: AccountService         = new AccountService(accountRepository, sessionRepository, database)
  lazy val applicationService: ApplicationService = new ApplicationService(applicationRepository, database)
  lazy val flagService: FlagService               = new FlagService(flagRepository, database)

  lazy val rootController: RootController               = new RootController(controllerComponents)
  lazy val accountController: AccountController         = new AccountController(accountService, controllerComponents)
  lazy val applicationController: ApplicationController = new ApplicationController(applicationService, accountService, controllerComponents)
  lazy val flagController: FlagController               = new FlagController(flagService, accountService, controllerComponents)
  lazy val sdkController: SDKController                 = new SDKController(applicationService, flagService, controllerComponents)

  override def router: Router =
    new Routes(
      httpErrorHandler,
      rootController,
      accountController,
      applicationController,
      flagController,
      sdkController
    )
}
