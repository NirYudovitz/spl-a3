package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class ERRORPacket extends BasePacket {
    String ErrMsg;
    short endByte;
    short ErrorCode;


    public ERRORPacket(){
        this.opCode=5;
    }
}
