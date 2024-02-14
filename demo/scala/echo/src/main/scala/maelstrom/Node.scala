package maelstrom

import com.eclipsesource.json.JsonObject
import maelstrom.Message.ReceivedType
import maelstrom.Node.Request
import maelstrom.echo.Echo

trait Node[T] {
  def accept(handler: PartialFunction[Request, Unit]): Unit
}
object Node extends Logging {

  def apply[T](implicit instance: Node[T]): Node[T] = instance

  final case class Request(msgType: ReceivedType, message: Message)

  private def send(message: Message): Unit = {
    val data = message.dump()
    log(s"Message to be sent back: $data")
    System.out.println(data)
    System.out.flush()
  }

  def reply(src: String, dest: String, body: JsonObject): Message = {
    val msg = Message(src, dest, body)
    send(msg)
    msg
  }

  implicit val echo: Echo = new Echo
}
