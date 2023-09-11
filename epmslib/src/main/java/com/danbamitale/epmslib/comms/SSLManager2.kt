package com.danbamitale.epmslib.comms

import android.content.Context
import androidx.annotation.RawRes
import java.io.InputStream
import java.net.Socket
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

object SSLManager2 {

    fun getTrustManagerFactory(
        context: Context,
        @RawRes certificateResourceId: Int,
    ): TrustManagerFactory {
        // Load CAs from an InputStream (could be from a resource or ByteArrayInputStream or ...)
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")

        val caInput: InputStream = context.resources.openRawResource(certificateResourceId)
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }
        System.out.println("ca=" + ca.subjectDN)

        // Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }

        // Create a TrustManager that trusts the CAs in our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }

        return tmf
    }

    fun getTrustySSLSocketFactory(trustManagerFactory: TrustManagerFactory): SSLSocketFactory {
        // Create an SSLContext with custom TrustManager
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustManagerFactory.trustManagers, SecureRandom())
        }

        return sslContext.socketFactory
    }

    fun createSocket(
        sslSocketFactory: SSLSocketFactory,
        ipAddress: String,
        port: Int,
        serverHostname: String, // The hostname to verify against the certificate's CN  07063543872
    ): Socket {
        val socket = sslSocketFactory.createSocket(ipAddress, port)

        if (socket is SSLSocket) {
            // Enable hostname verification
            val params = SSLParameters()
            params.endpointIdentificationAlgorithm = "HTTPS" // Use HTTPS style hostname verification
            socket.sslParameters = params

            // Set the server hostname for verification
//            socket.peerHost = serverHostname
        }

        return socket
    }
}
