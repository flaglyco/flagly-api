package co.flagly.api.services

import co.flagly.api.models.{Account, Session}
import co.flagly.api.repositories.{AccountRepository, SessionRepository}
import co.flagly.api.utilities.PSQLErrors
import co.flagly.api.views.CreateAccount
import co.flagly.core.FlaglyError
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AccountService(accounts: AccountRepository, sessions: SessionRepository, db: Database) extends BaseService(db) {
  def create(createAccount: CreateAccount)(implicit ec: ExecutionContext): Future[(Account, Session)] =
    withDBTransaction { implicit connection =>
      val account = accounts.create(Account(createAccount))
      val session = sessions.create(Session(account.id))

      account -> session
    } {
      case PSQLErrors.UniqueKeyInsert(column, value) =>
        FlaglyError.of(s"Cannot create account because '$column' as '$value' is already used!")
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
