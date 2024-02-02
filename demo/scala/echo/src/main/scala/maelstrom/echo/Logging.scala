package maelstrom.echo

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

trait Logging {

  def log(msg: => String): String = {
    val timezone = TimeZone.getDefault();
    val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    formatter.setTimeZone(timezone);
    System.err.println(formatter.format(new Date()) + " " + msg);
    System.err.flush();
    msg
  }
}
