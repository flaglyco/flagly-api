package co.flagly.api.account

import cats.effect.IO
import co.flagly.api.common.Errors
import co.flagly.api.common.base.Service
import co.flagly.api.session.{Session, SessionRepository}
import co.flagly.api.utilities.IOExtensions._
import co.flagly.api.utilities.PasswordUtils
import play.api.db.Database

class AccountService(accounts: AccountRepository, sessions: SessionRepository, db: Database) extends Service(db) {
  def create(registerAccount: RegisterAccount): IO[(Account, Session)] =
    withDBTransaction { implicit connection =>
      for {
        account <- accounts.create(Account(registerAccount))
        session <- sessions.create(Session(account.id))
      } yield {
        account -> session
      }
    }

  def login(loginAccount: LoginAccount): IO[(Account, Session)] =
    withDBTransaction { implicit connection =>
      lazy val e = Errors.unauthorized("Email or password is invalid!").data("email", loginAccount.email)

      for {
        account <- accounts.getByEmail(loginAccount.email) ifNoneE e
        session <- if (!PasswordUtils.isValid(account, loginAccount.password)) {
                     IO.raiseError(e)
                   } else {
                     sessions.create(Session(account.id))
                   }
      } yield {
        account -> session
      }
    }

  def logout(session: Session): IO[Unit] =
    withDB { implicit connection =>
      sessions.delete(session.id)
    }

  def getByToken(token: String): IO[(Account, Session)] =
    withDB { implicit connection =>
      for {
        session <- sessions.getByToken(token)      ifNoneE Errors.unauthorized("Session does not exist!")
        account <- accounts.get(session.accountId) ifNoneE Errors.unauthorized("Account does not exist!")
      } yield {
        account -> session
      }
    }
}
