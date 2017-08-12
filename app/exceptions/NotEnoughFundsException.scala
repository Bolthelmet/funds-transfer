package exceptions

import model.UserId

case class NotEnoughFundsException(userId: UserId) extends Exception{
  override def getMessage: String = s"User with id = ${userId.value} does not have enough funds for transfer"
}
