package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class ERRORPacket extends BasePacket {
    private String ErrMsg;
    private short endByte;
    private int errorType;
    private short ErrorCode;

    public ERRORPacket(int errorType) {
        this.opCode = 5;
        this.errorType = errorType;
        difineErrMsg();
    }

    private void difineErrMsg() {
        switch (errorType) {
            case 0:
                ErrMsg = "Not defined, see error message (if any).";
                break;
            case 1:
                ErrMsg = "File not found – RRQ \\ DELRQ of non-existing file.";
                break;
            case 2:
                ErrMsg = "Access violation – File cannot be written, read or deleted.";
                break;
            case 3:
                ErrMsg = "Disk full or allocation exceeded – No room in disk.";
                break;
            case 4:
                ErrMsg = "Illegal TFTP operation – Unknown Opcode.";
                break;
            case 5:
                ErrMsg = "File already exists – File name exists on WRQ.";
                break;
            case 6:
                ErrMsg = "User not logged in – Any opcode received before Login completes.";
                break;
            case 7:
                ErrMsg = "User already logged in – Login username already connected.";
                break;

        }
    }

    @Override
    public boolean haveEndByte(){
        return true;
    }


}
