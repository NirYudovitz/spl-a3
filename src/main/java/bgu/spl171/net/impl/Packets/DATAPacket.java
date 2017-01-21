package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class DATAPacket extends BasePacket {
    short packetSize;
    short blockNum;
    // field from zero to 512 bytes long.
    private byte[] data;
    private String fileName;

    public DATAPacket(byte[] bytes) {
        this.opCode = 3;
        this.data = bytes;
        this.setPacketSize((short) (data.length));
    }
    public DATAPacket(short block,byte[] bytes) {
        this.opCode = 3;
        this.data = bytes;
        setBlockNum(block);
        this.setPacketSize((short) (data.length));
    }

    public short getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(short packetSize) {
        this.packetSize = packetSize;
    }

    public short getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(short blockNum) {
        this.blockNum = blockNum;
    }

    public byte[] getData() {
        //todo - clone?
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
