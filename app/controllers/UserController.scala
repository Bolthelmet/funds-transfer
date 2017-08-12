package controllers

import javax.inject.Inject

import dao.UserAccountDao
import exceptions.UserAlreadyExistsException
import model.{UserAccount, UserId}
import model.requests.CreateUserRequest
import play.api.libs.json.{JsArray, JsNumber, JsObject, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}


class UserController @Inject()(userAcountDao: UserAccountDao,
                               implicit val ec: ExecutionContext) extends Controller{

  import CreateUserRequest._

  def createUser = Action.async(parse.json[CreateUserRequest]) { implicit req =>
    val account = UserAccount(
      req.body.id,
      req.body.funds
    )
    userAcountDao.insert(account)
      .map(_ => Accepted )
      .recover{
        case UserAlreadyExistsException(_) => Conflict
        case _ => InternalServerError
      }
  }

  def allUsers = Action.async{ implicit req =>

    userAcountDao.all.map{ as =>
      JsArray (
        as.map(Json.toJson(_))
      )
    }
    .map{Ok(_)}

  }

  def getFunds(userId:String) = Action.async { implicit req =>
    userAcountDao.by(UserId(userId))
      .map{ account =>
        Ok(
          Json.toJson(account)
        )
      }
        .recover{
          case _ => NotFound
        }
  }

}
