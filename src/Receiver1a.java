import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.*;
import java.util.Arrays;

public class Receiver1a {

    private static void receive(int portNum, String filename) throws IOException {
        try {
            DatagramSocket socket = new DatagramSocket();
            File file = new File(filename);
            byte[] buffer = new byte[1027];

            FileOutputStream out = new FileOutputStream(file);
            DatagramPacket recPacket = new DatagramPacket(buffer, buffer.length);
            boolean last = false;

            while (!last) {
                System.out.println("Receiving packets...");
                socket.receive(recPacket);
                byte[] data = recPacket.getData();
                //byte[] header = Arrays.copyOfRange(data,0, 2);
                int lastPack = (int) data[2];
                System.out.println("" + lastPack);

                out.write(data, 3, 1024);

                if (lastPack == 1) {
                    last = true;
                    out.close();
                    System.exit(1);
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
