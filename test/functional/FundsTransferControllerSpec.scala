package functional

import dao.{FundsTransferSqlHelper, UserAccountSqlHelper}
import model.{FundsEuros, FundsTransfer, UserAccount, UserId}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import model.requests.CreateUserRequest._

import scala.util.Random
import UserAccount._
import model.requests.CreateUserRequest
import play.api.Application

class FundsTransferControllerSpec extends FreeSpec with OneAppPerSuite with Matchers{

  "POST /transfer_funds should transfer funds and put tracking to DB" in {


    val sender = UserAccount(
      UserId(Random.nextString(20)),
      FundsEuros(100)
    )

    val reciever = UserAccount(
      UserId(Random.nextString(20)),
      FundsEuros(0)
    )

    FundTransferTestHelper.createUsersAndTransferMoney(app)(sender, reciever, FundsEuros(80)) shouldBe ACCEPTED

    val senderAccountAfterTransferReq = FakeRequest(GET, s"/funds/${sender.id.value}")
    val senderAccountAfterTransferRes = route(app, senderAccountAfterTransferReq).get
    contentAsJson(senderAccountAfterTransferRes).as[UserAccount] shouldBe sender.copy(funds = FundsEuros(20))

    val recieverAccountAfterTransferReq = FakeRequest(GET, s"/funds/${reciever.id.value}")
    val recieverAccountAfterTransferRes = route(app, recieverAccountAfterTransferReq).get
    contentAsJson(recieverAccountAfterTransferRes).as[UserAccount] shouldBe reciever.copy(funds = FundsEuros(80))

    val allTransfersRequest = FakeRequest(GET, "/transfer_funds")
    val allTransfersResult = route(app, allTransfersRequest).get

    import FundsTransfer._
    val allTransfers = contentAsJson(allTransfersResult).as[Seq[FundsTransfer]]

    allTransfers should contain (FundsTransfer(
      sender.id,
      reciever.id,
      FundsEuros(80)
    ))
  }

  "POST /transfer_funds should return BadRequest when not enough funds" in {

    def getAllTransfers = {
      val allTransfersRequest = FakeRequest(GET, "/transfer_funds")
      val allTransfersResult = route(app, allTransfersRequest).get

      import FundsTransfer._
      contentAsJson(allTransfersResult).as[Seq[FundsTransfer]]
    }

    val sender = UserAccount(
      UserId(Random.nextString(20)),
      FundsEuros(100)
    )

    val reciever = UserAccount(
      UserId(Random.nextString(20)),
      FundsEuros(0)
    )

    FundTransferTestHelper.createUsersAndTransferMoney(app)(sender, reciever, FundsEuros(180)) shouldBe BAD_REQUEST

    val senderAccountAfterTransferReq = FakeRequest(GET, s"/funds/${sender.id.value}")
    val senderAccountAfterTransferRes = route(app, senderAccountAfterTransferReq).get
    contentAsJson(senderAccountAfterTransferRes).as[UserAccount] shouldBe sender.copy(funds = FundsEuros(100))

    val recieverAccountAfterTransferReq = FakeRequest(GET, s"/funds/${reciever.id.value}")
    val recieverAccountAfterTransferRes = route(app, recieverAccountAfterTransferReq).get
    contentAsJson(recieverAccountAfterTransferRes).as[UserAccount] shouldBe reciever.copy(funds = FundsEuros(0))

    getAllTransfers should not contain (FundsTransfer(
      sender.id,
      reciever.id,
      FundsEuros(180)
    ))
  }

  "POST /transfer_funds should return BadRequest when not sender header" in {
    val body = JsObject(
      Map(
        "reciever_id" -> JsString(""),
        "funds_euros" -> JsNumber(180)
      )
    )

    val request = FakeRequest(POST, "/transfer_funds")
      .withBody(body)

    val result = route(app, request).get
    status(result) shouldBe BAD_REQUEST
  }




//  private def createUserRequest(userAccount: UserAccount) = CreateUserRequest(
//    userAccount.id,
//    userAccount.funds
//  )
//
//  private def createUsersAndTransferMoney(sender: UserAccount,
//                                          reciever: UserAccount,
//                                          fundsEuros: FundsEuros) = {
//
//    val createSenderRequest = FakeRequest(POST, s"/user_account")
//      .withBody(
//        Json.toJson(
//          createUserRequest(sender)
//        )
//      )
//    val createSenderResult = route(app, createSenderRequest).get
//
//    status(createSenderResult) shouldBe ACCEPTED
//
//
//    val createRecieverRequest = FakeRequest(POST, s"/user_account")
//      .withBody(
//        Json.toJson(
//          createUserRequest(reciever)
//        )
//      )
//    val createRecieverResult = route(app, createRecieverRequest).get
//
//    status(createRecieverResult) shouldBe ACCEPTED
//
//    val body = JsObject(
//      Map(
//        "reciever_id" -> JsString(reciever.id.value),
//        "funds_euros" -> JsNumber(fundsEuros.value)
//      )
//    )
//
//    val transferRequest = FakeRequest(POST, "/transfer_funds")
//      .withBody(body)
//      .withHeaders(
//        "user_id" -> sender.id.value
//      )
//
//    val result = route(app, transferRequest).get
//    status(result)
//
//  }
}

object FundTransferTestHelper extends Matchers {
  private def createUserRequest(userAccount: UserAccount) = CreateUserRequest(
    userAccount.id,
    userAccount.funds
  )

  def createUsersAndTransferMoney(app: Application)(sender: UserAccount,
                                          reciever: UserAccount,
                                          fundsEuros: FundsEuros) = {

    val createSenderRequest = FakeRequest(POST, s"/user_account")
      .withBody(
        Json.toJson(
          createUserRequest(sender)
        )
      )
    val createSenderResult = route(app, createSenderRequest).get

    status(createSenderResult) shouldBe ACCEPTED


    val createRecieverRequest = FakeRequest(POST, s"/user_account")
      .withBody(
        Json.toJson(
          createUserRequest(reciever)
        )
      )
    val createRecieverResult = route(app, createRecieverRequest).get

    status(createRecieverResult) shouldBe ACCEPTED

    val body = JsObject(
      Map(
        "reciever_id" -> JsString(reciever.id.value),
        "funds_euros" -> JsNumber(fundsEuros.value)
      )
    )

    val transferRequest = FakeRequest(POST, "/transfer_funds")
      .withBody(body)
      .withHeaders(
        "user_id" -> sender.id.value
      )

    val result = route(app, transferRequest).get
    status(result)

  }
}
