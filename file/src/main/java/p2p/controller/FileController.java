package p2p.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import p2p.service.FileSharer;
import sun.security.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileController {
    private final FileSharer fileSharer;
    private final HttpServer server;
    private final String uploadDir;
    private final ExecutorService executorService;

    public FileController(int port) throws IOException {
        this.fileSharer = new FileSharer();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "peerlink uploads";
        this.executorService = Executors.newFixedThreadPool(10);

        File uploadDirFile = new File(uploadDir);
        if(!uploadDirFile.exists()){
            uploadDirFile.mkdirs();
        }

        server.createContext("/upload", new UploadHandler());
        server.createContext("/download", new DownloadHandler());
        server.createContext("/", new CORSHandler());
    }

    private class CORSHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if(exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")){
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String response = "NOT FOUND";
            exchange.sendResponseHeaders(404, response.getBytes().length);

            try (OutputStream oos = exchange.getResponseBody()) {
                oos.write(response.getBytes());
            }
        }
    }

    private class UploadHanlder implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");

            if(!exchange.getRequestMethod().equalsIgnoreCase("POST")){
                String response = "Method not allowed";
                exchange.sendResponseHeaders(405, response.getBytes().length);
                try(OutputStream oos = exchange.getResponseBody()){
                    oos.write(response.getBytes());
                }
                return;
            }

            Headers requestHeaders = exchange.getRequestHeaders();
            String contentType = requestHeaders.getFirst("Content-Type");
            if(contentType == null || !contentType.startsWith("multipart/form-data")){
                String response = "Bad Request";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                try(OutputStream oos = exchange.getResponseBody()){
                    oos.write(response.getBytes());
                }
                return;
            }

            try{
                String boundary = contentType.substring(contentType.indexOf("boundary=")+9);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                IOUtils.copy(exchange.getResponseBody(), baos);
                byte[] requestData = baos.toByteArray();

                Multiparser parser = new Multiparser(requestData, boundary);
            } catch (Exception ex){
                
            }
        }
    }
}
