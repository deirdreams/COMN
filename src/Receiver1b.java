import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by s1368635 on 27/02/17.
 */
public class Receiver1b {

    private static void receive(int portNum, String filename) throws IOException {
        try {
            //create sockets for both receiver and sender
            DatagramSocket recSocket = new DatagramSocket(portNum);
            DatagramSocket sendSocket = new DatagramSocket();

            File file = new File(filename);
            FileOutputStream out = new FileOutputStream(file);
            short packNum = 0;

            boolean fileReceived = false;
            //initialised so that lastPackNum == pacKNum is false; number cannot be 2
            int lastPackNum = 2;

            System.out.println("Receiving file...");

            while (!fileReceived) {
                //File is 1024 bytes plus 3 for offset
                byte[] buffer = new byte[1027];
                DatagramPacket recPacket = new DatagramPacket(buffer, buffer.length);
                recSocket.receive(recPacket);
                byte[] data = recPacket.getData();

                //Real file starts at data[3] because of an offset of 3
                byte[] offset = Arrays.copyOfRange(data, 0, 2);
                packNum = ByteBuffer.wrap(offset).getShort();

                //Check flag
                int lastPack = (int) data[2];
//                int recPort = recSocket.getPort();

                System.out.println("Sending Acknowledgement");
                //Attempting to send acknowledgements
                if(packNum == lastPackNum) {
                    try {
                        InetAddress recPackAddress = recPacket.getAddress();
                        sendAck(packNum, portNum, sendSocket, recPackAddress);
                    } catch (Exception e) {}
                } else {
                    //Write the data received in the new
                    out.write(buffer, 3, 1024);
                    try {
                        InetAddress recPackAddress = recPacket.getAddress();
                        sendAck(packNum, portNum, sendSocket, recPackAddress);
                    } catch (Exception e) {}
                }
                //change lastPackNum for next execution of while loop
                lastPackNum = packNum;

                //Check if the flag in the data received is 1, ie is the last packet
                if (lastPack == 1) {
                    fileReceived = true;
                    System.out.println("File received.");
                    //Close output stream and socket
                    out.close();
                    recSocket.close();
                }
            }
        } catch (Exception e) {}
    }

    //new method to send acknowledgement to specified port to sender
    public static void sendAck(short packNum, int portNum, DatagramSocket socket, InetAddress add) throws IOException {
        try {
            ByteBuffer bb = ByteBuffer.allocate(2);
            byte[] sendByte = bb.putShort(packNum).array();
            //Strange error if using the same port number as receiving socket so add 1 to change
            DatagramPacket sendPack = new DatagramPacket(sendByte, sendByte.length, add, portNum + 1);
            socket.send(sendPack);
            System.out.println("Acknowledgement Sent!");
        } catch (IOException e) {}
    }

    public static void main(String args[]) throws IOException {
        int port = Integer.parseInt(args[0]);
        String filename = args[1];

        receive(port, filename);

    }

}
