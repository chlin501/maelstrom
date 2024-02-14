package maelstrom.echo

import scala.io.StdIn.readLine
import com.eclipsesource.json.{Json, JsonObject}
import maelstrom.echo.Message._

object Node extends Logging {

  private final case class Request(msgType: ReceivedType, message: Message)

  private def send(message: Message): Unit = {
    val data = message.dump()
    log(s"Message to be sent back: $data")
    System.out.println(data)
    System.out.flush()
  }

  private def reply(src: String, dest: String, body: JsonObject): Message = {
    val msg = Message(src, dest, body)
    send(msg)
    msg
  }

  def main(args: Array[String]): Unit = {
    val node = new Node()
    node.accept({ case Request(Echo, request) =>
      log(s"Handle message type 'echo' for the request $request ...");
      val replyMsg = reply(
        request.dest,
        request.src,
        request newBodyWithMsgId Json
          .`object`()
          .add("type", EchoOk.toString())
          .add("echo", request.body.getString("echo", ""))
      )
      log(
        s"Send message back from ${request.dest} $replyMsg to ${request.src}"
      )
    })
  }
}
class Node extends Logging {

  import Node._
  import Message._

  private def accept(
      handle: PartialFunction[Request, Unit]
  ): Unit = LazyList
    .continually(readLine())
    .map { line =>
      log(s"A line read from stdin: $line")
      val request = Message.from(Json.parse(line))
      log(s"Message from the line: $request")
      request.`type`() match {
        case Init =>
          val nodeId = request.nodeId()
          val _ = request.nodeIds()
          reply(
            nodeId,
            request.src,
            request.newBodyWithMsgId(
              Json
                .`object`()
                .add("type", InitOk.toString())
            )
          )
          Init
        case Echo =>
          handle(Request(Echo, request))
          Echo
      }
    }
    .foreach { msgType => log(s"Message $msgType is handled!") }
}
