import java.net.*;
import java.util.*;
import java.io.*;

public class BiDirectionalTransfer {

	public static final int BUFFER_SIZE = 2048;
  	public static final int TIMEOUT = 3000;
  	public static final int REDOS = 3;
	public static final double MAX_DATA_SIZE = 1000.0;
  	protected static DatagramSocket socket;
  	protected static DatagramPacket packet;
  	protected static byte[] receivedResponse;
	protected static InetAddress addr;
	protected static int prt;
	static int capacity;
	static Frame headWindow;
	protected static List<RELDATPacket> packetList;
	static TreeMap<Integer, Frame> sendBuffer;
	protected static int currPacketNumber = 0;
	protected static int expAckNumber = 0;
	protected static int currSeqNum;
  	protected static int currAckNum;
	protected static int isTimedoutNumber = -1;
	protected static boolean isTimedout = false;
	protected Window<Frame> window;
	private TreeSet<Frame> dataSet;


	public BiDirectionalTransfer(int cap, int currAckNum, int currSeqNum, DatagramSocket sock) {
		socket = sock;
      	receivedResponse = new byte[BUFFER_SIZE];
		this.capacity = cap;
		this.currSeqNum = currSeqNum;
		this.currAckNum = currAckNum;
		window = new Window<Frame>(cap);
		dataSet = new TreeSet<Frame>();
		packetList = new ArrayList<RELDATPacket>();
		sendBuffer = new TreeMap<Integer, Frame>();
		packet = new DatagramPacket(receivedResponse, BUFFER_SIZE);

	}

	public void setDataSet(TreeSet<Frame> set) {
		this.dataSet = set;
	}

	class ReceiveAcksThread extends Thread {
		Window<Frame> sendingWindow;
		TreeSet<Frame> mainSet;
		int currSeqNum;
		DatagramSocket socket;
		ReceiveAcksThread(String name, Window<Frame> window, TreeSet<Frame> cumSet, DatagramSocket socket) {
			super(name);
			this.sendingWindow = window;
			mainSet = cumSet;
			currSeqNum = 0;
			this.socket = socket;
		}

		public void run() {
			while (true) {
				try {
					packet = new DatagramPacket(receivedResponse, BUFFER_SIZE);
					System.out.println("Waiting for ack");
					socket.receive(packet);
					RELDATPacket currAck = RELDATPacket.deserialize(receivedResponse);
					System.out.println("IN RECEIVE " + currAck.getAckNumber());
					for (int i = 0; i < sendingWindow.getSendFileWindow().size(); i++) {
						if (currAck.getAckNumber() == sendingWindow.getSendFileWindow().get(i).getFrameSeqNum()) {
							sendingWindow.getSendFileWindow().get(i).setAckRec(true);
							window.removeElement(i);
							break;
						}
					}

				} catch (IOException e) {
					System.out.println("EXCeption IO");
				}
				
			}
		}
	}

	public void sendFilePackets() {
		int size = dataSet.size();
		Thread thread = new ReceiveAcksThread("Send file back", this.window, this.dataSet, this.socket);
		thread.start();
		currSeqNum = 0;
		while (true) {
				Frame frame;
				//System.out.println(window.getCapac() + " " + window.getCurrAmount());
				for (int i = 0; i < window.getCapac() - window.getCurrAmount(); i++) {
					frame = dataSet.pollFirst();
					if (frame != null) {
						try {
							
							if (socket == null) {
								System.out.println("socket is null");
							}
							
									
							RELDATPacket packet = frame.getCurrFramePack();
							System.out.println("IN SEND " + frame.getFrameSeqNum());
							packet.setSeqNumber(frame.getFrameSeqNum());
							packet.setAck(0);
							packet.setAckNumber(0);
							packet.setDataLength(packet.getDataLength());
							packet.setChecksum();
							packet.setSrcPort(size);
							frame.setCurrFramePack(packet);
							window.addElement(frame);
							byte[] arr = packet.serialize();
							DatagramPacket sendPacket = new DatagramPacket(arr, arr.length, frame.getAddress(), frame.getPort());
							socket.send(sendPacket);
							System.out.println("Packet sent");
							frame.startTimer(socket);
						} catch (IOException io) {
							System.out.println("IO Exception");
						}
					}		
				}
				// if (window.getCurrAmount() == window.getCapac() && dataSet.() == null) {
				// 	break;
				// }

				// if (BiDirectionalTransfer.isTimedout) {
				// 	System.out.println("IN RESEND");
				// 	BiDirectionalTransfer.isTimedout = false;
				// 	BiDirectionalTransfer.isTimedoutNumber = -1;
				// 	for (int i = 0; i < window.getCapac(); i++) {
				// 		if (window.getSendFileWindow().get(i).getFrameSeqNum() == BiDirectionalTransfer.isTimedoutNumber) {
				// 			window.getSendFileWindow().get(i).startTimer(socket);
				// 		}
				// 	}
				// }
		}
	}

	class SendingThread extends Thread {
		Window<Frame> sendingWindow;
		SendingThread(String name, Window<Frame> window) {
			super(name);
			this.sendingWindow = window;
		}

		public void run() {
			while (true) {
				try {
					Frame frame = sendingWindow.popHead();
					RELDATPacket currPack = frame.getCurrFramePack();
					RELDATPacket newPacket = new RELDATPacket(0, 0, 1, 0);
					newPacket.setAckNumber(currPack.getSeqNumber());
					newPacket.setChecksum();
					byte[] arr = newPacket.serialize();
					DatagramPacket packet = new DatagramPacket(arr, arr.length, frame.getAddress(), frame.getPort());
					socket.send(packet);
					if (currPack.getSrcPort() == dataSet.size() && window.getCurrAmount() == 0) {
						break;
					}
				} catch (IOException io) {
					io.printStackTrace();
				}
			}
		}
	}

	public void receiveFilePacket() {
		Thread thread = new SendingThread("Send", this.window);
		thread.start();
		byte[] receivedData = new byte[BUFFER_SIZE];
		while (true) {
			packet = new DatagramPacket(receivedData, BUFFER_SIZE);
			try {
				System.out.println("Waiting to receive packet");
				socket.receive(packet);
				RELDATPacket newPacket = RELDATPacket.deserialize(receivedData);
				
				if (RELDATPacket.getCheckSumPacket(newPacket) == newPacket.getChecksum()) {
					Frame frame = new Frame(newPacket, true, packet.getAddress(), packet.getPort());
					System.out.println("Packet checsum is fine");
					addr = packet.getAddress();
					prt = packet.getPort();
					System.out.println(new String(packet.getData()));
					window.addPacket(frame);
					dataSet.add(frame);
					newPacket.setData(processString(new String(newPacket.getData())).getBytes());
					newPacket.setAck(0);
					newPacket.setChecksum();
					packetList.add(newPacket);
				}
				
				if (newPacket.getSrcPort() == dataSet.size()) {
					System.out.println("HELLO EXIT PLEASE");
					break;
				}
			} catch (IOException io) {
				io.printStackTrace();
			}
		}
		try {
			thread.join();
			System.exit(0);
		} catch (InterruptedException e) {

		}

	}


	public int getCurrAckNum() {
        return currAckNum;
    }

    public void setCurrAckNum(int ack) {
        this.currAckNum = ack;
    }
	private static String processString(String input) {
        return input.toUpperCase();
    }

}
