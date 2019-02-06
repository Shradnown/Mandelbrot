package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 *
 * @author Erik Engelbrekt-Tchang
 */
public class MandelbrotServer {
    //TODO: possibly move everything out of main
    public static void main(String[] args) {
        //Opening server socket, defaulting to port 4444 if no port is specified in args
        int port = 4444;
        if (args[0].length() > 0) {
            port = Integer.parseInt(args[0]);
        }
        
        try (
            ServerSocket serverSocket = new ServerSocket(port);
        ) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
    
    //Creates a handler object to separate calls from multiple clients to mulitple threads
    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        
        @Override
        public void run(){
            //Uses the traditional try statement as it's coded in Java 8, and I'm unsure how to make the Thread class work with try-with-resources
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                out.print(Arrays.deepToString(calculateArray(in.readLine())));
                
            } catch (IOException e) {
                System.out.println("Exception caught");
                System.out.println(e.getMessage());
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.out.println("Exception caught while trying to close socket");
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    
    public static byte[][] calculateArray(String input) {
        String[] splitInput = input.split("/");
        double minCRe = Double.parseDouble(splitInput[0]);
        double minCIm = Double.parseDouble(splitInput[1]);
        double maxCRe = Double.parseDouble(splitInput[2]);
        double maxCIm = Double.parseDouble(splitInput[3]);
        int maxN = Integer.parseInt(splitInput[4]);
        int width = Integer.parseInt(splitInput[5]);
        int height = Integer.parseInt(splitInput[6]);
        //TODO: decide on an implementation of the divisions
        int divisions = Integer.parseInt(splitInput[7]);
        byte[][] pixelArray = new byte[width][height];
        
        for (int row = 0; row < width; row++) {
            for (int column = 0; column < height; column++) {
                //Starts calculations from "top right" in positive imaginary and negative real
                double cRe = minCRe + column*(maxCRe-minCRe)/width;
                double cIm = maxCIm - row*(maxCIm-minCIm)/height;
                double x = 0;
                double y = 0;
                byte iterations = 0;
                
                while (x*x+y*y <= 4 && iterations < maxN) {
                    double temp = x*x-y*y+cRe;
                    y = 2*x*y + cIm;
                    x = temp;
                    iterations++;
                }
                pixelArray[row][column] = iterations;
            }
        }
        return pixelArray;
    }
}
