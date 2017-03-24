import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by s1368635 on 14/03/2017.
 */
public class Receiver2a {
    private static void receive(int portNum, String filename) throws IOException {
        try {
            //create sockets for both receiver and sender
            DatagramSocket recSocket = new DatagramSocket(portNum);
            DatagramSocket sendSocket = new DatagramSocket();

            File file = new File(filename);
            FileOutputStream out = new FileOutputStream(file);
            short packNum = 0;

            boolean fileReceived = false;
            //initialised so that lastPackNum == pacKNum is false; number cannot be -1
            int lastPackNum = -1;
            int bytes_rec = 0;

            System.out.println("Receiving file...");

            while (!fileReceived) {

                byte[] buffer = new byte[1024];
                DatagramPacket recPacket = new DatagramPacket(buffer, buffer.length);
                recSocket.receive(recPacket);
                byte[] data = recPacket.getData();

                //Real file starts at data[3] because of an offset of 3
                byte[] offset = Arrays.copyOfRange(data, 0, 2);
                packNum = ByteBuffer.wrap(offset).getShort();

                //if packet has already been seen, send ACK
                if (lastPackNum >= packNum) {
                    InetAddress recPackAddress = recPacket.getAddress();
                    sendAck(packNum, portNum, sendSocket, recPackAddress);

                //check if packet is next in sequence
                } else if (lastPackNum + 1 == packNum) {
                    //Check flag
                    int lastPack = (int) data[2];
                    out.write(buffer, 3, data.length-3);
                    bytes_rec += data.length-3;
                    InetAddress recPackAddress = recPacket.getAddress();
                    sendAck(packNum, portNum, sendSocket, recPackAddress);

                    //Check if the flag in the data received is 1, ie is the last packet
                    if (lastPack == 1) {
                        fileReceived = true;
                        recSocket.close();
                        out.close();
                        System.out.println("File received.");
                        System.out.println("Num bytes received: " + bytes_rec);

                    }
                    //change lastPackNum for next execution of while loop
                    lastPackNum = packNum;
                }
            }

        } catch (Exception e) {} finally {
            System.exit(1);
        }
    }

    //new method to send acknowledgement to specified port to sender
    public static void sendAck(short packNum, int portNum, DatagramSocket socket, InetAddress add) throws IOException {
        try {
            ByteBuffer bb = ByteBuffer.allocate(2);
            byte[] sendByte = bb.putShort(packNum).array();
            //Strange error if using the same port number as receiving socket so add 1 to change
            DatagramPacket sendPack = new DatagramPacket(sendByte, sendByte.length, add, portNum + 1);
            socket.send(sendPack);
        } catch (IOException e) {}
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        String filename = args[1];

        receive(port, filename);
    }
}
