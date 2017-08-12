package dao

import java.sql.Connection
import javax.inject.Inject

import exceptions.{NotEnoughFundsException, UserNotFoundException}
import model.{FundsEuros, FundsTransfer, UserAccount}
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait FundsTransferDao {

  def saveTransfer(transfer: FundsTransfer): Future[FundsTransfer]
  def all: Future[Seq[FundsTransfer]]

}

class FundsTransferDaoImpl @Inject()(db: Database,
                                     fundsTransferSqlHelper: FundsTransferSqlHelper,
                                     userAccountDbHelper: UserAccountSqlHelper,
                                     implicit val ec: ExecutionContext //todo adjust ExecutionContext to database needs if needed
                                    ) extends FundsTransferDao {


  override def all: Future[Seq[FundsTransfer]] = Future{
    db.withConnection{ implicit connection =>
      fundsTransferSqlHelper.all match {
        case Success(x) => x
        case Failure(e) => throw e
      }
    }
  }

  override def saveTransfer(transfer: FundsTransfer): Future[FundsTransfer] = {

    Future{
      db.withTransaction { implicit connection =>

        fundsTransferSqlHelper.insertSql(transfer)
          .flatMap {
            senderThatHasEnough
          }
          .flatMap { s =>
            moveFunds(s)(transfer)
          }
        match {
          case Success(x) => x
          case Failure(e) => throw e
        }
      }
    }

  }

  private def senderThatHasEnough(transfer: FundsTransfer)(implicit connection: Connection): Try[UserAccount] = {
    val senderAcc = userAccountDbHelper.user(transfer.sender)
    senderAcc
      .orElse(Failure(UserNotFoundException(transfer.sender)))
      .filter{ s =>
        s.funds.value >= transfer.amount.value
      }
      .orElse(Failure(NotEnoughFundsException(transfer.sender)))
  }

  private def moveFunds(sender: UserAccount)(transfer: FundsTransfer)(implicit connection: Connection): Try[FundsTransfer] = {
    userAccountDbHelper.user(transfer.reciever)
      .flatMap { reciever =>
        addFundsTo(reciever, transfer.amount)
      }.flatMap{ _ =>
        takeFundsFrom(sender, transfer.amount)
      }.map{ _ =>
        transfer
      }

  }

  private def addFundsTo(user: UserAccount, fundsEuros: FundsEuros)(implicit connection: Connection) = {
    val updatedAcc = user.copy(
      funds = FundsEuros(
        user.funds.value + fundsEuros.value
      )
    )
    userAccountDbHelper.updateSql(updatedAcc)

  }

  private def takeFundsFrom(userAccount: UserAccount, fundsEuros: FundsEuros)(implicit connection: Connection) =
    addFundsTo(
      userAccount,
      FundsEuros(fundsEuros.value.unary_-)
    )

}
