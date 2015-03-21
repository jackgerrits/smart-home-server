package com.jackgerrits.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * Created by Jack on 21/03/2015.
 */
public class StaticHandler implements HttpHandler {

    public void handle(HttpExchange t) throws IOException {
        System.out.println(t.getRequestURI().getPath());
        String root = System.getProperty("user.dir");
        URI uri = t.getRequestURI();
        String filePath = uri.getPath();
        if(filePath.endsWith("/")){
            filePath += "index.html";
        }
        File file = new File(root + filePath).getCanonicalFile();
        if (!file.getPath().startsWith(root)) {
            // Suspected path traversal attack: reject with 403 error.
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
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer,0,count);
            }
            fs.close();
            os.close();
        }
    }
}
