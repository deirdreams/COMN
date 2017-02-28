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

                byte[] buffer = new byte[1027];
                DatagramPacket recPacket = new DatagramPacket(buffer, buffer.length);
                recSocket.receive(recPacket);
                byte[] data = recPacket.getData();



                //Real file starts at data[3] because of an offset of 3
                byte[] offset = Arrays.copyOfRange(data, 0, 2);

                packNum = ByteBuffer.wrap(offset).getShort();

                int lastPack = (int) data[2];
//                int recPort = recSocket.getPort();

                System.out.println("Sending Acknowledgement");
                if(packNum == lastPackNum) {
                    try {
                        InetAddress recPackAddress = recPacket.getAddress();
                        sendAck(packNum, portNum, sendSocket, recPackAddress);
                    } catch (Exception e) {}
                } else {
                    out.write(buffer, 3, 1024);
                    try {
                        InetAddress recPackAddress = recPacket.getAddress();
                        sendAck(packNum, portNum, sendSocket, recPackAddress);
                    } catch (Exception e) {}
                }

                lastPackNum = packNum;

                if (lastPack == 1) {
                    fileReceived = true;
                    System.out.println("File received.");
                    out.close();
                    recSocket.close();
                }
            }
        } catch (Exception e) {}
    }

    public static void sendAck(short packNum, int portNum, DatagramSocket socket, InetAddress add) throws IOException {
        try {
            byte[] sendByte = ByteBuffer.allocate(2).putShort(packNum).array();
            DatagramPacket sendPack = new DatagramPacket(sendByte, sendByte.length, add, portNum + 1);
            socket.send(sendPack);
        } catch (IOException e) {}
    }

    public static void main(String args[]) throws IOException {
        int port = Integer.parseInt(args[0]);
        String filename = args[1];

        receive(port, filename);

    }

}
