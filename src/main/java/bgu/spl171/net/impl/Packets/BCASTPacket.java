package bgu.spl171.net.impl.Packets;



public class BCASTPacket extends BasePacket {
    private String Filename;
    private boolean fileAdded;

    public BCASTPacket(byte[] bytes) {
        this.opCode=9;
        bytyarr=bytes;

    }

    public String getFilename() {
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
}
