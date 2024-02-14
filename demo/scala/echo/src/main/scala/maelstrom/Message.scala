package maelstrom

import com.eclipsesource.json.{Json, JsonObject, JsonValue}

import scala.jdk.CollectionConverters._

object Message {

  sealed trait Type
  sealed trait ReceivedType extends Type
  sealed trait ReplyType extends Type
  final case object Init extends ReceivedType {
    override def toString(): String = "init"
  }
  final case object Echo extends ReceivedType {
    override def toString(): String = "echo"
  }
  final case object InitOk extends ReplyType {
    override def toString(): String = "init_ok"
  }
  final case object EchoOk extends ReplyType {
    override def toString(): String = "echo_ok"
  }

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

  import Message._

  def `type`(default: String = ""): ReceivedType =
    body.asObject().getString("type", default) match {
      case "init" => Init
      case "echo" => Echo
    }

  private def messageId(): Long = body.asObject().getLong("msg_id", -1L)

  def nodeId(default: String = ""): String =
    body.asObject().getString("node_id", default)

  /** Create a new json object by combining body with a new 'in_reply_to' field
    * @param body is the json object
    * @return a new json object that contains body and msg_id value
    */
  def newBodyWithMsgId(body: JsonObject): JsonObject =
    Json
      .`object`()
      .merge(body)
      .set("in_reply_to", messageId())

  def nodeIds(): Seq[String] = body.asObject().get("node_ids") match {
    case null => Seq.empty[String]
    case value @ _ =>
      value
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
