package maelstrom.echo

import scala.io.StdIn.readLine
import com.eclipsesource.json.{Json, JsonObject}
import maelstrom.{Logging, Message, Node}
import maelstrom.Message._
import maelstrom.Node._

object Echo extends Logging {

  def main(args: Array[String]): Unit = {
    val node = Node[Echo]
    node.accept { case Request(Message.Echo, request) =>
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
    }
  }
}
class Echo extends Node[Echo] with Logging {
  override def accept(
      handle: PartialFunction[Request, Unit]
  ): Unit = LazyList
    .continually(readLine())
    .map { line =>
      log(s"Read from stdin: $line")
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
        case Message.Echo =>
          handle(Request(Message.Echo, request))
          Echo
      }
    }
    .foreach { msgType => log(s"Message $msgType is handled!") }
}
