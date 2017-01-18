package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class LOGRQPacket extends BasePacket {

    private String userName;
    char endByte;

    public LOGRQPacket() {
        this.opCode=7;

    }
    public LOGRQPacket(String userName) {
        this.opCode=7;
        this.userName=userName;

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
