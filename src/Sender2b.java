import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by s1368635 on 20/03/17.
 */
public class Sender2b {
    public static void send(InetAddress host, int portNum, String filename, int timeout, int N) throws SocketException {
        long starttime = System.currentTimeMillis();
        DatagramSocket socket = new DatagramSocket();


    }

    public static void main(String[] args) throws IOException {
        final InetAddress host = InetAddress.getByName(args[0]); //localhost
        int port = Integer.parseInt(args[1]);
        String filename = args[2];
        int timeout = Integer.parseInt(args[3]);
        int N = Integer.parseInt(args[4]);

        send(host, port, filename, timeout, N);
    }


    //Create Packet class that holds the information we need for acknowledgement
    private static class Packet {
        int packetNumber;
        DatagramPacket data;
        long timeSent;
        boolean ack;

        public Packet(int num, DatagramPacket data, long time) {
            this.packetNumber = num;
            this.data = data;
            this.timeSent = time;
            this.ack = false;
        }
    }
}
