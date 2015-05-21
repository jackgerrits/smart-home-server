package com.jackgerrits;

import com.jackgerrits.handlers.FeedHandler;
import com.jackgerrits.handlers.SensorHandler;
import com.jackgerrits.handlers.StaticHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
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
 * Server class is essentially the overall driver, it is a singleton class. <br>
 * It's primary role is running the web server as well as creating and communicating with the <code>SensorController</code>
 * @author Jack Gerrits
 */
public class Server {
    private static Server self = null;
    private SensorController sensorController;
    private HttpsServer server = null;
    private FeedHandler ps;
    private final String username;
    private int port;

    /**
     * Gets the static reference to itself, otherwise creates a <code>Server</code> object.
     * @return Singleton <code>Server</code> object.
     */
    public static Server get() {
        if(self == null){
            self = new Server();
        }
        return self;
    }


    /**
     * Constructs server instance <br>
     * Configures SSL for server and sets up routes for '/data/feed', '/data/sensors' and '/'<br>
     * Then finally starts web server.
     */
    public Server(){
        self = this;
        Options options = Options.get();
        sensorController = SensorController.get();
        port = options.getServerPort();
        username = options.getUsername();

        SSLContext sslContext = null;
        try {
            server = HttpsServer.create(new InetSocketAddress(port), 0);

            sslContext = SSLContext.getInstance("TLS");

            //retrieves SSL password from options file
            char[] sslPassword = options.getSSLPassword().toCharArray();
            KeyStore keystore = KeyStore.getInstance("JKS");

            //opens keystore file specified in options file
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
    }

    /**
     * Responds to an OPTIONS request used for cross origin requests
     * @param t HttpExchange for incoming request
     */
    public void handleOptionsRequest(HttpExchange t) throws IOException{
        t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST,OPTIONS");
        t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Origin,Host");
        t.sendResponseHeaders(200, -1);
    }


    /**
     * Responds to client on failed authentication
     * @param t HttpExchange for incoming request
     */
    public void handleAuthFailure(HttpExchange t) throws IOException{
        t.getResponseHeaders().set("Reason", "Incorrect username or password");
        t.sendResponseHeaders(401, -1);
    }

    /**
     * Authenticates a request with HttpExchange t
     * @param t HttpExchange for incoming request
     * @return <b>true</b> if authentication is successful
     */
    public boolean authRequest(HttpExchange t) throws IOException{
        //will only authenticate a POST request
        if(!t.getRequestMethod().equals("POST")){
            t.getResponseHeaders().set("Reason", "Must use POST");
            t.sendResponseHeaders(401, -1);
            return false;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
        StringBuilder out =  new StringBuilder();
        String line;

        //reads in post body
        while ((line = br.readLine()) != null) {
            out.append(line);
        }

        String reqUsername = "";
        String sysUsername = Options.get().getUsername();
        String reqPassword = "";

        //parses body into JSON object
        JSONObject body = (JSONObject)(JSONValue.parse(out.toString()));

        //retrieves username from post body if it exists
        if(body.containsKey("username")){
           reqUsername = (String)body.get("username");
        }

        //receives password from post body if it exists
        if(body.containsKey("password")){
            reqPassword = (String)body.get("password");
        }

        //if either password or username field was omitted in JSON, it will not equal credentials on server
        if(PasswordHash.validatePassword(reqPassword) && sysUsername.equals(reqUsername)){
            return true;
        }

        //returns false when password and username didn't equal and notifies client
        handleAuthFailure(t);
        return false;
    }

    /**
     * Responds to request with 404 message
     * @param t HttpExchange to respond to
     */
    public void serve404(HttpExchange t) throws IOException {
        String response = "<h1>404 - Not Found</h1>\n";
        t.sendResponseHeaders(404, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Starts the server that was configured in the constructor
     */
    public void start(){
        System.out.println("Starting server on port " + port + "...");
        server.start();
        System.out.println("Server started successfully!");
    }


    /**
     * Stops the server, by calling stop to <code>FeedHandler</code> thread and <code>SensorController</code>
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
