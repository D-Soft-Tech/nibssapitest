package com.danbamitale.epmslib.utils

import android.content.Context
import android.util.Log
import com.danbamitale.epmslib.BuildConfig
import com.danbamitale.epmslib.R
import com.solab.iso8583.IsoMessage
import com.solab.iso8583.MessageFactory
import com.solab.iso8583.parse.ConfigParser
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.text.ParseException

object IsoAdapter {

    fun processISOBitStreamWithJ8583(context: Context, data: String): IsoMessage {
        val outputData = data.substring(data.indexOf("0"))

        return unpackWith8583(context, outputData)
    }

    private fun unpackWith8583(context: Context, data: String): IsoMessage {
        val dataByteArray = data.toByteArray(Charset.forName("UTF-8"))
        val msgFactory = MessageFactory<IsoMessage>()
        msgFactory.ignoreLastMissingField = true

        if (BuildConfig.DEBUG) {
            Log.d("BREAK DOWN DATA: ", data)
        }

        try {
            val xmlReader = context.resources.openRawResource(R.raw.config).bufferedReader()
            xmlReader.use {
                ConfigParser.configureFromReader(msgFactory, it)
                val isoMessage = msgFactory.parseMessage(dataByteArray, 0)
                logIsoMessage(isoMessage)
                return isoMessage
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            throw RuntimeException("Invalid response received")
        } catch (e: StringIndexOutOfBoundsException) {
            e.printStackTrace()
            throw RuntimeException("Invalid response received")
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }

    @Throws(Exception::class, UnsupportedEncodingException::class)
    fun prepareByteStream(isoMessage: IsoMessage): ByteArray {
        val isoStream = isoMessage.writeData()

        return prepareByteStream(isoStream)
    }

    @Throws(Exception::class)
    fun prepareByteStream(isoBytes: ByteArray): ByteArray {
        val len = String(isoBytes).length
        val headerBytes = byteArrayOf(len.shr(8).toByte(), (len % 256).toByte()).also {
            println("Header bytes: ${TripleDES.Hex2String(it)}")
        }

        val request = headerBytes + isoBytes
        println("Request: ${String(request)}\n Hex: ${TripleDES.Hex2String(request)}")
        return request
    }

    fun logIsoMessage(msg: IsoMessage) {
        if (BuildConfig.DEBUG) {
            Log.d("Epms Library", "----ISO MESSAGE-----")
            try {
                Log.d("Epms Library", " MTI : " + msg.type.toString(16))
                for (i in 1..128) {
                    if (msg.hasField(i)) {
                        Log.d(
                            "Epms Library",
                            "    Field-" + i + " : " + getResponseDataFromIndex(msg, i)
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                Log.d("Epms Library", "--------------------")
            }
        }
    }

    fun getResponseDataFromIndex(isoMsg: IsoMessage, index: Int): String {
        return isoMsg.getField<String>(index).value
    }
}
