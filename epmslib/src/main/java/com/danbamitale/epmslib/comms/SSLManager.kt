package com.danbamitale.epmslib.comms

import android.content.Context
import androidx.annotation.RawRes
import timber.log.Timber
import java.io.InputStream
import java.net.Socket
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.*
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
        Timber.d("ca= ${ca.subjectDN}")

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

    fun getTrustySSLSocketFactory(context: Context, @RawRes certId: Int): SSLSocketFactory {
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                chain?.let {
                    it.forEach { cert ->
                        if (!isCertClient(cert) && !isCertKnown(cert)) {
                            throw CertificateNotYetValidException("Certificate is not valid")
                        }
                    }
                }
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                chain?.let {
                    it.forEach { cert ->
                        if (!isCertServer(cert) && !isCertKnown(cert)) {
                            throw CertificateNotYetValidException("Certificate is not valid")
                        }
                    }
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> =
                getTrustManagerFactory(context, certId).trustManagers.map {
                    it as X509Certificate
                }.toTypedArray()
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

    fun createSocket(sslSocketFactory: SSLSocketFactory, ipAddress: String, port: Int): Socket =
        sslSocketFactory.createSocket(ipAddress, port)

    fun verifyCertificateAndURL(socket: SSLSocket, expectedURL: String): Boolean {
        try {
            val session: SSLSession = socket.session
            val peerCertificates: Array<X509Certificate> = session.peerCertificates as Array<X509Certificate>

            if (peerCertificates.isNotEmpty()) {
                val peerCertificate = peerCertificates[0]
                val cnFromCertificate = extractCommonName(peerCertificate.subjectDN.name)
                val hostFromURL = extractHostFromURL(expectedURL)

                // Compare the CN from the certificate with the host from the URL
                return cnFromCertificate == hostFromURL
            }
        } catch (e: SSLPeerUnverifiedException) {
            // Handle the case where the peer's certificate cannot be verified
            Timber.d(e.localizedMessage)
        } catch (e: CertificateParsingException) {
            // Handle certificate parsing errors
            Timber.d(e.localizedMessage)
        }

        return false
    }

    // Helper function to extract the Common Name (CN) from the X.509 subject DN
    private fun extractCommonName(subjectDN: String): String {
        val cnPattern = "CN=([^,]+)".toRegex()
        val matchResult = cnPattern.find(subjectDN)
        return matchResult?.groupValues?.getOrNull(1) ?: ""
    }

    // Helper function to extract the host part from a URL
    private fun extractHostFromURL(url: String): String {
        val urlParts = url.split("://")
        if (urlParts.size >= 2) {
            val hostPortPart = urlParts[1].split("/")[0]
            return hostPortPart.split(":")[0] // Extract only the host, not the port if specified
        }
        return ""
    }

    private fun isCertKnown(certificate: X509Certificate): Boolean {
        try {
            // Create a KeyStore containing the certificate
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry("cert", certificate)

            // Create a TrustManagerFactory and initialize it with the KeyStore
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)

            // Check if the certificate is trusted by the TrustManager
            for (trustManager in tmf.trustManagers) {
                if (trustManager is X509TrustManager) {
                    try {
                        // Check if the certificate is trusted
                        trustManager.checkServerTrusted(arrayOf(certificate), "RSA")
                        return true
                    } catch (e: CertificateException) {
                        // Certificate is not trusted
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun isCertServer(certificate: X509Certificate): Boolean {
        try {
            val ekuExtension = certificate.extendedKeyUsage
            if (ekuExtension != null) {
                return ekuExtension.contains("1.3.6.1.5.5.7.3.1") // OID for server authentication
            }
        } catch (e: CertificateParsingException) {
            e.printStackTrace()
        }
        return false
    }

    // Function to check if a certificate is intended for client authentication
    fun isCertClient(certificate: X509Certificate): Boolean {
        try {
            val ekuExtension = certificate.extendedKeyUsage
            if (ekuExtension != null) {
                return ekuExtension.contains("1.3.6.1.5.5.7.3.2") // OID for client authentication
            }
        } catch (e: CertificateParsingException) {
            e.printStackTrace()
        }
        return false
    }
}
