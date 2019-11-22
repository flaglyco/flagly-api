package co.flagly.api

import co.flagly.api.account.{AccountController, AccountDürüm, AccountRepository, AccountService}
import co.flagly.api.application.{ApplicationController, ApplicationDürüm, ApplicationRepository, ApplicationService}
import co.flagly.api.common.PublicDürüm
import co.flagly.api.durum.{Ctx, ResponseLog}
import co.flagly.api.flag.{FlagController, FlagRepository, FlagService}
import co.flagly.api.session.SessionRepository
import dev.akif.e.E
import dev.akif.e.playjson.eWrites
import play.api.ApplicationLoader.Context
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.{DBComponents, Database, HikariCPComponents}
import play.api.http.{ContentTypes, HttpErrorHandler}
import play.api.libs.json.Json
import play.api.mvc.Results.Status
import play.api.mvc.{EssentialFilter, RequestHeader, Result}
import play.api.routing.Router
import play.api.{BuiltInComponentsFromContext, Logger}
import play.filters.HttpFiltersComponents
import play.filters.cors.{CORSConfig, CORSFilter}
import router.Routes

import scala.concurrent.Future

class Components(ctx: Context) extends BuiltInComponentsFromContext(ctx)
                                           with DBComponents
                                           with EvolutionsComponents
                                           with HikariCPComponents
                                           with HttpFiltersComponents {
  // Touch the lazy val so database migrations are run on startup
  applicationEvolutions

  lazy val requestLogger: Logger = Logger("RequestLogger")

  override lazy val httpErrorHandler: HttpErrorHandler = new ErrorHandler(requestLogger, handleError)

  override def httpFilters: Seq[EssentialFilter] = Seq(new CORSFilter(CORSConfig.fromConfiguration(configuration), httpErrorHandler))

  lazy val database: Database = dbApi.database("default")

  lazy val accountRepository: AccountRepository         = new AccountRepository
  lazy val applicationRepository: ApplicationRepository = new ApplicationRepository
  lazy val flagRepository: FlagRepository               = new FlagRepository
  lazy val sessionRepository: SessionRepository         = new SessionRepository

  lazy val accountService: AccountService         = new AccountService(accountRepository, sessionRepository, database)
  lazy val applicationService: ApplicationService = new ApplicationService(applicationRepository, database)
  lazy val flagService: FlagService               = new FlagService(flagRepository, database)

  lazy val publicDürüm: PublicDürüm           = new PublicDürüm(requestLogger, controllerComponents)
  lazy val accountDürüm: AccountDürüm         = new AccountDürüm(accountService, requestLogger, controllerComponents)
  lazy val applicationDürüm: ApplicationDürüm = new ApplicationDürüm(applicationService, requestLogger, controllerComponents)

  lazy val rootController: RootController               = new RootController(publicDürüm, controllerComponents)
  lazy val accountController: AccountController         = new AccountController(accountService, publicDürüm, accountDürüm, controllerComponents)
  lazy val applicationController: ApplicationController = new ApplicationController(accountService, applicationService, accountDürüm, controllerComponents)
  lazy val flagController: FlagController               = new FlagController(accountService, flagService, accountDürüm, controllerComponents)
  lazy val sdkController: SDKController                 = new SDKController(applicationService, flagService, applicationDürüm, controllerComponents)

  override def router: Router =
    new Routes(
      httpErrorHandler,
      rootController,
      accountController,
      applicationController,
      flagController,
      sdkController
    )

  private def handleError(request: RequestHeader, e: E): Future[Result] = {
    val headers = request.headers.headers.toMap
    val log = ResponseLog(e.code, request.method, request.uri, Ctx.getOrCreateId(headers), 0, headers, e.toString)
    requestLogger.error(log.toLogString(isIncoming = false))
    Future.successful(Status(e.code)(Json.toJson(e)).as(ContentTypes.JSON))
  }
}
