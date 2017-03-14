import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.*;
import java.util.Arrays;

public class Receiver1a {

    private static void receive(int portNum, String filename) throws IOException {
        try {
            DatagramSocket socket = new DatagramSocket(portNum);
            File file = new File(filename);
            FileOutputStream out = new FileOutputStream(file);

            boolean fileReceived = false;
            System.out.println("Receiving file...");

            //runs while the file is not received
            while (!fileReceived) {

                byte[] buffer = new byte[1027];
                DatagramPacket recPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(recPacket);
                byte[] data = recPacket.getData();

                //check for the flag, which is the third value in the received data (see Sender1a)
                // can be 0 or 1
                int lastPack = (int) data[2];

                //write to the buffer with offset of 3, which is the header, and file size of 1024
                out.write(buffer, 3, 1024);

                if (lastPack == 1) {
                    fileReceived = true;
                    System.out.println("File received.");
                    //close socket and output stream when the last packet is received
                    out.close();
                    socket.close();
                }
            }
        } catch (Exception e) {}
    }

    public static void main(String args[]) throws IOException {
        int port = Integer.parseInt(args[0]);
        String filename = args[1];

        receive(port, filename);

    }
}
