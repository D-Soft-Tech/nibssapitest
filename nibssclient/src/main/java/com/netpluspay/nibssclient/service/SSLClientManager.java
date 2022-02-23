package com.netpluspay.nibssclient.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class SSLClientManager {
    public static int DEFAULT_BUFFER_SIZE = 8 * 1024;

    public static SSLSocket getSSLSocket(Context context, String host, int port) throws Exception {
        return (SSLSocket) getSSLFactory(context).createSocket(host, port);
    }

    private static SSLSocketFactory getSSLFactory(Context context) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLSv1");
        sslContext.init(getKeyManagerFactory(context).getKeyManagers(), trustAllCerts(), null);
        return sslContext.getSocketFactory();
    }

    private static KeyManagerFactory getKeyManagerFactory(Context context) throws IOException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        String password = "keystore_password";
        Certificate clientCert = getX509Certificate(context.getAssets().open("epms-client.cert.pem"));
        PrivateKey clientKey = getPrivateKey(context.getAssets().open("epms-client.key.pem"));
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("certificate", clientCert);
        keyStore.setKeyEntry("private-key", clientKey, password.toCharArray(), new Certificate[]{clientCert});
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        return keyManagerFactory;
    }

    private static X509Certificate getX509Certificate(InputStream stream) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(stream);
    }

    private static PrivateKey getPrivateKey(InputStream inputStream) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyContent = new String(inputStreamToByteArray(inputStream));
        privateKeyContent = privateKeyContent.replace("-----BEGIN PRIVATE KEY-----\n", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----\n", "")
                .replace("-----END RSA PRIVATE KEY-----", "");
        byte[] decoded = Base64.decode(privateKeyContent, Base64.DEFAULT);
        KeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    @SuppressLint({"TrustAllX509TrustManager", "CustomX509TrustManager"})
    private static X509TrustManager[] trustAllCerts() {
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        return new X509TrustManager[]{trustManager};
    }

    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(DEFAULT_BUFFER_SIZE, inputStream.available()));
        long bytesCopied = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int bytes = inputStream.read(buffer);
        while (bytes >= 0) {
            out.write(buffer, 0, bytes);
            bytesCopied += bytes;
            bytes = inputStream.read(buffer);
        }
        inputStream.close();
        return out.toByteArray();
    }
}
