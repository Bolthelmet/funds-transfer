package dao

import java.sql.SQLException
import javax.inject.Inject

import exceptions.UserAlreadyExistsException
import model.{UserAccount, UserId}
import org.h2.jdbc.JdbcSQLException
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait UserAccountDao {

  def insert(account: UserAccount): Future[UserAccount]
  def by(userId: UserId): Future[UserAccount]
  def all: Future[Seq[UserAccount]]
}

class UserAccountDaoImpl @Inject()(db: Database,
                                   userAccountDbHelper: UserAccountSqlHelper,
                                   implicit val ec: ExecutionContext //todo adjust ExecutionContext to database needs if needed
                                  ) extends UserAccountDao {
  lazy val uniqueViolationSqlCode = 23505


  override def all: Future[Seq[UserAccount]] = Future {
    db.withConnection{ implicit connection =>
      userAccountDbHelper.all match {
        case Success(x) => x
        case Failure(e) => throw e
      }
    }
  }

  override def insert(account: UserAccount): Future[UserAccount] = Future {
    db.withConnection { implicit connection =>
      userAccountDbHelper.insertSql(account) match {
        case Success(r) => r
        case Failure(e: JdbcSQLException) if e.getErrorCode == uniqueViolationSqlCode =>
          throw UserAlreadyExistsException(account.id)
        case Failure(e) => throw e
      }
    }
  }

  override def by(userId: UserId): Future[UserAccount] = Future{
    db.withConnection{ implicit connection =>
      userAccountDbHelper.user(userId) match {
        case Success(r) => r
        case Failure(e) => throw e
      }
    }
  }
}
