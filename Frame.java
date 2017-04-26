import java.util.Timer;
import java.util.TimerTask;
import java.net.*;
import java.util.*;
import java.io.*;

public class Frame implements Comparable {

	public static final int TIMEOUT = 3000;

	RELDATPacket currFramePack;
	protected static Timer timer = new Timer();
	TimerTask task;
	boolean ackRec;
	boolean hasData;
	boolean isSent;
	InetAddress address;
	int frameSeqNum;
	int port;

	public Frame(RELDATPacket packet, boolean hasData, InetAddress address, int port) {
		this.currFramePack = packet;
		this.hasData = hasData;
		this.isSent = false;
		this.ackRec = false;
		this.address = address;
		this.port = port;
		frameSeqNum = packet.getSeqNumber();
		
	}

	public void cancelTask() {
		if (task != null) {
			task.cancel();
		}
		
	}

	public void startTimer(DatagramSocket socket) {
			task = new TimeoutTask(this, socket);
			timer.schedule(task, TIMEOUT);
		
	}

	class TimeoutTask extends TimerTask {

		Frame frame;
		DatagramSocket socket;

		TimeoutTask(Frame frame, DatagramSocket socket) {
			this.frame = frame;
			this.socket = socket;
		}
	
        @Override
        public void run() {
			// BiDirectionalTransfer.isTimedout = true; //Not necessary because we call System.exit
			// BiDirectionalTransfer.timeOutSeqNum = getFrameSeqNum();
			try {
				if (frame.getAckRec()) {
					task.cancel();
				} else {
					frame.setFrameSeqNum(frame.getFrameSeqNum());	
					RELDATPacket packet = frame.getCurrFramePack();
					packet.setSeqNumber(frame.getFrameSeqNum());
					packet.setAck(0);
					packet.setAckNumber(0);
					packet.setDataLength(packet.getDataLength());
					packet.setChecksum();
					frame.setCurrFramePack(packet);
					byte[] arr = packet.serialize();
					DatagramPacket sendPacket = new DatagramPacket(arr, arr.length, frame.getAddress(), frame.getPort());
					socket.send(sendPacket);
					task.cancel();
					frame.startTimer(socket);
					// BiDirectionalTransfer.isTimedoutNumber = frame.getFrameSeqNum();
					// BiDirectionalTransfer.isTimedout = true;
					// System.out.println("TASK FINISHED");
				}
				
			} catch (IOException io) {
				System.out.println("IO Exception");
			}
			
			
            //System.exit(0); //Stops the AWT thread (and everything else)
        }
    }

	@Override
	public int compareTo(Object frame) {
		Frame other = (Frame) frame;
		if (this.getFrameSeqNum() == other.getFrameSeqNum()) {
			return 0;
		} else if (this.getFrameSeqNum() > other.getFrameSeqNum()) {
			return 1;
		} else {
			return -1;
		}
	}

	public int getFrameSeqNum() {
		return frameSeqNum;
	}

	public void setFrameSeqNum(int framenum) {
		frameSeqNum = framenum;
	}


	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress addr) {
		this.address = addr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public RELDATPacket getCurrFramePack() {
		return currFramePack;
	}

	public void setCurrFramePack(RELDATPacket pack) {
		this.currFramePack = pack;
	}

	public boolean getAckRec() {
		return ackRec;
	}

	public void setAckRec(boolean ackRec) {
		this.ackRec = ackRec;
	}

	public boolean getHasData() {
		return hasData;
	}

	public void setHasData(boolean hasData) {
		this.hasData = hasData;
	}

	public boolean getIsSent() {
		return isSent;
	}

	public void setIsSent(boolean sent) {
		this.isSent = sent;
	}

	public TimerTask getTask() {
		return task;
	}
	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}
}
