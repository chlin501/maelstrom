package maelstrom.echo

import com.eclipsesource.json.{Json, JsonObject, JsonValue}

import scala.jdk.CollectionConverters._

object Message {

  def from(value: String): Message = from(Json.parse(value))

  def from(value: JsonValue): Message = {
    val obj = value.asObject()
    new Message(
      obj.getString("src", ""),
      obj.getString("dest", ""),
      obj.get("body").asObject()
    )
  }

}
final case class Message(src: String, dest: String, body: JsonObject) {

  def `type`(default: String = ""): String =
    body.asObject().getString("type", default)

  private def messageId(): Long = body.asObject().getLong("msg_id", -1L)

  def nodeId(default: String = ""): String =
    body.asObject().getString("node_id", default)

  def newBodyWithMsgId(body: JsonObject): JsonObject =
    body
      .set("in_reply_to", messageId())

  def nodeIds(): Seq[String] = {
    body
      .asObject()
      .get("node_ids")
      .asArray()
      .iterator()
      .asScala
      .map(_.asString())
      .toSeq
  }

  def dump(): String =
    Json
      .`object`()
      .add("src", src)
      .add("dest", dest)
      .add("body", body)
      .toString
}
