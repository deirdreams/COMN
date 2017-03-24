import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by s1368635 on 20/03/17.
 */


//avoid unnecessary retransmissions by having the sender retransmit only
// those packets that it suspects were received in error
public class Sender2b {
    private static boolean StopAckRun = false;
    private static int ackedPackNum = -1;

    public static void send(InetAddress host, int portNum, String filename, int timeout, int N) throws SocketException {
        long starttime = System.currentTimeMillis();
        DatagramSocket socket = new DatagramSocket();
        //Array list of packets waiting to be ack'ed
        ArrayList<Packet> ackedPackets = new ArrayList<Packet>();
        long timer;
        int numBytes = 0;
        int retransmissions = 0;

        byte[] buffer = new byte[1024];

        try{
            File file = new File(filename);
            FileInputStream in = new FileInputStream(file);
            //Some variables for the while loop
            short packNum = 0;
            int packLen;
            int dataLeft;

            boolean last = false;
            boolean retransmit = false;
            boolean loop = true;

            //Starting thread
            Acknowledge Ack = new Acknowledge(portNum, timeout);
            Ack.start();

            System.out.println("Sending packets...");

            while (loop) {

                if (ackedPackets.size() > 0) {
                    for (int i = 0; i < ackedPackets.size(); i++) {
                        //if the packet has already been ack'ed set it to true
                        if (ackedPackets.get(i).packetNumber == ackedPackNum) {
                            ackedPackets.get(i).ack = true;
                        }
                    }
                }
                //Slide the window by removing the packets that have been ack'ed
                while (ackedPackets.size() > 0 && ackedPackets.get(0).ack) {
                    ackedPackets.remove(0);
                }

                while (!last && ackedPackets.size() < N) {
                    dataLeft = in.available();
                    if (dataLeft > buffer.length-3) {
                        packLen = buffer.length-3;
                    } else {
                        //Check if last packet
                        packLen = dataLeft;
                        last = true;
                        //Start timer after finishing last iteration of loop
                        timer = System.currentTimeMillis();
                    }
                    numBytes += packLen;
                    //Full size of the file including the header
                    byte[] bytes = new byte[packLen+3];
                    in.read(bytes, 3, packLen);

                    //get the header of the current packet
                    byte[] header = ByteBuffer.allocate(2).putShort(packNum).array();
                    bytes[0] = header[0];
                    bytes[1] = header[1];

                    //If this is the last packet then make sure the flag is 1, if not then flag is 0
                    //This is so the receiver knows that this is the last packet being sent
                    if (last) {
                        bytes[2] = 1;
                    } else {
                        bytes[2] = 0;
                    }

                    DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, host, portNum);
                    socket.send(sendPacket);

                    //add packets to list waiting to be ack'ed
                    ackedPackets.add(new Packet(packNum, sendPacket, System.currentTimeMillis()));
                    packNum++;
                }

                //check whether we need to retransmit packets
                retransmit = ackedPackets.get(0).timeSent + timeout <= System.currentTimeMillis();
                if (ackedPackets.size() > 0 && retransmit) {
                    for (int i = 0; i < ackedPackets.size(); i ++) {
                        Packet currPack = ackedPackets.get(i);
                        //resend data and store back into array list
                        socket.send(currPack.data);
                        ackedPackets.get(i).timeSent = System.currentTimeMillis();
                        currPack.timeSent = System.currentTimeMillis();
                        ackedPackets.set(i, currPack);
                        retransmissions++;
                    }
                }
                if (last) {
                    loop = false;
                    StopAckRun = true;
                }

            }

        } catch (Exception e) {

        }
        System.out.println("File sent!");
        socket.close();

        long endtime = System.currentTimeMillis();

        long time = endtime-starttime;
        time = (long) (time*0.001);
        long kb = (long) (numBytes*0.001);

        long throughputRate = kb/time;

        //Print out data for results sheet
        System.out.println("Time for transfer: " + time + " sec");
        System.out.println("Num of bytes: " + kb + "kb");
        System.out.println("Throughput rate: " + throughputRate + "kb/s with N = " + N);
        System.out.println("Retransmissions: " + retransmissions);
    }

    public static void main(String[] args) throws IOException {
        final InetAddress host = InetAddress.getByName(args[0]); //localhost
        int port = Integer.parseInt(args[1]);
        String filename = args[2];
        int timeout = Integer.parseInt(args[3]);
        int N = Integer.parseInt(args[4]);

        send(host, port, filename, timeout, N);
    }

    //Create Ack class to create thread
    private static class Acknowledge extends Thread{
        DatagramSocket recSocket;
        int timeout;

        public Acknowledge(int portNum, int timeout) throws SocketException {
            //Create different socket for receiver in different port
            //strange error when using same port number so add 1 to change port num
            this.recSocket = new DatagramSocket(portNum+1);
            this.timeout = timeout;
        }
        @Override
        public void run() {
            while (!StopAckRun) {
                //Allocate two bytes
                byte[] recBuf = new byte[2];
                DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length);
                //Set how many milliseconds until the socket times out
                try {
                    recSocket.setSoTimeout(timeout);
                    recSocket.receive(recPacket);
                    byte[] recData = recPacket.getData();
                    ackedPackNum = ByteBuffer.wrap(recData).getShort();
                } catch (SocketTimeoutException e){
                    System.out.println(e);
                } catch (IOException e) {
                    System.out.println(e);
                }
                //yield its current use of a processor
                Thread.yield();
            }

        }
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
            //each packet holds timer
            this.timeSent = time;
            this.ack = false;
        }
    }
}
