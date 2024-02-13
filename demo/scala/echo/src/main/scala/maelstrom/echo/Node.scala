package maelstrom.echo

import scala.io.StdIn.readLine
import com.eclipsesource.json.Json

final case class UnsupportedMessageException(message: String)
    extends RuntimeException(message)

final case class Request(tpe: String, message: Message)

object Node extends Logging {

  def send(message: Message): Unit = {
    val data = message.dump()
    log(s"Message to be sent back: $data")
    System.out.println(data)
    System.out.flush()
  }

  def main(args: Array[String]): Unit = {
    val node = new Node()
    node.accept({ case Request("echo", request) =>
      log(s"Handle message type echo for the request $request ...");
      val replyBody = request newBodyWithMsgId Json
        .`object`()
        .add("type", "echo_ok")
        .add("echo", request.body.getString("echo", ""))
      val replyMsg = Message(node.nodeId, request.src, replyBody)
      send(replyMsg)
      log(
        s"Send message $replyMsg back (from ${node.nodeId} to ${request.src})"
      )
    })
  }
}
class Node extends Logging {

  import Node._

  var nodeId = ""
  private var nodeIds = Seq.empty[String]

  private def accept(
      handle: PartialFunction[Request, Unit]
  ): Unit = LazyList
    .continually(readLine())
    .map { line =>
      log(s"A line read from stdin: $line")
      val message = Message.from(Json.parse(line))
      log(s"Message from the line: $message")
      message.`type`() match {
        case "init" =>
          nodeId = message.nodeId() // n1
          nodeIds = message.nodeIds()
          val replyBody =
            message.newBodyWithMsgId(
              Json
                .`object`()
                .add("type", "init_ok")
            )
          val replyMsg = Message(nodeId, message.src, replyBody)
          send(replyMsg)
          "init"
        case "echo" =>
          handle(Request("echo", message))
          "echo"
        case msg @ _ => throw UnsupportedMessageException(msg)
      }
    }
    .foreach { msg => log(s"Message $msg is handled!") }
}
