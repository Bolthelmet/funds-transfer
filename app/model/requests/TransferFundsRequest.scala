package model.requests

import model.{FundsEuros, UserId}
import play.api.libs.json._

case class TransferFundsRequest (reciever: UserId,
                                 amount: FundsEuros)

object TransferFundsRequest{

  implicit val reads: Reads[TransferFundsRequest] = new Reads[TransferFundsRequest] {
    override def reads(json: JsValue): JsResult[TransferFundsRequest] = {

      val reciever = (json \ "reciever_id").toOption
      val fundsEuros = (json \ "funds_euros").toOption

      (reciever, fundsEuros) match {
        case (Some(JsString(recieverId)), Some(JsNumber(funds))) => JsSuccess(
          TransferFundsRequest(
            UserId(recieverId),
            FundsEuros(funds)
          )
        )
        case _ => JsError("Fields are missing")
      }

    }
  }
}
