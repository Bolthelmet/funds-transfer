package exceptions

import model.UserId

case class UserAlreadyExistsException(id: UserId) extends Exception{
  override def getMessage: String = s"User account with id = ${id.value} already exists"
}
