package model

import play.api.libs.json._

case class UserAccount(id: UserId,
                       funds: FundsEuros)

object UserAccount {
  implicit val reads: Reads[UserAccount] = new Reads[UserAccount] {
    override def reads(json: JsValue): JsResult[UserAccount] = {
      val id = (json \ "id").toOption
      val funds = (json \ "funds_euros").toOption

      (id, funds) match {
        case (Some(JsString(id)), Some(JsNumber(funds))) => JsSuccess(
          UserAccount(
            UserId(id),
            FundsEuros(funds)
          )
        )
        case _ => JsError("Some field is missing")
      }
    }
  }

  implicit val wtires: Writes[UserAccount] = new Writes[UserAccount] {
    override def writes(o: UserAccount): JsValue = JsObject{
      Map(
        "id" -> JsString(o.id.value),
        "funds_euros" -> JsNumber(o.funds.value)
      )
    }
  }
}
