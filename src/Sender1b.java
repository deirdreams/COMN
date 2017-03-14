import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

/**
 * Created by s1368635 on 27/02/17.
 */
public class Sender1b {


    private static void send(InetAddress address, int portNum, String filename, int RetryTimeout) throws IOException {
        //Getting starting ms for use when calculating throughput
        long starttime = System.currentTimeMillis();
        //No arguments for sending socket because it binds to first available port (Receiver's port)
        //Does not work if initialised with given port number
        DatagramSocket socket = new DatagramSocket();
        System.out.println("Socket connected");

        //Size of the file being transferred without the header
        byte[] buffer = new byte[1024];
        int retransmissions = 0;
        int numBytes = 0;

        try {
            FileInputStream in = new FileInputStream(filename);
            //initialising variables for while loop
            short packNum = 0;
            int packLen;
            int dataLeft;
            boolean last = false;
            //Create different socket for receiver in different port
            //strange error when using same port number so add 1 to change port num
            DatagramSocket recSocket = new DatagramSocket(portNum+1);
            System.out.println("Sending packet...");

            while(!last) {
                dataLeft = in.available();
                if (dataLeft > buffer.length) {
                    packLen = buffer.length;
                } else {
                    //Check if last packet
                    packLen = dataLeft;
                    last = true;
                }
                numBytes += packLen;

                //Full size of the file including the header
                byte[] bytes = new byte[1027];
                in.read(bytes, 3, packLen);

                //get the header of the current packet
                byte[] header = ByteBuffer.allocate(2).putShort(packNum).array();
                bytes[0] = header[0];
                bytes[1] = header[1];

                //If this is the last packet then make sure the flag is 1, if not then flag is 0
                //This is so the receiver knows that this is the last packet being sent
                if (last) {bytes[2] = 1;}
                else {bytes[2] = 0;}

                DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, address, portNum);
                socket.send(sendPacket);

                //Check for acknowledgement
                System.out.println("Waiting for acknowledgement...");
                boolean acknowledged = false;
                //resends packets until acknowledged before timeout
                while(!acknowledged) {
                    acknowledged = isAcknowledged(packNum, RetryTimeout, recSocket);
                    if (acknowledged) break;
                    else {
                        //Resend the packet if it is not acknowledged
                        System.out.println("Resending");
                        retransmissions++;
                        socket.send(sendPacket);
                    }
                }
                packNum++;
//                Thread.sleep(10);
            }
        } catch (Exception e){
            System.out.println(e);
        }
        socket.close();

        long endtime = System.currentTimeMillis();

        System.out.println("File sent!");

        long time = endtime-starttime;
        time = (long) (time*0.001);
        long kb = (long) (numBytes*0.001);

        long throughputRate = kb/time;

        //Print out data for results sheet
        System.out.println("Number of retransmissions: " + retransmissions);
        System.out.println("Time for transfer: " + time + " sec");
        System.out.println("Num of bytes: " + kb + "kb");
        System.out.println("Throughput rate: " + throughputRate + "kb/s");
    }

    //method for checking if the current packet is being acknowledged
    public static boolean isAcknowledged(short packNum, int timeout, DatagramSocket rec) throws Exception{
        //Allocate two bytes
        byte[] recBuf = new byte[2];
        DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length);
        //Set how many milliseconds until the socket times out
        rec.setSoTimeout(timeout);
        try {
            rec.receive(recPacket);
            byte[] recData = recPacket.getData();
            //make sure packet being received is the correct packet
            int ack = ByteBuffer.wrap(recData).getShort();
            if (ack == packNum) {
                System.out.println("Packets acknowledged");
                return true;
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Socket timed out.");
            return false;
        }
        return false;
    }

    public static void main(String args[]) throws IOException{
        final InetAddress host = InetAddress.getByName(args[0]); //localhost
        int port = Integer.parseInt(args[1]);
        String filename = args[2];
        int timeout = Integer.parseInt(args[3]);

        send(host, port, filename, timeout);

    }

}
