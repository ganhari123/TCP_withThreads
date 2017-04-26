import java.net.*;
import java.util.*;
import java.io.*;

public class RELDATMID {
  public static final int BUFFER_SIZE = 2048;
  public static final int TIMEOUT = 3000;
  public static final int REDOS = 3;
  protected static DatagramSocket socket;
  protected static byte[] receivedData;
  protected static DatagramPacket packet;
  protected static int currSeqNum = 0;
  protected static int currAckNum = 0;
  protected static BiDirectionalTransfer transferItem;
  protected static int serverPortNumber = 8000;

  protected static int windowSize;

  private String cumulativeData;

  public RELDATMID (int serverPortNumber, int windowSize) {
    try {
      this.windowSize = windowSize;
      socket = new DatagramSocket(serverPortNumber);
      cumulativeData = "";
      receivedData = new byte[BUFFER_SIZE];
      StateMachineServer.setCurrState(ServerStates.LISTEN);
      this.serverPortNumber = serverPortNumber;
    } catch (SocketException s) {
      System.out.println("WTF ITS NULL");
      s.printStackTrace();
    }

  }

  public void sendFile() {
      //transferItem.reset(currAckNum, currSeqNum);
      System.out.println("I AM READY TO RECEIVE");
      transferItem.sendFilePackets();

  }

  public void sendFileNow(TreeSet<Frame> packets) {
    try {
      DatagramSocket sock = new DatagramSocket();
      if (sock == null) {
        System.out.println("WOW ITS NULL");
      } else {
        transferItem = new BiDirectionalTransfer(windowSize, 0, 0, sock);
        transferItem.setDataSet(packets);
        transferItem.sendFilePackets();
      }
    } catch (SocketException s) {
      System.out.println("WTF ITS NULL");
      s.printStackTrace();
    }
    
    
    
  }



  public void receiveFile() {
      int count = 0;
      cumulativeData = "";
      transferItem = new BiDirectionalTransfer(windowSize, currAckNum, 0, socket);
      transferItem.receiveFilePacket();
  }



  //initialHandshake
  public void waitForConnection () {
      boolean flag = true;
      boolean receivedResponse = false;
      int tries = 0;
      while(flag) {
        packet = new DatagramPacket(receivedData, BUFFER_SIZE);
        RELDATPacket recPack;
        try {
            if (StateMachineServer.getCurrState() == ServerStates.LISTEN) {
              socket.receive(packet);
              recPack = RELDATPacket.deserialize(receivedData);
              if (recPack.getSyn() == 1 && RELDATPacket.getCheckSumPacket(recPack) == recPack.getChecksum()) {
                StateMachineServer.switchStates();
                recPack.setSyn(0);
                recPack.setAckNumber(recPack.getSeqNumber() + 1);
                recPack.setSeqNumber(currSeqNum++);
                recPack.setSynack(1);
                recPack.setChecksum();

                byte[] arr = recPack.serialize();
                packet = new DatagramPacket(arr,
                                            arr.length,
                                            packet.getAddress(),
                                            packet.getPort());

                socket.send(packet);
                receivedResponse = false;
                tries = 0;
                packet = new DatagramPacket(receivedData, BUFFER_SIZE);
                socket.setSoTimeout(TIMEOUT);
              } else {
                StateMachineServer.setCurrState(ServerStates.LISTEN);
              }
            }

            if (StateMachineServer.getCurrState() == ServerStates.SYN_RCV) {
              do {
                try {
                  socket.receive(packet);
                  recPack = RELDATPacket.deserialize(receivedData);
                  if (recPack.getAck() == 1
                      &&  RELDATPacket.getCheckSumPacket(recPack) == recPack.getChecksum()) {
                    StateMachineServer.switchStates();
                    flag = false;
                    receivedResponse = true;
                    socket.setSoTimeout(0);
                    System.out.println("Connection Made!");
                  } else {
                    StateMachineServer.setCurrState(ServerStates.SYN_RCV);
                  }
                } catch(InterruptedIOException e) {
                    tries++;
                    if (tries == 3) {
                      StateMachineServer.setCurrState(ServerStates.LISTEN);
                    }
                }
              } while (!receivedResponse && (tries < REDOS));
            }
        } catch (IOException e) {
                e.printStackTrace();
        }
      }
  }

}
