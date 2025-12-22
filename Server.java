package tcpJavaSimulation;

import java.util.*;
import java.io.*;
import java.net.*;

public class Server {
	
	public static void main (String[]args) throws IOException {
        System.out.println("Server will begin sending packets to the user");
        
        ServerSocket serverSocket = new ServerSocket(5555);
        Socket clientSocket = serverSocket.accept();
        
        PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true); //put in true because writing according to preprogrammed java constructor of the net class
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//I thought I should use fileReader because that is that we had learned in programming but I had researched the topic and learned that for the server....it is better to use bufferedReader
        Random random = new Random();//for dropping/sending of packets
        
        List<String> droppedPackets = new ArrayList<>();//creates an arrayList to track dropped packets - for the client..not server
        
        String message = "Demonstration of TCP protocol";//I had to research this because I thought I should use printlns but in my researched I remembered they are would only show up on my monitor
        int numberOfPackets = 20;
        // ADDED: make sure sizeOfPackets is at least 1 so we never build empty substrings
        int sizeOfPackets = Math.max(1, message.length() / numberOfPackets);//I needed help with this from AI to decide how to set up this equation
        List<String> packets = new ArrayList<>();//arrayList to hold all packets
        
        for (int index = 0; index < numberOfPackets; index++) {//for loop to track all the packets
            int beginning = index * sizeOfPackets;
            int end = (index == numberOfPackets - 1) ? message.length() : Math.min(message.length(), (index + 1) * sizeOfPackets);//creates boolean flag to check if this is the last packet
            if (beginning >= message.length()) break; // ADDED: stop if we've consumed the message
            String data = message.substring(beginning, end);//collects all the characters of this particular packet
            boolean lastPacket = (index == numberOfPackets - 1) || (end >= message.length());//creates a boolean for the last packet

            // Use client-compatible format: SEQUENCE: <seq> DATA: <payload> END: <true/false>
            String packet = "SEQUENCE: " + index + " DATA: " + data + " END: " + lastPacket;
            packets.add(packet);
            if (lastPacket) break; // ADDED: once we've reached the end, stop generating more packets
        }
        
        Collections.shuffle(packets); // shuffle actual packets to simulate out-of-order delivery
        
        for (int ctr = 0; ctr < packets.size(); ctr++) {
            String packet = packets.get(ctr);
            boolean lastPacket = packet.contains("END: true");
            if (lastPacket || random.nextDouble() <= 0.8) {
                output.println(packet); // send packet if not dropped
            } else {
                droppedPackets.add(packet); // (optional) we tracked it
            }
        }
        
        String line;
        while ((line = input.readLine()) != null) {
            if (line.startsWith("RESEND:")) {
                String[] missing = line.substring(7).split(",");//array for missing packets that request a re-send
                for (int i = 0; i < missing.length; i++) {
                    String missString = missing[i];
                    try {
                        int miss = Integer.parseInt(missString.trim());
                        String resendPacket = packets.get(miss);
                        boolean lastPacket = resendPacket.contains("END: true");
                        if (lastPacket || random.nextDouble() <= 0.8) {
                            output.println(resendPacket); // send actual packet (fixed: removed quotes)
                        }
                    } catch (Exception e) {
                        System.err.println("Invalid resend request: " + missString);
                    }
                }
            }
        }
        
        clientSocket.close(); // fixed: removed duplicate close
        serverSocket.close();
    }
}
