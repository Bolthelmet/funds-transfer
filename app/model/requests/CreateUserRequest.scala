package model.requests

import model.{FundsEuros, UserId}
import play.api.libs.json._

case class CreateUserRequest (id: UserId, funds: FundsEuros)

object CreateUserRequest{
  implicit val reads: Reads[CreateUserRequest] = new Reads[CreateUserRequest] {
    override def reads(json: JsValue): JsResult[CreateUserRequest] = {
      val userId = (json \ "id").toOption
      val fundsEuros = (json \ "funds_euros").toOption

      (userId, fundsEuros) match {
        case (Some(JsString(id)), Some(JsNumber(funds))) => JsSuccess(
          CreateUserRequest(
            UserId(id),
            FundsEuros(funds)
          )
        )
        case _ => JsError("Fields are missing")
      }
    }
  }

  implicit val writes: Writes[CreateUserRequest] = new Writes[CreateUserRequest] {
    override def writes(o: CreateUserRequest): JsValue = JsObject(
      Map(
        "id" -> JsString(o.id.value),
        "funds_euros" -> JsNumber(o.funds.value)
      )
    )
  }
}