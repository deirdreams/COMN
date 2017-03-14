import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

//The image should be split into multiple 1027B packets (one packet consists of 1024KB payload and 3B header).

public class Sender1a {

    private static void send(InetAddress address, int portNum, String filename) throws IOException{
        //create socket without arguments so it will bind to the first open socket
        DatagramSocket socket = new DatagramSocket();
        System.out.println("Socket connected");
        byte[] buffer = new byte[1024];

        try {
            FileInputStream in = new FileInputStream(filename);
            //packNum small value so no need for type int
            short packNum = 0;
            int packLen;
            boolean last = false;
            int dataLeft = in.available();
            System.out.println("Sending packet...");

            //runs while there is still data to be sent
            while(dataLeft > 0) {

                //make sure there is still data left in the input stream to be sent
                dataLeft = in.available();

                //Check if it is the last packet to send
                if (dataLeft > buffer.length) {
                    packLen = buffer.length;
                } else {
                    packLen = dataLeft;
                    last = true;
                }
                //Actual size of the file, 1024 plus 3 for the header
                byte[] bytes = new byte[1027];
                //read file by the length of the packet
                in.read(bytes, 3, packLen);

                //get the details of the header
                byte[] header = ByteBuffer.allocate(2).putShort(packNum).array();
                bytes[0] = header[0];
                bytes[1] = header[1];

                //If this is the last packet then make sure the flag is 1, if not then flag is 0
                //This is so the receiver knows that this is the last packet being sent
                if (last) {bytes[2] = 1;}
                else {bytes[2] = 0;}
                //int port = socket.getPort();
                DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, address, portNum);
                socket.send(sendPacket);

                packNum++;
                //time delay for 10ms as specified in the coursework
                Thread.sleep(10);
            }
        } catch (Exception e){
            System.out.println(e);
        }
        System.out.println("File sent!");
        socket.close();
        }

    public static void main(String args[]) throws IOException{
        //convert localhost into InetAddress; will not change so use "final" keyword
        final InetAddress host = InetAddress.getByName(args[0]); //localhost
        int port = Integer.parseInt(args[1]);
        String filename = args[2];

        send(host, port, filename);

    }
}