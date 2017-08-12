package exceptions

case class BadRequestException(reason:String) extends Exception {
  override def getMessage: String = reason
}
