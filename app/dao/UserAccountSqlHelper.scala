package dao

import java.sql.Connection

import anorm.SqlParser._
import anorm.{SqlParser, ~, _}
import exceptions.UserNotFoundException
import model.{FundsEuros, UserAccount, UserId}

import scala.util.Try

trait UserAccountSqlHelper {

  def user(id: UserId)(implicit connection: Connection): Try[UserAccount]

  def insertSql(userAccount: UserAccount)(implicit connection: Connection): Try[UserAccount]

  def all(implicit connection: Connection): Try[Seq[UserAccount]]

  def updateSql(userAccount: UserAccount)(implicit connection: Connection): Try[UserAccount]

}

class UserAccountSqlHelperImpl extends UserAccountSqlHelper {

  override def user(id: UserId)(implicit connection: Connection): Try[UserAccount] = Try {
    SQL(
      """
        |SELECT * FROM user_account
        |WHERE id = {id}
      """.stripMargin
    ).on(
      "id" -> id.value
    )
      .executeQuery()
      .as(parser.*).head
  }


  override def all(implicit connection: Connection): Try[Seq[UserAccount]] = Try {
    SQL(
      """
        |SELECT * FROM user_account
        |ORDER BY id
      """.stripMargin
    ) .executeQuery()
      .as(parser.*)
  }

  override def insertSql(userAccount: UserAccount)(implicit connection: Connection): Try[UserAccount] = Try{
    SQL(
      """
        |INSERT INTO user_account (id, funds_euro)
        |VALUES ({id}, {fundsEuro})
      """.stripMargin
    ).on(
      "id" -> userAccount.id.value,
      "fundsEuro" -> userAccount.funds.value
    ).executeInsert()
    userAccount
  }

  override def updateSql(userAccount: UserAccount)(implicit connection: Connection): Try[UserAccount]= {
    val updatedRowsQty = SQL(
      """
        |UPDATE user_account
        |SET funds_euro = {newFunds}
        |WHERE id = {id}
      """.stripMargin
    ).on(
      "id" -> userAccount.id.value,
      "newFunds" -> userAccount.funds.value
    ).executeUpdate()

    updatedRowsQty match {
      case 1 => scala.util.Success(userAccount)
      case 0 => scala.util.Failure(throw UserNotFoundException(userAccount.id))
      case _ => scala.util.Failure(throw new Exception("Inconsistent db state"))
    }

  }

  private val parser: RowParser[UserAccount] = (
    SqlParser.str("id") ~
      get[BigDecimal]("funds_euro")
    ).map {
    case id ~ funds =>
      UserAccount(UserId(id), FundsEuros(funds))
  }
}
