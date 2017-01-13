package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class BasePacket {
    protected short opCode;
    protected byte[] bytyarr;
    public short getOpCode() {
        return opCode;
    }

    public BasePacket(byte[] bytes){
        this.bytyarr=bytes;
    }
    public BasePacket(){
        bytyarr=new byte[0];
    }

    public boolean haveEndByte(){
        return false;
    }

    public void setOpCode(short opCode) {
        this.opCode = opCode;
    }
}
