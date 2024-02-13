package maelstrom.echo

import com.eclipsesource.json.Json
import org.scalatest._
import flatspec._
import org.scalatest.matchers.should._

class MessageSpec extends AnyFlatSpec with Matchers {

  "Init message" should "match replied init message" in {
    val expectedReplyStr =
      """{"src":"n1","dest":"c0","body":{"type":"init_ok","in_reply_to":1}}"""

    val cltInitStr =
      """{"id":0,"src":"c0","dest":"n1","body":{"type":"init","node_id":"n1","node_ids":["n1"],"msg_id":1}}"""
    val cltInitMsg = Message.from(cltInitStr)
    val replyBody =
      cltInitMsg newBodyWithMsgId Json.`object`().add("type", "init_ok")
    val replyMsg =
      Message(cltInitMsg.nodeId(), cltInitMsg.src, body = replyBody)
    assertResult(expectedReplyStr)(replyMsg.dump())
  }

  "Echo message" should "match replied echo message" in {
    val expectedReplyStr =
      """{"src":"n1","dest":"c2","body":{"type":"echo_ok","in_reply_to":44}}"""

    val cltEchoStr =
      """{"id":88,"src":"c2","dest":"n1","body":{"echo":"Please echo 28","type":"echo","msg_id":44}}"""
    val cltEchoMsg = Message.from(cltEchoStr)
    val replyBody =
      cltEchoMsg newBodyWithMsgId Json.`object`().add("type", "echo_ok")
    val replyMsg =
      Message(cltEchoMsg.dest, cltEchoMsg.src, body = replyBody)
    assertResult(expectedReplyStr)(replyMsg.dump())
  }
}
