package com.netpluspay.nibssclient.service;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;

public class SocketClient {
    public static String write(Context context, String payload) throws Exception {
        System.out.println(payload);
        SSLSocket sslSocket = SSLClientManager.getSSLSocket(context, "196.6.103.18", 5016);
        sslSocket.setSoTimeout(60000);
        sslSocket.setTcpNoDelay(true);
        sslSocket.setKeepAlive(true);
        sslSocket.startHandshake();
        PrintWriter outWriter = new PrintWriter(sslSocket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        outWriter.println(payload);
        return reader.readLine();
    }
}
