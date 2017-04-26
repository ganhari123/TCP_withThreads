import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.lang.Integer;
import java.lang.Byte;

public class RELDATPacket {



    protected int syn;
    protected int synack;
    protected int ack;
    protected int fin;
    protected int srcPort;
    protected int dstPort;
    protected int seqNumber;
    protected int ackNumber;
    protected int dataLength;
    protected int receiveWindow;
    protected int checksum;
    protected int lastPacket;
    protected byte[] data;
    protected static int TOTAL_PACKET_SIZE = ((Integer.SIZE * 12) / Byte.SIZE) + (1001);

    public RELDATPacket(int srcPort, int dstPort, int lastPacket,  int seqNumber, int ackNumber,
                        int dataLength, int syn, int synack, int ack, int fin,
                        int receiveWindow, int checksum,
                        byte[] data) {
        this.syn = syn;
        this.synack = synack;
        this.ack = ack;
        this.fin = fin;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.seqNumber = seqNumber;
        this.ackNumber = ackNumber;
        this.dataLength = dataLength;
        this.receiveWindow = receiveWindow;
        this.checksum = checksum;
        this.lastPacket = lastPacket;
        this.data = data;

    }

    public RELDATPacket() {
        this.syn = 0;
        this.synack = 0;
        this.ack = 0;
        this.fin = 0;
        this.srcPort = 0;
        this.dstPort = 0;
        this.seqNumber = 0;
        this.ackNumber = 0;
        this.dataLength = 0;
        this.receiveWindow = 0;
        this.checksum = 0;
        this.lastPacket = 0;
        this.data = new byte[1000];
    }

    public RELDATPacket(int syn, int synack, int ack, int fin) {
        this.syn = syn;
        this.synack = synack;
        this.ack = ack;
        this.fin = fin;
        this.srcPort = 0;
        this.dstPort = 0;
        this.seqNumber = 0;
        this.ackNumber = 0;
        this.dataLength = 0;
        this.receiveWindow = 0;
        this.checksum = 0;
        this.lastPacket = 0;
        this.data = new byte[1000];

    }

    public byte[] serialize() {
        ByteBuffer buf = ByteBuffer.allocate(TOTAL_PACKET_SIZE);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(srcPort);
        buf.putInt(dstPort);
        buf.putInt(lastPacket);
        buf.putInt(seqNumber);
        buf.putInt(ackNumber);
        buf.putInt(dataLength);
        buf.putInt(syn);
        buf.putInt(synack);
        buf.putInt(ack);
        buf.putInt(fin);
        buf.putInt(receiveWindow);
        buf.putInt(checksum);
        buf.put(data, 0, data.length);
        return buf.array();
    }

    public static RELDATPacket deserialize(byte[] packetArray) {
        RELDATPacket newPack = new RELDATPacket();
        ByteBuffer buf = ByteBuffer.wrap(packetArray);
        buf.order(ByteOrder.BIG_ENDIAN);
        newPack.setSrcPort(buf.getInt());
        newPack.setDstPort(buf.getInt());
        newPack.setLastPacket(buf.getInt());
        newPack.setSeqNumber(buf.getInt());
        newPack.setAckNumber(buf.getInt());
        newPack.setDataLength(buf.getInt());
        newPack.setSyn(buf.getInt());
        newPack.setSynack(buf.getInt());
        newPack.setAck(buf.getInt());
        newPack.setFin(buf.getInt());
        newPack.setReceiveWindow(buf.getInt());
        newPack.setChecksum(buf.getInt());
        byte[] tempData = new byte[1000];
        buf.get(tempData, 0, 1000);
        newPack.setData(tempData);
        return newPack;
    }

    public static int getCheckSumPacket(RELDATPacket pack) {
        int sum = 0;
        sum = pack.getSyn() + pack.getSynack() + pack.getAck() + pack.getFin()
                + pack.getSrcPort() + pack.getDstPort() + pack.getSeqNumber() + pack.getAckNumber()
                + pack.getReceiveWindow() + pack.getLastPacket();
        byte[] arr = pack.getData();
        for (int i = 0; i < arr.length; i++) {
            sum = sum + arr[i];
        }

        return sum;
    }

    public void setChecksum() {
        int sum = 0;
        sum = syn + synack + ack + fin
                + srcPort + dstPort + seqNumber + ackNumber
                + receiveWindow + lastPacket;

        for (int i = 0; i < data.length; i++) {
            sum = sum + data[i];
        }

        checksum = sum;
    }


    public String toString() {
        String dataTemp = new String(getData());
        String temp = "Seq Number: " + getSeqNumber() + " | Ack Number: " + getAckNumber() +
            " | Data Length: " + getDataLength() + " | " + getSyn() + " | " + getSynack() + " | " + getAck() + " | " + getFin() + " | " + getLastPacket()
            + " | Check Sum: " + getChecksum() + "\n"; //+ dataTemp;


        return temp;
    }


    public int getSyn() {
        return syn;
    }

    public void setSyn(int syn) {
        this.syn = syn;
    }

    public int getSynack() {
        return synack;
    }

    public void setSynack(int synack) {
        this.synack = synack;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    public int getFin() {
        return fin;
    }

    public void setFin(int fin) {
        this.fin = fin;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    public int getAckNumber() {
        return ackNumber;
    }

    public void setAckNumber(int ackNumber) {
        this.ackNumber = ackNumber;
    }

    public int getReceiveWindow() {
        return receiveWindow;
    }

    public void setReceiveWindow(int receiveWindow) {
        this.receiveWindow = receiveWindow;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public int getLastPacket() {
        return lastPacket;
    }

    public void setLastPacket(int lastPacket) {
        this.lastPacket = lastPacket;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
