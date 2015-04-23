package com.jackgerrits;

import com.jackgerrits.handlers.FeedHandler;
import com.jackgerrits.handlers.SensorHandler;
import com.jackgerrits.handlers.StaticHandler;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;

/**
 * Created by Jack on 21/03/2015.
 */
public class Server {
    private int port;
    private SensorController sensorController;
    private Options options;
    private HttpsServer server = null;
    private FeedHandler ps;
    private final String username;
    private final String password;
    

    //runs webserver and application server
    public Server(){
        options = Options.get();
        sensorController = SensorController.get();
        port = options.getServerPort();
        username = options.getUsername();
        password = options.getPassword();

        BasicAuthenticator bAuth = new BasicAuthenticator("get") {
            @Override
            public boolean checkCredentials(String user, String pwd) {
                return user.equals(username) && pwd.equals(password);
            }
        };

        SSLContext sslContext = null;
        try {
            server = HttpsServer.create(new InetSocketAddress(port), 0);

            sslContext = SSLContext.getInstance("TLS");
            char[] sslPassword = options.getSSLPassword().toCharArray();
            KeyStore keystore = KeyStore.getInstance("JKS");
            FileInputStream keystoreFile = new FileInputStream(options.getSSLKeystore());
            keystore.load(keystoreFile, sslPassword);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init( keystore, sslPassword );

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keystore);

            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        } catch (IOException | CertificateException | KeyStoreException | KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters httpsParameters) {
                SSLContext sslContext = getSSLContext();
                SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
//                httpsParameters.setNeedClientAuth(true);
                httpsParameters.setSSLParameters(defaultSSLParameters);
            }
        };

        ps = new FeedHandler();
        server.createContext("/data/feed", ps).setAuthenticator(bAuth);
        server.createContext("/data/sensors", new SensorHandler()).setAuthenticator(bAuth);
        server.createContext("/", new StaticHandler()).setAuthenticator(bAuth);
        server.setExecutor(Executors.newCachedThreadPool());
        server.setHttpsConfigurator(httpsConfigurator);

        System.out.println("Starting server on port " + port + "...");
        server.start();
        System.out.println("Server started successfully!");
    }

    //just runs web server
    /*
    public Server(){
        options = Options.get();
        port = options.getServerPort();
        username = options.getUsername();
        password = options.getPassword();

        BasicAuthenticator bAuth = new BasicAuthenticator("get") {
            @Override
            public boolean checkCredentials(String user, String pwd) {
                return user.equals(username) && pwd.equals(password);
            }
        };

        SSLContext sslContext = null;
        try {
            server = HttpsServer.create(new InetSocketAddress(port), 0);

            sslContext = SSLContext.getInstance("TLS");
            char[] sslPassword = options.getSSLPassword().toCharArray();
            KeyStore keystore = KeyStore.getInstance("JKS");
            FileInputStream keystoreFile = new FileInputStream(options.getSSLKeystore());
            keystore.load(keystoreFile, sslPassword);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init( keystore, sslPassword );

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keystore);

            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        } catch (IOException | CertificateException | KeyStoreException | KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters httpsParameters) {
                SSLContext sslContext = getSSLContext();
                SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
//                httpsParameters.setNeedClientAuth(true);
                httpsParameters.setSSLParameters(defaultSSLParameters);
            }
        };

        server.createContext("/", new StaticHandler()).setAuthenticator(bAuth);
        server.setExecutor(Executors.newCachedThreadPool());
        server.setHttpsConfigurator(httpsConfigurator);

        System.out.println("Starting server on port " + port + "...");
        server.start();
        System.out.println("Server started successfully!");
    }
    */

    public void stop(){
        System.out.println("Server stopping...");
        server.stop(0);
        if(ps != null){
            ps.stop();
        }
        if(sensorController != null){
            sensorController.stop();
        }
    }
}
