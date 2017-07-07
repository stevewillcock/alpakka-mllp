import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Flow, Framing, Tcp}
import akka.util.ByteString

// https://akka.io/blog/2016/07/30/mastering-graph-stage-part-1
// https://akka.io/blog/2016/08/25/simple-sink-source-with-graphstage
// https://akka.io/blog/2016/10/21/emit-and-friends

object consoleTest {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem("server-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val connections = Tcp().bind("localhost", 53878)

    val echo = Flow[ByteString]
      .via(Framing.delimiter(ByteString(0x0d), maximumFrameLength = 20000, allowTruncation = true))
      .via(new MLLPFramer())
      .map(hl7Message => {
        println(hl7Message)
        ByteString("OK")
      })

    connections runForeach { connection =>

      println(s"New connection from: ${connection.remoteAddress}")
      connection.handleWith(echo)

    }

    println("Hit enter to stop")
    scala.io.StdIn.readLine()
    system.terminate()

  }

}