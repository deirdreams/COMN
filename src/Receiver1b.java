import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

/**
 * Created by s1368635 on 27/02/17.
 */
public class Receiver1b {

    private static void receive(int portNum, String filename) throws IOException {
        try {
            DatagramSocket recSocket = new DatagramSocket(portNum);
            File file = new File(filename);
            FileOutputStream out = new FileOutputStream(file);
            short packNum = 0;

            boolean fileReceived = false;
            System.out.println("Receiving file...");

            while (!fileReceived) {

                byte[] buffer = new byte[1027];
                DatagramPacket recPacket = new DatagramPacket(buffer, buffer.length);
                recSocket.receive(recPacket);
                byte[] data = recPacket.getData();

                int lastPack = (int) data[2];

                out.write(buffer, 3, 1024);

                if (lastPack == 1) {
                    fileReceived = true;
                    System.out.println("File received.");

                    out.close();
                    recSocket.close();
                }
            }
        } catch (Exception e) {}
    }

    public static void sendAcknowledgement(short packNum) {
        byte[] sendByte = ByteBuffer.allocate(2).putShort(packNum).array();
        

    }

    public static void main(String args[]) throws IOException {
        int port = Integer.parseInt(args[0]);
        String filename = args[1];

        receive(port, filename);

    }

}
