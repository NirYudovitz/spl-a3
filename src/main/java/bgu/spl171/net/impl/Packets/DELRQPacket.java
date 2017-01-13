package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class DELRQPacket extends BasePacket {
    String fileName;
    char endByte;

    public DELRQPacket(byte[] bytes){
        this.opCode=8;
        bytyarr = bytes;
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
