package com.netpluspay.nibssclient.exception

import android.util.Log
import com.google.gson.Gson

class NibssClientException(messages: String?, val nibssError: NibssError?) : Exception(messages) {
    init {
        Log.e(NibssClientException::class.java.simpleName, messages ?: "Error")
    }

    data class NibssError(
        val code: Int?,
        val error: String?,
        var possibleCause: String?,
        var possibleSolution: String?
    )
}

fun String.createNibssException(action: String): NibssClientException {
    val nibssError: NibssClientException.NibssError? =
        Gson().fromJson(this, NibssClientException.NibssError::class.java)
    nibssError?.let {
        val message = it.error ?: "Gateway Error, Retry"
        val possibleCause = when {
            message.contains("out of bounds") -> {
                when (action) {
                    "getConfigData" -> "Invalid Terminal Id|Nibss Downtime"
                    "callHome" -> "Expired Session|Nibss Downtime"
                    else -> "Expired session|Terminal not configured|Nibss Downtime"
                }
            }
            message.contains("parsedResponse.getField") && message.contains("must not be null") -> "Invalid Terminal Id|Nibss Downtime"
            message == "Terminal Id and/or terminalSerial absent" -> "Client was not initialized with TID"
            message == "Amount should not be less than 200 kobo" -> "Amount too small"
            message == "SSL credentials not configured" -> "Client was not initialized with SSL credentials"
            else -> "Anything could have gone wrong [Didn't initialize client, didn't pass TID to params....]"
        }
        val possibleSolution =
            when {
                message.contains("parsedResponse.getField") && message.contains("must not be null") -> "Check TID|Attempt configure terminal"
                message.contains("out of bounds") -> "Attempt configure terminal"
                message == "Terminal Id and/or terminalSerial absent" -> "Initialize client with terminal Id and serial"
                message == "Amount should not be less than 200 kobo" -> "Increase amount"
                message == "SSL credentials not configured" -> "Initialize client with ssl credentials"
                else -> "Try again"
            }
        it.possibleCause = possibleCause
        it.possibleSolution = possibleSolution
        return NibssClientException(it.error, it)
    }
    return NibssClientException(
        "Server did not return a response",
        NibssClientException.NibssError(500, "Server did not return a response", "Unknown", "Retry")
    )
}
