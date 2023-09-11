package com.danbamitale.epmslib.comms

import android.content.Context
import com.danbamitale.epmslib.entities.ConnectionData
import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import java.net.*
import javax.net.ssl.SSLSocket

class SocketRequest
/**
 *
 * @param connectionData
 */
(private val connectionData: ConnectionData) {

    /**
     *
     * @param isoStream byte[]
     * @param isSSL boolean
     * @param context Context
     * @return response String
     * @throws Exception
     */
    @Throws(Exception::class)
    fun send(context: Context, isoStream: ByteArray): String {
        if (connectionData.isSSL) {
            val sslSocket = getConnection(
                context,
                connectionData.ipAddress,
                connectionData.ipPort,
                connectionData.certFileResId ?: 0,
            )
            sslSocket.soTimeout = 60 * 1000
            return send(isoStream, sslSocket)
        } else {
            val socket = Socket()
            val sockAddress = InetSocketAddress(connectionData.ipAddress, connectionData.ipPort)
            socket.connect(sockAddress, 60 * 1000)
            socket.soTimeout = 60 * 1000

            return send(isoStream, socket)
        }
    }

    /**
     *
     * @param isoStream byte[]
     * @param socket Socket
     * @return response String
     * @throws Exception
     */

    @Throws(Exception::class)
    private fun send(isoStream: ByteArray, socket: Socket): String {
        var responseArray = byteArrayOf()
        try {
            val dataOut = DataOutputStream(socket.getOutputStream())
            val dataIn = DataInputStream(socket.getInputStream())

            Timber.tag("OutData").d("${String(isoStream)} Length: ${isoStream.size}")

            dataOut.write(isoStream)

            dataIn.buffered().use {
                responseArray = it.readBytes()
            }
        } catch (_: EOFException) {
        } catch (e: SocketTimeoutException) {
            throw SocketTimeoutException("Connection timed out, failed to receive response from remote server")
        } catch (e: ConnectException) {
            throw RuntimeException("Could not connect to the internet, check your connection settings and try again")
        } catch (e: NoRouteToHostException) {
            throw RuntimeException("Could not connect with remote server, check your connection settings and try again")
        } catch (e: PortUnreachableException) {
            throw RuntimeException("Could not connect with remote server, port is unreachable, check your connection settings and try again")
        } catch (e: MalformedURLException) {
            throw RuntimeException("Malformed url, check your connection settings and try again")
        } catch (e: BindException) {
            throw RuntimeException("Could not bind socket to local address or port, check your connection settings and try again")
        } catch (e: SocketException) {
            throw RuntimeException("Could not create socket, check your connection settings and try again")
        } catch (e: UnknownHostException) {
            throw RuntimeException("Host address could not be recognized, check your connection settings and try again")
        } catch (e: UnknownServiceException) {
            throw RuntimeException("Unknown service, check your connection settings and try again")
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            try {
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return String(responseArray)
    }

    /**
     *
     * @param ip
     * @param port
     * @param context
     * @return SSLSocket
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getConnection(context: Context, ip: String, port: Int, certFileResId: Int): SSLSocket {
        val trustFactory = SSLManager.getTrustManagerFactory(context, certFileResId)
        val sslFactory = SSLManager.getTrustySSLSocketFactory(context, certFileResId)
//        val sslFactory = getSSLSocketFactory(trustManagerFactory = trustFactory)
        return SSLManager.createSocket(sslFactory, ip, port) as SSLSocket
    }
}
