package com.jackgerrits;

import com.jackgerrits.handlers.FeedHandler;
import com.jackgerrits.handlers.SensorHandler;
import com.jackgerrits.handlers.StaticHandler;
import com.sun.net.httpserver.*;
import com.sun.scenario.Settings;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;

/**
 * Created by Jack on 21/03/2015.
 */
public class Server {
    private static Server self = new Server();
    private SensorController sensorController;
    private HttpsServer server = null;
    private FeedHandler ps;
    private final String username;
    private final String password;

    public static Server get() {
        if(self == null){
            self = new Server();
        }
        return self;
    }

    //runs webserver and application server
    public Server(){
        self = this;
        Options options = Options.get();
        sensorController = SensorController.get();
        int port = options.getServerPort();
        username = options.getUsername();
        password = options.getPassword();

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
                httpsParameters.setSSLParameters(defaultSSLParameters);
            }
        };

        ps = new FeedHandler();
        server.createContext("/data/feed", ps);
        server.createContext("/data/sensors", new SensorHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.setHttpsConfigurator(httpsConfigurator);

        System.out.println("Starting server on port " + port + "...");
        server.start();
        System.out.println("Server started successfully!");
    }


    public void handleOptionsRequest(HttpExchange t) throws IOException{
        t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST,OPTIONS");
        t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Origin,Host");
        t.sendResponseHeaders(200, -1);
    }

    public void handleAuthFailure(HttpExchange t) throws IOException{
        t.getResponseHeaders().set("Reason", "Incorrect username or password");
        t.sendResponseHeaders(401, -1);
    }

    public boolean authRequest(HttpExchange t) throws IOException{
         if(t.getRequestMethod().equals("GET")){
            t.getResponseHeaders().set("Reason", "GET not supported.");
            t.sendResponseHeaders(401, -1);
            return false;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
        StringBuilder out =  new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            out.append(line);
        }

        String username = "";
        String password = "";

        JSONObject body = (JSONObject)(JSONValue.parse(out.toString()));

        if(body.containsKey("username")){
           username = (String)body.get("username");
        }

        if(body.containsKey("password")){
            password = (String)body.get("password");
        }

        if(password.equals(password) && username.equals(username)){
            return true;
        }
        handleAuthFailure(t);
        return false;
    }

    public void serve404(HttpExchange t) throws IOException {
        String response = "<h1>404 - Not Found</h1>\n";
        t.sendResponseHeaders(404, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }


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
