package functional

import java.sql.Connection

import dao.FundsTransferSqlHelper
import functional.FundTransferTestHelper._
import model.{FundsEuros, FundsTransfer, UserAccount, UserId}
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.play.OneAppPerSuite
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.util.{Failure, Random, Try}

class FailingTrackingTransferSpec extends FreeSpec with OneAppPerSuite with Matchers {


  override lazy val app = (new GuiceApplicationBuilder()).overrides(
    inject.bind[FundsTransferSqlHelper].to[FailingFundsTransferSqlHelper]
  ).build()

  "POST /transfer_funds should not transfer funds if it cannot put tracking to DB" in {

    val sender = UserAccount(
      UserId(Random.nextString(20)),
      FundsEuros(100)
    )

    val reciever = UserAccount(
      UserId(Random.nextString(20)),
      FundsEuros(0)
    )

    createUsersAndTransferMoney(app)(sender, reciever, FundsEuros(80)) shouldBe INTERNAL_SERVER_ERROR

    val senderAccountAfterTransferReq = FakeRequest(GET, s"/funds/${sender.id.value}")
    val senderAccountAfterTransferRes = route(app, senderAccountAfterTransferReq).get
    contentAsJson(senderAccountAfterTransferRes).as[UserAccount] shouldBe sender.copy(funds = FundsEuros(100))

    val recieverAccountAfterTransferReq = FakeRequest(GET, s"/funds/${reciever.id.value}")
    val recieverAccountAfterTransferRes = route(app, recieverAccountAfterTransferReq).get
    contentAsJson(recieverAccountAfterTransferRes).as[UserAccount] shouldBe reciever.copy(funds = FundsEuros(0))

  }
}

class FailingFundsTransferSqlHelper extends FundsTransferSqlHelper {
  override def insertSql(fundsTransfer: FundsTransfer)(implicit connection: Connection): Try[FundsTransfer] = {
    Failure(new Exception("sorry"))
  }

  override def all(implicit connection: Connection): Try[Seq[FundsTransfer]] = ???
}

