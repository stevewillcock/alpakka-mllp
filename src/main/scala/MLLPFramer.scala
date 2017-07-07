import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging

class MLLPFramer extends GraphStage[FlowShape[ByteString, ByteString]] with LazyLogging {

  import MLLPFramer._

  val in: Inlet[ByteString] = Inlet[ByteString]("MLLPStream")
  val out: Outlet[ByteString] = Outlet[ByteString]("MLLPMessages")
  override val shape = FlowShape(in, out)
  val lineSeparatorByteString = ByteString(LINE_SEPARATOR_BYTE)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    var isInMllpMessage = false
    var mllpLines: Vector[ByteString] = Vector[ByteString]()

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val line = grab(in)
        if (line.head == MESSAGE_START_BYTE) {
          logger.debug("MLLP header found")
          if (isInMllpMessage) {
            logger.debug("Already inside MLLP delimited message, unexpectedly received start of message character")
          }
          isInMllpMessage = true
          mllpLines = Vector[ByteString](line.tail)
          pull(in)
        } else {
          if (line.head == MESSAGE_END_BYTE && isInMllpMessage) {
            logger.debug("MLLP tail found")
            isInMllpMessage = false
            push(out, mllpLines.tail.fold(mllpLines.head)((a, b) => a ++ lineSeparatorByteString ++ b))
          } else if (isInMllpMessage) {
            mllpLines = mllpLines :+ line
            pull(in)
          } else {
            logger.debug("Discarding line with length " + line.length + " as we are not inside an MLLP delimited message atm")
            pull(in)
          }
        }
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = pull(in)
    })

  }
}

object MLLPFramer {
  val MESSAGE_START_BYTE = 0x0b
  val MESSAGE_END_BYTE = 0x1c
  val LINE_SEPARATOR_BYTE = 0x0d
}