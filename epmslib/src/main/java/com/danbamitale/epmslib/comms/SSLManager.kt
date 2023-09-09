package com.danbamitale.epmslib.comms

import android.content.Context
import androidx.annotation.RawRes
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.* // ktlint-disable no-wildcard-imports

object SSLManager {

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

        // Create a TrustManager that trusts the CAs inputStream our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }

        return tmf
    }

    fun getTrustySSLSocketFactory(): SSLSocketFactory {
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }

        return SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), SecureRandom())
        }.socketFactory
    }

    fun getSSLSocketFactory(
        trustManagerFactory: TrustManagerFactory,
        keyManagerFactory: KeyManagerFactory? = null,
        secureRandom: SecureRandom? = null,
    ): SSLSocketFactory {
        val context: SSLContext = SSLContext.getInstance("TLSv1").apply {
            init(keyManagerFactory?.keyManagers, trustManagerFactory.trustManagers, secureRandom)
        }

        return context.socketFactory
    }

    fun createSocket(sslSocketFactory: SSLSocketFactory, ipAddress: String, port: Int) =
        sslSocketFactory.createSocket(ipAddress, port)
}
