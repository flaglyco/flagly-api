package co.flagly.api

import co.flagly.api.account.{AccountController, AccountDurum, AccountRepository, AccountService}
import co.flagly.api.application.{ApplicationController, ApplicationDurum, ApplicationRepository, ApplicationService}
import co.flagly.api.common.PublicDurum
import co.flagly.api.flag.{FlagController, FlagRepository, FlagService}
import co.flagly.api.session.SessionRepository
import dev.akif.durum.Durum
import dev.akif.durum.ResponseLog
import dev.akif.durum.LogType
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

  lazy val publicDurum: PublicDurum           = new PublicDurum(requestLogger, controllerComponents)
  lazy val accountDurum: AccountDurum         = new AccountDurum(accountService, requestLogger, controllerComponents)
  lazy val applicationDurum: ApplicationDurum = new ApplicationDurum(applicationService, requestLogger, controllerComponents)

  lazy val rootController: RootController               = new RootController(publicDurum, controllerComponents)
  lazy val accountController: AccountController         = new AccountController(accountService, publicDurum, accountDurum, controllerComponents)
  lazy val applicationController: ApplicationController = new ApplicationController(accountService, applicationService, accountDurum, controllerComponents)
  lazy val flagController: FlagController               = new FlagController(accountService, flagService, accountDurum, controllerComponents)
  lazy val sdkController: SDKController                 = new SDKController(applicationService, flagService, applicationDurum, controllerComponents)

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
    val log = ResponseLog(headers.getOrElse(Durum.idHeaderName, "N/A"), 0L, request.method, request.uri, headers, e.toString, failed = true, e.code, 0L)
    requestLogger.error(log.toLog(LogType.OutgoingResponse))
    Future.successful(Status(e.code)(Json.toJson(e)).as(ContentTypes.JSON))
  }
}
