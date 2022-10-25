package com.example.pochtao.beans;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

// Bean class used to create a custom SSLSocketFactory that ignores SSL certificate errors
public class AlwaysTrustSSLContextFactory extends SSLSocketFactory {

    private final SSLSocketFactory factory;

    @SuppressLint({"CustomX509TrustManager", "TrustAllX509TrustManager"})
    @SuppressWarnings("java:S4830")
    public static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException { /* Always trust */ }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException { /* Always trust */ }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public AlwaysTrustSSLContextFactory()
            throws NoSuchAlgorithmException, KeyManagementException {
        super();

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {
                new DefaultTrustManager()}, new SecureRandom());
        factory = (SSLSocketFactory) ctx.getSocketFactory();
    }

    public static SocketFactory getDefault() {
        try {
            return new AlwaysTrustSSLContextFactory();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate default AlwaysTrustSSLContextFactory", e);
        }
    }

    @Override
    public Socket createSocket() throws IOException {
        return factory.createSocket();
    }

    public Socket createSocket(InetAddress address, int port,
                               InetAddress localAddress, int localPort)
            throws IOException {
        return factory.createSocket(address, port, localAddress, localPort);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return factory.createSocket(host, port);
    }

    public Socket createSocket(Socket s, String host, int port,
                               boolean autoClose) throws IOException {
        return factory.createSocket(s, host, port, autoClose);
    }

    public Socket createSocket(String host, int port, InetAddress localHost,
                               int localPort) throws IOException, UnknownHostException {
        return factory.createSocket(host, port, localHost, localPort);
    }

    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        return factory.createSocket(host, port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlwaysTrustSSLContextFactory that = (AlwaysTrustSSLContextFactory) o;
        return Objects.equals(factory, that.factory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factory);
    }

    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

}
