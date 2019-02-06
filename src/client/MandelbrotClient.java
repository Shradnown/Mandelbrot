package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import methods.CopiedMethods;

/**
 *
 * @author Erik Engelbrekt-Tchang
 */
public class MandelbrotClient {
    //Currently uses a minimal amount of libraries to keep things straightforward and focus more on programming logic rather than specific functionality of a library
    //TODO: possibly move everything out of main
    public static void main(String[] args) {
        try {
            System.out.println("Enter arguments accoring to following structure: ");
            System.out.println("min_c_re min_c_im max_c_re max_c_im max_iterations image_size_x imagesize_y divisions list_of_servers");
        
            //Reads input from user
            //TODO: test input and create a "menu loop" to let user retry/exit between inputs
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String input = stdIn.readLine();
            
            //Splits the input to create different calls to different servers
            String[] splitInput = input.split(" ");
            
            //Uses an array of strings to keep track of order in case mulitple servers were called for 1 image
            String[] serverResponse = null;
            
            //Currently uses 1 server if no servers are found in arguments
            if (splitInput.length == 8) {
                serverResponse = new String[1];
                serverResponse[0] = sendCalculation("localhost", 4444, input);
            } else if (splitInput.length == 9) {
                serverResponse = new String[1];
                serverResponse[0] = sendCalculation(splitInput[9], 0, input);
            } else if (splitInput.length > 9) {
                String[] serverList = new String[splitInput.length-8];
                serverResponse = new String[serverList.length];
                for (int i = 8; i < splitInput.length; i++) {
                    serverList[i-8] = splitInput[i];
                }
                //Somewhat simple way to split the input to make different calls to the servers
                //TODO: add the missing modulus of y to one of the server calls
                for (int i = 0; i < serverList.length; i++) {
                    String[] tempList = serverList[i].split(":");
                    StringBuilder sb = new StringBuilder();
                    //Ordered the parts of the stringbuilder like this purely for my own readability
                    sb.append(splitInput[0]).append("/");
                    sb.append((Double.parseDouble(splitInput[1])/serverList.length)*i).append("/");
                    sb.append(splitInput[2]).append("/");
                    sb.append((Double.parseDouble(splitInput[3])/serverList.length)*i).append("/");
                    sb.append(splitInput[4]).append("/");
                    sb.append(splitInput[5]).append("/");
                    sb.append((Double.parseDouble(splitInput[6])/serverList.length)*i).append("/");
                    sb.append(splitInput[7]).append("/");
                    sb.append(splitInput[8]);
                    String message = sb.toString();
                    serverResponse[i] = sendCalculation(tempList[0], Integer.parseInt(tempList[1]), message);
                }
            } else {
                System.out.println("Invalid input");
            }
            //Sends the server response to the rendering method if there there was anything returned
            if (serverResponse != null && !serverResponse[0].equals("")) {
                parseImage(serverResponse, splitInput[6], splitInput[7], splitInput[5]);
            }
            
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    
    public static String sendCalculation(String ip, int port, String message) {
        try (
            Socket clientSocket = new Socket(ip, port);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  
        ){
            out.println(message);
            return in.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
    
    private static void parseImage(String[] image, String width, String height, String iterations) {
        try (
            //Currrently just saves the file as mandelbrot.pgm
            BufferedWriter out = new BufferedWriter(new FileWriter("mandelbrot.pgm"));
        ){
            //Compiles the serverresponses into one object in the correct order
            StringBuilder sb = new StringBuilder();
            for (String string : image) {
                sb.append(string);
            }
            //Uses a method I found online to convert the serverrespons back to a multidimensional arraay
            Byte[][] output = CopiedMethods.stringToDeep(sb.toString());
            
            //Prints the PGM header and then loops through the array(s) to render the image
            out.write("P2\n " + width + " " + height + "\n" + iterations);
            for (Byte[] row : output) {
                for (int j = 0; j < output[0].length; j++) {
                    out.write(row[j] + " ");
                }
                out.write("\n");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
