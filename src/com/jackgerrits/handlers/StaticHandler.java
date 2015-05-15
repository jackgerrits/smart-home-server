package com.jackgerrits.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;

/**
 * Handler for the static route '/' when the request doesn't match the other routes
 * @author jackgerrits
 */
public class StaticHandler implements HttpHandler {

    /**
     * Handler for the static route '/' when the request doesn't match the other routes
     * Serves files out of the www directory in project directory
     * @param t HttpExchange to respond to
     */
    public void handle(HttpExchange t) throws IOException {
        System.out.println("[Static] Serving: " + t.getRequestURI().getPath());
        String root = System.getProperty("user.dir");
        URI uri = t.getRequestURI();
        String filePath =  "/www" + uri.getPath();
        //gets index.html if request is root
        if(filePath.endsWith("/")){
            filePath += "index.html";
        }
        File file = new File(root + filePath).getCanonicalFile();
        if (!file.getPath().startsWith(root)) {
            // Suspected path traversal attack: reject with 403 error.
            //root is project directory, all other directories are forbidden
            String response = "403 (Forbidden)\n";
            t.sendResponseHeaders(403, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else if (!file.isFile()) {
            // Object does not exist or is not a file: reject with 404 error.
            String response = "<h1>404 - Not Found</h1>\n";
            t.sendResponseHeaders(404, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            // Object exists and is a file: accept with response code 200.
            String fileType = Files.probeContentType(file.toPath());

            //probeContentType returns null if it cannot determine type
            //On OSX it seems to always return null, could be a bug?
            //Browser can determine content type in this situation
            if(fileType !=  null){
                t.getResponseHeaders().set("Content-Type", fileType );
            }

            //sets response to 200 OK and sets variable content length
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            final byte[] buffer = new byte[0x10000];    //Buffer size of 64kb
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) { //Reads until there is no data left in the file
                os.write(buffer,0,count);   //writes the read data to the response body
            }
            fs.close();
            os.close();
        }
    }
}
