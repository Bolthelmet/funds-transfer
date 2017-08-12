package dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import model.{FundsEuros, FundsTransfer, UserId}

import scala.util.Try

trait FundsTransferSqlHelper {
  def insertSql(fundsTransfer: FundsTransfer)(implicit connection: Connection): Try[FundsTransfer]

  def all(implicit connection: Connection): Try[Seq[FundsTransfer]]
}

class FundsTransferSqlHelperImpl extends FundsTransferSqlHelper {

  override def all(implicit connection: Connection): Try[Seq[FundsTransfer]] = Try {
    SQL(
      """
        |SELECT * FROM fund_transaction
      """.stripMargin
    ).as(parser.*)
  }

  override def insertSql(fundsTransfer: FundsTransfer)(implicit connection: Connection): Try[FundsTransfer] = {
    Try {
      val r = SQL(
        """
          |INSERT INTO fund_transaction(sender, reciever, funds_euro) VALUES ({sender_id}, {reciever_id}, {funds_euro})
        """.stripMargin
      ).on(
        "sender_id" -> fundsTransfer.sender.value,
        "reciever_id" -> fundsTransfer.reciever.value,
        "funds_euro" -> fundsTransfer.amount.value
      ).executeInsert()
      fundsTransfer
    }
  }

  private val parser: RowParser[FundsTransfer] = (
    SqlParser.str("sender") ~
      SqlParser.str("reciever") ~
      get[BigDecimal]("funds_euro")
    ).map {
    case sender ~ reciever ~ funds =>
      FundsTransfer(UserId(sender), UserId(reciever), FundsEuros(funds))
  }
}
