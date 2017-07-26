// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.iothubreact.helpers

class InvalidDateFormatException(message: String = null, cause: Throwable = null) extends
  RuntimeException(InvalidDateFormatException.defaultMessage(message, cause), cause)

object InvalidDateFormatException {
  def defaultMessage(message: String, cause: Throwable) =
    if (message != null) message
    else if (cause != null) cause.toString()
    else null
}
