package bgu.spl171.net.impl.Packets;

/**
 * Created by Nirdun on 9.1.2017.
 */
public class ERRORPacket extends BasePacket {
    private String ErrMsg;
    private short endByte;
    private short ErrorCode;

    public ERRORPacket(short errorType) {
        this.opCode = 5;
        this.ErrorCode = errorType;
        difineErrMsg();
    }
    public ERRORPacket(short errorType,String errMsg) {
        this.opCode = 5;
        this.ErrorCode = errorType;
        this.ErrMsg=errMsg;
    }

    private void difineErrMsg() {
        switch (ErrorCode) {
            case 0:
                //todo ?
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
            default:
                ErrMsg="Wrong error code insert";

        }
    }

    @Override
    public boolean haveEndByte() {
        return true;
    }


    public String getErrMsg() {
        return ErrMsg;
    }

    public void setErrMsg(String errMsg) {
        ErrMsg = errMsg;
    }

    public short getEndByte() {
        return endByte;
    }

    public void setEndByte(short endByte) {
        this.endByte = endByte;
    }

    public int getErrorType() {
        return ErrorCode;
    }

    public void setErrorType(short errorType) {
        this.ErrorCode = errorType;
    }

    public short getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(short errorCode) {
        ErrorCode = errorCode;
    }
}
