package maelstrom.echo

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

trait Logging {

  def log(msg: => String)(implicit
      formatter: SimpleDateFormat = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss"
      ),
      timezone: TimeZone = TimeZone.getDefault,
      printFn: (SimpleDateFormat, String) => String = { (formatter, msg) =>
        System.err.println(formatter.format(new Date()) + " " + msg)
        System.err.flush()
        msg
      }
  ): String = {
    formatter.setTimeZone(timezone)
    printFn(formatter, msg)
  }
}
