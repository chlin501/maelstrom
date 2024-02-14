package maelstrom.echo

import com.eclipsesource.json.{Json, JsonArray}
import org.scalatest._
import flatspec._
import maelstrom.Message
import org.scalatest.matchers.should._

import scala.jdk.CollectionConverters.IteratorHasAsScala

class MessageSpec extends AnyFlatSpec with Matchers {

  "Message" should "create a new immutable json object with body and message id" in {
    val body = Json
      .`object`()
      .add("type", "init")
      .add("node_id", "n1")
      .add("node_ids", Json.parse("""["n1"]""").asArray())
      .add("msg_id", 1)
    val msg = Message("c0", "n1", body)
    val newBody = msg.newBodyWithMsgId(body)
    newBody.iterator().asScala.foreach { e =>
      val (expected, actual) = e.getName match {
        case "in_reply_to" =>
          ("", body.getString("in_reply_to", ""))
        case fieldName @ _ if e.getValue.isString =>
          val actual =
            e.getValue.toString.stripPrefix(""""""").stripSuffix(""""""")
          val expected = body.getString(fieldName, "")
          (expected, actual)
        case fieldName @ _ if e.getValue.isNumber =>
          val actual = e.getValue.asInt
          val expected = body.getInt(fieldName, -1)
          (expected, actual)
        case _ @_ if e.getValue.isArray =>
          val actual =
            e.getValue.asArray().iterator().asScala.toSeq.map(_.asString())
          (Seq("n1"), actual)
      }
      assertResult(expected)(actual)
    }
  }

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
