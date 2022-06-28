package com.netpluspay.nibssclient.service

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import com.netpluspay.nibssclient.exception.createNibssException
import com.netpluspay.nibssclient.util.app.TerminalParams
import com.netpluspay.nibssclient.util.app.TerminalParams.CLIENT_CERT_ASSET
import com.netpluspay.nibssclient.util.app.TerminalParams.CLIENT_KEY_ASSET
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.* // ktlint-disable no-wildcard-imports

object SSLManager {

    fun getSSLSockets(): SSLSocket {
        val socketFac: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        return socketFac.createSocket("localhost", 6868) as SSLSocket
    }

    fun getSSLSocket(context: Context): SSLSocket =
        getSSLFactory(context).createSocket(
            TerminalParams.CONNECTION_HOST,
            TerminalParams.CONNECTION_PORT
        ) as SSLSocket

    private fun getSSLFactory(context: Context): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLSv1")
        sslContext.init(
            getKeyMangerFactory(context)!!.keyManagers,
            trustEveryOne(),
            null
        )
        return sslContext.socketFactory
    }

    private fun getKeyMangerFactory(context: Context): KeyManagerFactory? {
        if (TerminalParams.isClientCertInitialized().not())
            throw "{\"code\":500,\"error\":\"SSL credentials not configured\"}".createNibssException(
                "initialize"
            )

        val password = "netpos_password"
        // load client certificate
        val cert =
            getX509Certificate(
                if (TerminalParams.FILE_CONTENT) CLIENT_CERT_ASSET.byteInputStream() else context.assets.open(
                    CLIENT_CERT_ASSET
                )
            )

        val key = getFromString(context)
        // client key and certificates are sent to server so it can authenticate us
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        ks.setCertificateEntry("certificate", cert)
        ks.setKeyEntry(
            "private-key",
            key,
            password.toCharArray(),
            arrayOf<Certificate>(cert)
        )
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(ks, password.toCharArray())
        return kmf
    }

    @Throws(Exception::class)
    private fun getFromString(context: Context): PrivateKey? {
        if (TerminalParams.isClientKeyInitialized().not())
            throw "{\"code\":500,\"error\":\"SSL credentials not configured\"}".createNibssException(
                "initialize"
            )
        var reader = if (TerminalParams.FILE_CONTENT) CLIENT_KEY_ASSET else {
            val inputStream = context.assets.open(CLIENT_KEY_ASSET)
            val out = String(inputStream.readBytes(), StandardCharsets.UTF_8)
            inputStream.close()
            out
        }
        reader = reader.replace("-----BEGIN PRIVATE KEY-----\n", "")
        reader = reader.replace("-----END PRIVATE KEY-----", "")
        reader = reader.replace("-----BEGIN RSA PRIVATE KEY-----\n", "")
        reader = reader.replace("-----END RSA PRIVATE KEY-----", "")
        val encoded: ByteArray = Base64.decode(reader, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(encoded)
        val kf: KeyFactory = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(keySpec)
    }

    private fun getX509Certificate(inputStream: InputStream): X509Certificate {
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        return inputStream.use {
            cf.generateCertificate(it) as X509Certificate
        }
    }

    @Throws(FileNotFoundException::class)
    private fun accessFile(resource: String): InputStream {
        var inputStream = SSLManager::class.java.getResourceAsStream("/resources/$resource")
        if (inputStream == null) inputStream = SSLManager::class.java.getResourceAsStream(resource)
        if (inputStream == null) inputStream = FileInputStream(resource)
        return inputStream
    }

    @SuppressLint("TrustAllX509TrustManager")
    private fun trustEveryOne(): Array<X509TrustManager> {
        return arrayOf(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        )
    }
}
