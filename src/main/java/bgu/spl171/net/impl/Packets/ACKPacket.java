package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class ACKPacket extends BasePacket {
    private short blockNum;

    public short getBlockNum() {
        return blockNum;
    }
}
