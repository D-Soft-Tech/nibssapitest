package com.netpluspay.nibssclient.service

import android.content.Context
import com.netpluspay.nibssclient.util.app.TerminalParams
import com.netpluspay.nibssclient.util.app.TerminalParams.CONNECTION_HOST
import com.netpluspay.nibssclient.util.app.TerminalParams.CONNECTION_PORT
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.jvm.Throws


object NibssApiClient {

    private lateinit var socket: Socket

    @Throws(Exception::class)
    fun write(context: Context, data: String): String {
        return try {
            if (::socket.isInitialized) {
                println(socket.isClosed)
            }
            if (::socket.isInitialized.not() || socket.isClosed)
                socket = (if (TerminalParams.USE_SSL.not()) {
                    Socket().apply {
                        soTimeout = 60000
                        tcpNoDelay = true
                        keepAlive = true
                        connect(InetSocketAddress(CONNECTION_HOST, CONNECTION_PORT))
                    }
                } else
                    SSLManager.getSSLSocket(context).apply {
                        soTimeout = 60000
                        tcpNoDelay = true
                        keepAlive = true
                        startHandshake()
                    }).apply {
                    println("Connected")
                }
            socket.use {
                val out = PrintWriter(socket.outputStream, true)
                val `in` = BufferedReader(
                    InputStreamReader(socket.inputStream)
                )
                println("Processing...")
                out.println(data)
                `in`.readLine()
            }
        } catch (e: Exception) {
            throw e
        }
    }
}