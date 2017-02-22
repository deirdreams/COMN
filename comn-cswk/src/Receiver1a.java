import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.*;
import java.util.Arrays;

public class Receiver1a {

    private static void receive(InetAddress address, int portNum, String filename) throws IOException {
        DatagramSocket socket = new DatagramSocket(portNum, address);
        File file = new File(filename);
        byte[] buffer = new byte[1027];

        FileOutputStream out = new FileOutputStream(file);
        DatagramPacket recPacket = new DatagramPacket(buffer, buffer.length, address, portNum);

        while (true) {
            socket.receive(recPacket);
            byte[] data = recPacket.getData();
            byte[] header = Arrays.copyOfRange(data,0, 2);
            int lastpack = (int) data[2];

            out.write(data, 3, 1024);

            if (lastpack == 1) {
                out.close();

            }
        }
    }

    public static void main(String args[]) throws IOException {
        InetAddress host = InetAddress.getByName(args[0]); //localhost
        int port = Integer.parseInt(args[1]);
        String filename = args[2];

        receive(host, port, filename);

    }
}
