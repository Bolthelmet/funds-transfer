package model

import play.api.libs.json._

case class FundsTransfer (sender: UserId,
                          reciever: UserId,
                          amount: FundsEuros)

object FundsTransfer {

  implicit val writes: Writes[FundsTransfer] = new Writes[FundsTransfer] {
    override def writes(o: FundsTransfer): JsValue = JsObject(
      Map(
        "sender_id" -> JsString(o.sender.value),
        "reciever_id" -> JsString(o.reciever.value),
        "funds_euros" -> JsNumber(o.amount.value)
      )
    )
  }

  implicit  val reads: Reads[FundsTransfer] = new Reads[FundsTransfer] {
    override def reads(json: JsValue): JsResult[FundsTransfer] = {
      val success = for {
        senderId <- (json \ "sender_id").toOption.map(_.as[String])
        recieverId <- (json \ "reciever_id").toOption.map(_.as[String])
        funds <- (json \ "funds_euros").toOption.map(_.as[BigDecimal])
      } yield {
        JsSuccess(
          FundsTransfer(
            UserId(senderId),
            UserId(recieverId),
            FundsEuros(funds)
          )
        )
      }

      success.getOrElse(JsError("Some fields are missing"))
    }
  }
}