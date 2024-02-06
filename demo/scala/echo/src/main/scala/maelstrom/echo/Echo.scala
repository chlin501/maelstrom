package maelstrom.echo

import scala.io.StdIn.readLine
import com.eclipsesource.json.{Json, JsonObject}

final case class UnsupportedMessageException(message: String)
    extends RuntimeException(message)

final case class Request(tpe: String, message: Message)

object Node extends Logging {

  def main(args: Array[String]): Unit = {
    val node = new Node()
    node.accept({ case Request("echo", request) =>
      log(s"Handle message type echo for the request $request ...");
      val newBody = node.replace(
        request,
        Json
          .`object`()
          .add("type", "echo_ok")
          .add("echo", request.body.getString("echo", ""))
      )
      val message = Message(node.nodeId, request.src, newBody)
      node.send(message)
      log(s"Send message $message back (from ${node.nodeId} to ${request.src})")
    })
  }
}
class Node() extends Logging {

  var nodeId = ""
  private var nodeIds = Seq.empty[String]

  private def send(message: Message): Unit = {
    val data = message.dump()
    System.out.println(data)
    System.out.flush()
  }

  def replace(request: Message, body: JsonObject): JsonObject =
    Json
      .`object`()
      .merge(body)
      .set("in_reply_to", request.messageId())

  private def accept(
      handle: PartialFunction[Request, Unit]
  ): Unit = {
    val input = LazyList.continually(readLine())
    input
      .map { line =>
        log(s"A line read from stdin: $line")
        val message = Message.from(Json.parse(line))
        log(s"Message from the line: $message")
        message.`type`() match {
          case "init" =>
            nodeId = message.nodeId() // n1
            nodeIds = message.nodeIds()
            val newBody =
              replace(message, Json.`object`().add("type", "init_ok"))
            send(Message(nodeId, message.src, newBody))
            "init"
          case "echo" =>
            handle(Request("echo", message))
            "echo"
          case msg @ _ => throw UnsupportedMessageException(msg)
        }
      }
      .foreach { msg => log(s"Message $msg is handled!") }
  }
}
