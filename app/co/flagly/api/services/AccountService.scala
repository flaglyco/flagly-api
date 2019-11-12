package co.flagly.api.services

import co.flagly.api.auth.PasswordUtils
import co.flagly.api.models.{Account, Session}
import co.flagly.api.repositories.{AccountRepository, SessionRepository}
import co.flagly.api.utilities.{Errors, PSQLErrors}
import co.flagly.api.views.{LoginAccount, RegisterAccount}
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AccountService(accounts: AccountRepository, sessions: SessionRepository, db: Database) extends BaseService(db) {
  def create(registerAccount: RegisterAccount)(implicit ec: ExecutionContext): Future[(Account, Session)] =
    withDBTransaction { implicit connection =>
      val account = accounts.create(Account(registerAccount))
      val session = sessions.create(Session(account.id))

      account -> session
    } {
      case PSQLErrors.UniqueKeyInsert(column, value) =>
        Errors.badRequest.message("Cannot create account!").data("reason", s"'$value' as '$column' is already used!")
    }

  def login(loginAccount: LoginAccount)(implicit ec: ExecutionContext): Future[(Account, Session)] =
    withDBTransaction { implicit connection =>
      accounts.getByEmail(loginAccount.email) match {
        case None =>
          throw Errors.unauthorized("Email or password is invalid!").data("email", loginAccount.email)

        case Some(account) =>
          if (PasswordUtils.hash(loginAccount.password, account.salt) != account.password) {
            throw Errors.unauthorized("Email or password is invalid!").data("email", loginAccount.email)
          } else {
            val session = sessions.create(Session(account.id))
            account -> session
          }
      }
    } {
      case NonFatal(t) =>
        Errors.unauthorized.message("Cannot login!").data("email", loginAccount.email).cause(t)
    }

  def logout(session: Session)(implicit ec: ExecutionContext): Future[Unit] =
    withDB { implicit connection =>
      sessions.delete(session.id)
    } {
      case NonFatal(t) =>
        throw Errors.unauthorized("Cannot logout!").data("token", session.token).cause(t)
    }

  def getByToken(token: String)(implicit ec: ExecutionContext): Future[(Account, Session)] =
    withDB { implicit connection =>
      sessions.getByToken(token) match {
        case None =>
          throw Errors.unauthorized("Session does not exist!")

        case Some(session) =>
          accounts.get(session.accountId) match {
            case None =>
              throw Errors.unauthorized("Account does not exist!")

            case Some(account) =>
              account -> session
          }
      }
    } {
      case NonFatal(t) =>
        throw Errors.unauthorized("Cannot get account and session!").data("token", token).cause(t)
    }
}
