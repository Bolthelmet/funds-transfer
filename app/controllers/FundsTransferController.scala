package controllers

import javax.inject.Inject

import dao.FundsTransferDao
import exceptions.{BadRequestException, NotEnoughFundsException, UserNotFoundException}
import model.requests.TransferFundsRequest
import model.{FundsTransfer, UserId}
import play.api.libs.json.{JsArray, JsString, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

class FundsTransferController @Inject()(fundsTransferDao: FundsTransferDao,
                                        implicit val ec: ExecutionContext) extends Controller {

  import TransferFundsRequest._

  def transferFunds = Action.async(parse.json[TransferFundsRequest]) { implicit req =>

    val senderId = Future {
      req.headers.get("user_id").map(UserId).getOrElse(throw new BadRequestException("No user_id header present in request"))
    }

    val body = req.body

    senderId.map { s =>
      FundsTransfer(
        s,
        body.reciever,
        body.amount
      )
    }
      .flatMap(fundsTransferDao.saveTransfer)
      .map { _ =>
        Accepted
      }
      .recover {
        case UserNotFoundException(_) => NotFound
        case NotEnoughFundsException(_) => BadRequest(JsString("Not enough funds"))
        case BadRequestException(r) => BadRequest(JsString(r))
        case e => InternalServerError(JsString("Could not log the transfer, transfer has not pass"))
      }

  }

  def allTransfers = Action.async { req =>
    fundsTransferDao.all.map { transfers =>
      JsArray(
        transfers.map(z => Json.toJson(z))
      )
    }
      .map { Ok(_)}
      .recover {
        case _ => BadRequest
      }

  }

}