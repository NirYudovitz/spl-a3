package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class RRQWRQPacket extends BasePacket {
    String fileName;
    char endByte;

    public RRQWRQPacket(byte[] bytes){
        this.bytyarr=bytes;

    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public char getEndByte() {
        return endByte;
    }

    public void setEndByte(char endByte) {
        this.endByte = endByte;
    }

    @Override
    public boolean haveEndByte(){
        return true;
    }
}
