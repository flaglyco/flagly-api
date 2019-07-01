package co.flagly.api.services

import co.flagly.api.auth.PasswordUtils
import co.flagly.api.models.{Account, Session}
import co.flagly.api.repositories.{AccountRepository, SessionRepository}
import co.flagly.api.utilities.PSQLErrors
import co.flagly.api.views.{RegisterAccount, LoginAccount}
import co.flagly.core.FlaglyError
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AccountService(accounts: AccountRepository, sessions: SessionRepository, db: Database) extends BaseService(db) {
  def logout(session: Session)(implicit ec: ExecutionContext): Future[Unit] =
    withDB { implicit connection =>
      sessions.delete(session.id)
    } {
      case NonFatal(t) =>
        throw FlaglyError.of(s"Cannot logout session by token '${session.token}'!", t)
    }

  def create(registerAccount: RegisterAccount)(implicit ec: ExecutionContext): Future[(Account, Session)] =
    withDBTransaction { implicit connection =>
      val account = accounts.create(Account(registerAccount))
      val session = sessions.create(Session(account.id))

      account -> session
    } {
      case PSQLErrors.UniqueKeyInsert(column, value) =>
        FlaglyError.of(s"Cannot create account because '$column' as '$value' is already used!")
    }

  def login(loginAccount: LoginAccount)(implicit ec: ExecutionContext): Future[(Account, Session)] =
    withDBTransaction { implicit connection =>
      accounts.getByEmail(loginAccount.email) match {
        case None =>
          throw FlaglyError.of(s"Cannot login account '${loginAccount.email}' because email or password is invalid!")

        case Some(account) =>
          if (PasswordUtils.hash(loginAccount.password, account.salt) != account.password) {
            throw FlaglyError.of(s"Cannot login account '${loginAccount.email}' because email or password is invalid!")
          } else {
            val session = sessions.create(Session(account.id))
            account -> session
          }
      }
    } {
      case NonFatal(t) =>
        FlaglyError.of(s"Cannot login account '${loginAccount.email}'!", t)
    }

  def getByToken(token: String)(implicit ec: ExecutionContext): Future[(Account, Session)] =
    withDB { implicit connection =>
      sessions.getByToken(token) match {
        case None =>
          throw FlaglyError.of(401, s"Session does not exist!")

        case Some(session) =>
          accounts.get(session.accountId) match {
            case None =>
              throw FlaglyError.of(401, s"Account '${session.accountId}' does not exist!")

            case Some(account) =>
              account -> session
          }
      }
    } {
      case NonFatal(t) =>
        throw FlaglyError.of(s"Cannot get account and session by token '$token'!", t)
    }
}
