import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;

/**
 * Created by s1368635 on 20/03/17.
 */
public class Receiver2b {

    public static void receive(int portNum, String filename, int N) throws IOException{
        //create sockets for both receiver and sender
        DatagramSocket recSocket = new DatagramSocket(portNum);
        DatagramSocket sendSocket = new DatagramSocket();
        File file = new File(filename);
        FileOutputStream out = new FileOutputStream(file);
        short packNum = 0;
        boolean fileReceived = false;

        //initialised so that lastPackNum == pacKNum is false; number cannot be -1
        int lastPackNum = -1;

        System.out.println("Receiving file...");
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        String filename = args[1];
        int N = Integer.parseInt(args[2]);

        receive(port, filename, N);
    }
}
