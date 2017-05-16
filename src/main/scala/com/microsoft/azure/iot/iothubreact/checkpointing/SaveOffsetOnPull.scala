// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.checkpointing

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import com.microsoft.azure.iot.iothubreact.MessageFromDevice
import com.microsoft.azure.iot.iothubreact.checkpointing.CheckpointService.UpdateOffset

/** Flow receiving and emitting IoT messages, while keeping note of the last offset seen
  *
  * @param partition IoT hub partition number
  */
private[iothubreact] class SaveOffsetOnPull(cpconfig: ICPConfiguration, partition: Int)
  extends GraphStage[FlowShape[MessageFromDevice, MessageFromDevice]] {

  val in   = Inlet[MessageFromDevice]("Checkpoint.Flow.in")
  val out  = Outlet[MessageFromDevice]("Checkpoint.Flow.out")
  val none = ""

  override val shape = FlowShape.of(in, out)

  // All state MUST be inside the GraphStageLogic, never inside the enclosing
  // GraphStage. This state is safe to read/write from all the callbacks
  // provided by GraphStageLogic and the registered handlers.
  override def createLogic(attr: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      val checkpointService = CheckpointActorSystem(cpconfig).getCheckpointService(partition)
      var lastOffsetSent    = none

      // when a message enters the stage we safe its offset
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val message: MessageFromDevice = grab(in)
          if (!message.isKeepAlive) lastOffsetSent = message.offset
          push(out, message)
        }
      })

      // when asked for more data we consider the saved offset processed and save it
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if (lastOffsetSent != none) checkpointService ! UpdateOffset(lastOffsetSent)
          pull(in)
        }
      })
    }
  }
}
