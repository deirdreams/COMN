import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by s1368635 on 20/03/17.
 */
public class Receiver2b_original{

    public static void receive(int portNum, String filename, int N) throws IOException {
        //create sockets for both receiver and sender
        DatagramSocket recSocket = new DatagramSocket(portNum);
        DatagramSocket sendSocket = new DatagramSocket();
        File file = new File(filename);
        FileOutputStream out = new FileOutputStream(file);
        ArrayList<Packet> BufferedPackets = new ArrayList<Packet>();
        short packNum = 0;
        boolean fileReceived = false;

        //initialised so that lastPackNum == pacKNum is false; number cannot be -1
        int lastPackNum = -1;
        int base = 0;
        int lastPack = -1;

        System.out.println("Receiving file...");

        while (!fileReceived) {
            byte[] buffer = new byte[1024];
            DatagramPacket recPacket = new DatagramPacket(buffer, buffer.length);
            recSocket.receive(recPacket);
            byte[] data = recPacket.getData();
            //adjust size of buffer
            int currPackSize = recPacket.getLength();

            //Real file starts at data[3] because of an offset of 3
            byte[] offset = Arrays.copyOfRange(data, 0, 2);
            packNum = ByteBuffer.wrap(offset).getShort();


            //reacknowledge packet if seen before
            if (packNum <= lastPackNum) {
                InetAddress recPackAddress = recPacket.getAddress();
                sendAck(packNum, portNum, sendSocket, recPackAddress);
            }
            //if next packet in sequence, write to file, then acknowledge
            else if (packNum == lastPackNum + 1) {
                lastPack = (int) data[2];
                out.write(data, 3, data.length-3);
                lastPackNum = packNum;
                InetAddress recPackAddress = recPacket.getAddress();
                sendAck(packNum, portNum, sendSocket, recPackAddress);
                if (lastPack == 1) {
                    fileReceived = true;
                }
            } else {
                //Out of sequence packet
                //if not acknowledged, add to buffer of unack'ed packets
                Packet p = new Packet(packNum, recPacket);
                if (BufferedPackets.size() < N) {
                    BufferedPackets.add(p);
                }

                //packet is out of sequence so reorder buffer so first one is oldest packet received
//                Collections.sort(BufferedPackets, Comparator.comparingInt(o -> o.packetNumber));
                Collections.sort(BufferedPackets, new Comparator<Packet>() {
                    @Override
                    public int compare(Packet o1, Packet o2) {
                        return o1.packetNumber - o2.packetNumber;
                    }
                });
                InetAddress recPackAddress = recPacket.getAddress();
                sendAck(packNum, portNum, sendSocket, recPackAddress);

                while (BufferedPackets.size() > 0 && BufferedPackets.get(0).packetNumber == lastPackNum + 1) {
                    Packet currPack = BufferedPackets.get(0);
                    BufferedPackets.remove(0);
                    lastPackNum = currPack.packetNumber;
                    out.write(currPack.data.getData(), 3, currPack.data.getLength()-3);
                }

            }
            if (lastPack == 1) {
                fileReceived = true;
            }

        }
        System.out.println("File Received!");
        out.close();
        recSocket.close();

    }

    //new method to send acknowledgement to specified port to sender
    public static void sendAck(short packNum, int portNum, DatagramSocket socket, InetAddress add) throws IOException {
        try {
            ByteBuffer bb = ByteBuffer.allocate(2);
            byte[] sendByte = bb.putShort(packNum).array();
            DatagramPacket sendPack = new DatagramPacket(sendByte, sendByte.length, add, portNum + 1);
            socket.send(sendPack);
        } catch (IOException e) {}
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        String filename = args[1];
        int N = Integer.parseInt(args[2]);

        receive(port, filename, N);
    }

    //Create Packet class that holds the information we need for acknowledgement
    private static class Packet {
        int packetNumber;
        DatagramPacket data;

        public Packet(int num, DatagramPacket data) {
            this.packetNumber = num;
            this.data = data;
        }

    }
}
