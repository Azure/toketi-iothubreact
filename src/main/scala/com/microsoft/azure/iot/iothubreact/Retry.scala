// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact

import scala.concurrent.duration.Duration

/** Retry logic
  */
private[iothubreact] object Retry extends Logger {

  /** Retry to execute some code, pausing between the attempts
    *
    * @param times Number of attempts (>=1)
    * @param pause Pause between attempts
    * @param code  Code to execute
    * @tparam A Type of the result returned by `code`
    *
    * @return Result provided by `code`
    */
  def apply[A](times: Int, pause: Duration)(code: ⇒ A): A = {
    var result: Option[A] = None
    var remaining = times
    while (remaining > 0) {
      remaining -= 1
      try {
        result = Some(code)
        remaining = 0
      } catch {
        case e: Exception ⇒
          if (remaining > 0) {
            log.warning("Retry loop: {} attempts left [{}]", remaining, e.getMessage)
            Thread.sleep(pause.toMillis)
          } else {
            throw e
          }
      }
    }
    result.get
  }
}
