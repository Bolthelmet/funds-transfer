package functional

import model.{FundsEuros, UserAccount, UserId}
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsString}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import UserAccountTestHelper._

import scala.util.Random

class UserControllerSpec extends FreeSpec with OneAppPerSuite with Matchers{

  val elevenEuro = FundsEuros(10.99)
  val twentyEuro = FundsEuros(20)

  "POST /user_account should" - {
    "return ACCEPTED when everything's fine " in {
      val body = generateAccountBody(elevenEuro)
      val createUserRequest = FakeRequest(POST, s"/user_account")
        .withBody(body)
      val result = route(app, createUserRequest).get

      status(result) shouldBe ACCEPTED
    }

    "return CONFLICT when user with same id exists already" in {
      val body = generateAccountBody(elevenEuro)
      val createUserRequest = FakeRequest(POST, s"/user_account")
        .withBody(body)
      val result = route(app, createUserRequest).get
      status(result) shouldBe ACCEPTED

      val nextResult = route(app, createUserRequest).get
      status(nextResult) shouldBe CONFLICT
    }

    "return BAD_REQUEST when request body is malformed" in {

      val malformedBody = JsObject(
        Map(
          "id" -> JsString("xxx")
        )
      )

      val createUserRequest = FakeRequest(POST, s"/user_account")
        .withBody(malformedBody)
      val result = route(app, createUserRequest).get
      status(result) shouldBe BAD_REQUEST

    }
  }

  "GET /users should" - {

    "return list of users" in {
      val body1 = generateAccountBody(elevenEuro)
      val body2 = generateAccountBody(twentyEuro)
      val createUserRequest1 = FakeRequest(POST, s"/user_account")
        .withBody(body1)
      val result1 = route(app, createUserRequest1).get

      val createUserRequest2 = FakeRequest(POST, s"/user_account")
        .withBody(body2)
      val result2 = route(app, createUserRequest2).get


      val getUsersRequest = FakeRequest(GET, s"/users")
      val usersResult = route(app, getUsersRequest).get

      val userFunds = contentAsJson(usersResult).as[JsArray].value.map(_.as[UserAccount]).map(_.funds)
      userFunds should contain allOf (elevenEuro, twentyEuro)

    }

  }


  "GET /funds/<userId> should" - {

    "return user funds" in {
      val body = generateAccountBody(elevenEuro)
      val userId = (body \ "id").get.as[String]
      val createUserRequest = FakeRequest(POST, s"/user_account")
        .withBody(body)
      val result = route(app, createUserRequest).get


      val getFundsRequest = FakeRequest(GET, s"/funds/${userId}")
        .withBody(body)
      val fundsResult = route(app, getFundsRequest).get

      contentAsJson(fundsResult) shouldBe JsObject(
        Map(
          "id" -> JsString(userId),
          "funds_euros" -> JsNumber(elevenEuro.value)
        )
      )

    }

    "return NOT_FOUND when there's no user with this id" in {
      val getFundsRequest = FakeRequest(GET, s"/funds/someId")
      val fundsResult = route(app, getFundsRequest).get
      status(fundsResult) shouldBe NOT_FOUND
    }

  }

}

object UserAccountTestHelper {

  def generateAccountBody(fundsEuros: FundsEuros) = {
    val id = Random.nextString(20)

    JsObject(
      Map(
        "id" -> JsString(id),
        "funds_euros" -> JsNumber(fundsEuros.value)
      )
    )
  }
}
