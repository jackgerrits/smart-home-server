package com.jackgerrits;

import com.jackgerrits.handlers.FeedHandler;
import com.jackgerrits.handlers.SensorHandler;
import com.jackgerrits.handlers.StaticHandler;
import com.sun.net.httpserver.*;
import com.sun.net.httpserver.HttpExchange;
import sun.net.httpserver.HttpsServerImpl;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    public Server(SensorController sensorController, Options options){
        this.options = options;
        this.sensorController = sensorController;
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
//            server = HttpServer.create(new InetSocketAddress(port), 0);
            server = HttpsServerImpl.create(new InetSocketAddress(port), 0);
            sslContext = SSLContext.getInstance("TLS");
            char[] password = options.getSSLPassword().toCharArray();
            KeyStore ks = KeyStore.getInstance ("JKS");
            FileInputStream fis = new FileInputStream (options.getSSLKeystore());
            ks.load ( fis, password );

            KeyManagerFactory kmf = KeyManagerFactory.getInstance ( "SunX509" );
            kmf.init ( ks, password );

            TrustManagerFactory tmf = TrustManagerFactory.getInstance ( "SunX509" );
            tmf.init ( ks );

            sslContext.init ( kmf.getKeyManagers (), tmf.getTrustManagers (), null );


            server.setHttpsConfigurator ( new HttpsConfigurator( sslContext )
            {
                public void configure ( HttpsParameters params )
                {
                    try
                    {

                        // initialise the SSL context
                        SSLContext c = SSLContext.getDefault ();
                        SSLEngine engine = c.createSSLEngine ();
                        params.setNeedClientAuth ( false );
                        params.setCipherSuites ( engine.getEnabledCipherSuites () );
                        params.setProtocols ( engine.getEnabledProtocols () );

                        // get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters ();
                        params.setSSLParameters ( defaultSSLParameters );
                    }
                    catch ( Exception ex )
                    {
                        ex.printStackTrace();
                    }
                }
            } );

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        ps = new FeedHandler(sensorController);
        server.createContext("/data/feed", ps);
        server.createContext("/data/sensors", new SensorHandler(sensorController));
        server.createContext("/", new StaticHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        System.out.println("Starting server on port " + port + "...");
        server.start();
        System.out.println("Server started successfully!");
    }

    //just runs web server
    public Server(Options options){
        port = options.getServerPort();
        username = options.getUsername();
        password = options.getPassword();


        SSLContext sslContext = null;
        try {
//            server = HttpServer.create(new InetSocketAddress(port), 0);
            server = HttpsServer.create(new InetSocketAddress(8080), 0);
            sslContext = SSLContext.getInstance("TLS");
            char[] password = options.getSSLPassword().toCharArray();
            KeyStore ks = KeyStore.getInstance ("JKS");
            FileInputStream fis = new FileInputStream (options.getSSLKeystore());
            ks.load ( fis, password );

            KeyManagerFactory kmf = KeyManagerFactory.getInstance ( "SunX509" );
            kmf.init ( ks, password );

            TrustManagerFactory tmf = TrustManagerFactory.getInstance ( "SunX509" );
            tmf.init ( ks );

            sslContext.init ( kmf.getKeyManagers (), tmf.getTrustManagers (), null );



        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters httpsParameters) {
                SSLContext sslContext = getSSLContext();
                SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
                httpsParameters.setSSLParameters(defaultSSLParameters);
            }
        };

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) throws IOException {
                HttpsExchange s = (HttpsExchange)t;
                s.getSSLSession();
                String response = "<html><body>Hello world.</body></html>";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });
        server.setExecutor(Executors.newCachedThreadPool());
        System.out.println("Starting server on port " + port + "...");
        server.setHttpsConfigurator(httpsConfigurator);
        server.start();
        System.out.println("Server started successfully!");
    }

    public void stop(){
        System.out.println("Server stopping...");
        server.stop(0);
        if(ps != null){
            ps.stop();
        }
    }
}
