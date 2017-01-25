// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}

private[iothubreact] class StreamManager
  extends GraphStage[FlowShape[MessageFromDevice, MessageFromDevice]] {

  private[this] val in          = Inlet[MessageFromDevice]("StreamCanceller.Flow.in")
  private[this] val out         = Outlet[MessageFromDevice]("StreamCanceller.Flow.out")
  private[this] var closeSignal = false

  override val shape = FlowShape.of(in, out)

  def close(): Unit = closeSignal = true

  override def createLogic(attr: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val message: MessageFromDevice = grab(in)
          push(out, message)
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if (closeSignal) {
            cancel(in)
          } else {
            pull(in)
          }
        }
      })
    }
  }
}
