import java.net.*; 
import java.util.Scanner;
import java.util.*;
import java.io.*;

public class Client {

	public static void main(String[] args) {
		try {
			if (args.length != 3) {
				throw new IllegalArgumentException();
			}
			// Reads the sms from the file
			String ipAddress = args[0];
			InetAddress serverAddress = InetAddress.getByName(ipAddress);
			int portNum = Integer.parseInt(args[1]);
			
			String smsTextFile = args[2];
			if (smsTextFile.endsWith(".txt")) {
				System.out.println("YAY ITS A TEXT FILE");
			} else {
				System.out.println("ITS NOT A TEXT FILE :(");
				throw new FileNotFoundException("For file: " + smsTextFile);
			}
			FileReader fr = new FileReader(smsTextFile);
			BufferedReader textReader = new BufferedReader(fr);
			String message = "";
			TreeSet<Frame> frameSet = new TreeSet();
			int seqNum = 0;
			while ((message = textReader.readLine()) != null ) {
				RELDATPacket packet = new RELDATPacket();
				if (message.getBytes().length != 0) {
					packet.setData(message.getBytes());
					packet.setSeqNumber(seqNum);
					packet.setChecksum();
					Frame frame = new Frame(packet, true, serverAddress, portNum);

					frame.setFrameSeqNum(seqNum);
					frameSet.add(frame);
					seqNum = seqNum + 1000;
				}
			}
			System.out.println(frameSet.size());
			RELDATMID mid = new RELDATMID(portNum, 4);
			mid.sendFileNow(frameSet);
			System.out.println("Done");
			System.exit(0);
		} catch (UnknownHostException exp) {
			System.out.println("UnknownHostException");
		} catch (FileNotFoundException f) {
			System.out.println("Catch file exception");
		} catch (IOException io) {
			System.out.println("Catch io exception");
		}
		
	}
	
}