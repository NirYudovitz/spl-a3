package bgu.spl171.net.impl.Packets;



public class BCASTPacket extends BasePacket {
    private String Filename;

    public BCASTPacket(byte[] bytes) {
        this.opCode=9;
        bytyarr=bytes;

    }
}
