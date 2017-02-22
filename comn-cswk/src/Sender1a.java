import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

//The image should be split into multiple 1027B packets (one packet consists of 1024KB payload and 3B header).

public class Sender1a {

    private static void send(InetAddress address, int portNum, String filename) throws IOException{
        DatagramSocket socket = new DatagramSocket(portNum, address);
        System.out.println("Socket connected");
        byte[] buffer = new byte[1024];
        File file = new File(filename);

        try {
            FileInputStream in = new FileInputStream(file);
            short packNum = 0;
            int packLen;
            boolean last = false;
            System.out.println("Sending packet...");

            while(in.available() > 0) {
                int dataLeft = in.available();

                if (dataLeft > buffer.length) {
                    packLen = buffer.length;
                } else {
                    packLen = dataLeft;
                    last = true;
                }

                byte[] bytes = new byte[1027];
                in.read(bytes, 3, packLen);

                byte[] header = ByteBuffer.allocate(2).putShort(packNum).array();
                bytes[0] = header[0];
                bytes[1] = header[1];
                if (last) {bytes[2] = 1;}
                else {bytes[2] = 0;}

                DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, address, portNum);
                socket.send(sendPacket);

                packNum++;
            }


        } catch (Exception e){
            System.out.println(e);
        }
        socket.close();

        }

    public static void main(String args[]) throws IOException{
        final InetAddress host = InetAddress.getByName(args[0]); //localhost
        int port = Integer.parseInt(args[1]);
        String filename = args[2];

        send(host, port, filename);

    }
}