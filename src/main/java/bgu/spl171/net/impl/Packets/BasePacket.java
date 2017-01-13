package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class BasePacket {
    protected short opCode;

    public short getOpCode() {
        return opCode;
    }

    public void setOpCode(short opCode) {
        this.opCode = opCode;
    }
}
