package p2p.service;

import p2p.utils.UploadUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class FileSharer {
    private HashMap<Integer, String> availableFiles; // list of available files

    public FileSharer(){
        availableFiles = new HashMap<>();
    }

    public int offerFile(String filePath){
        int port;
        while(true){
            port = UploadUtils.getPort();
            if(!availableFiles.containsKey(port)){
                availableFiles.put(port, filePath);
                return port;
            }
        }
    }

    public void startFileServer(int port){
        String filePath = availableFiles.get(port);
        if(filePath == null){
            System.out.println("No file is associated with the port: "+port);
            return;
        }

        try(Socket serversocket = new ServerSocket(port)){
            System.out.println("Serving file: " + new File(filePath).getName() + " on port: " + port);
            Socket clientSocket = serversocket.accept();
            System.out.println("Client Connection" + clientSocket.getInetAddress());
            new Thread(new FileSharerHandler(clientSocket, filePath)).start();
        } catch (IOException ex){
            System.err.println("Error handling file server on port" + port);
        }
    }

    public static class FileSharerHandler implements Runnable {
        private final Socket clientSocket;
        private final String filePath;

        public FileSharerHandler(Socket clientSocket, String filePath){
            this.clientSocket = clientSocket;
            this.filePath = filePath;
        }

        @Override
        public void run(){
            try(FileInputStream fis = new FileInputStream(filePath)){
                OutputStream oos = clientSocket.getOutputStream();
                String fileName = new File(filePath).getName();
                String header = "Filename:" + fileName + "\n";
                oos.write(header.getBytes());

                byte[] buffer = new byte[4096];
                int byteRead;

                while(byteRead = fis.read(buffer) != -1){
                    oos.write(buffer, 0, byteRead);
                }
                System.out.println("File: " + fileName + " sent to " + clientSocket.getInetAddress());
            } catch (IOException e) {
                // throw new RuntimeException(e);
                System.err.println("Error in file fetching in handler");
            } finally {
                try{
                    clientSocket.close();
                } catch (Exception ex){
                    System.err.println("Error closing socket: " + ex.getMessage());
                }
            }
        }

    }
}
