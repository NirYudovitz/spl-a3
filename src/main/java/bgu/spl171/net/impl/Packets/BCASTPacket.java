package bgu.spl171.net.impl.Packets;



public class BCASTPacket extends BasePacket {
    private String Filename;
    private boolean fileAdded;

    public BCASTPacket(byte[] bytes) {
        this.opCode=9;
        bytyarr=bytes;

    }
    public BCASTPacket(byte[] bytes,short addOrDelete,String filename) {
        this.opCode=9;
        this.bytyarr=bytes;
        this.fileAdded= addOrDelete==1 ? true : false;
        this.Filename=filename;

    }
    public BCASTPacket(String filename) {
        this.opCode=9;
        this.Filename=filename;

    }

    public String getFileName() {
        return Filename;
    }

    public void setFilename(String filename) {
        Filename = filename;
    }

    public boolean isFileAdded() {
        return fileAdded;
    }

    public void setFileAdded(boolean fileAdded) {
        this.fileAdded = fileAdded;
    }

    @Override
    public boolean haveEndByte(){
        return true;
    }
}
