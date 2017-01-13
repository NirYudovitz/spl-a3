package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class DATAPacket extends BasePacket {
    short packetSize;
    short blockNum;
    // field from zero to 512 bytes long.
    int data;

    public DATAPacket(){
        this.opCode=3;
    }
}
