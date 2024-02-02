package maelstrom.echo

import scala.io.StdIn.readLine
import com.eclipsesource.json.{Json, JsonObject}

final case class Request(tpe: String, message: Message)

object Node extends Logging {

  def main(args: Array[String]): Unit = {
    val node = new Node()
    node.accept({ case Request(tpe, request) =>
      log(s"Handle type $tpe for request $request ...");
      val newBody = node.replace(
        request,
        Json
          .`object`()
          .add("type", "echo_ok")
          .add("echo", request.body.getString("echo", ""))
      )
      node.send(Message(node.nodeId, request.src, newBody))
    })
  }
}
class Node() extends Logging {

  var nodeId = ""
  var nodeIds = Seq.empty[String]

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
    val line = readLine()
    val message = Message.from(Json.parse(line))
    message.`type`() match {
      case "init" =>
        nodeId = message.nodeId()
        nodeIds = message.nodeIds()
        val newBody =
          replace(message, Json.`object`().add("type", "init_ok"))
        send(Message(nodeId, message.src, newBody))
      case _ =>
        handle(Request("echo", message))
    }
  }
}
