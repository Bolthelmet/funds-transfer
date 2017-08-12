package exceptions

import model.UserId

case class UserNotFoundException (userId: UserId) extends Exception {
  override def getMessage: String = s"User with id = $userId was not found"
}
