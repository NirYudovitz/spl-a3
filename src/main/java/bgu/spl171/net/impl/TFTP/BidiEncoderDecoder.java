package bgu.spl171.net.impl.TFTP;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.Packets.*;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.tools.javac.util.ArrayUtils;
import com.sun.tools.javac.util.ByteBuffer;
import sun.jvm.hotspot.runtime.Bytes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;



public class BidiEncoderDecoder implements MessageEncoderDecoder<BasePacket> {
    private short opCode;
    private byte[] byteArr;
    private int counterRead;
    private static final Set<Integer> haveEndByte = new HashSet<Integer>(Arrays.asList(1, 2, 5, 7, 8, 9));

    public BidiEncoderDecoder() {
        System.out.println("inside BidiEncoderDecoder c-tor");
        this.counterRead = 0;
        this.opCode = opCode;
        byteArr = new byte[1 << 10]; // todo size?
    }

    @Override
    public BasePacket decodeNextByte(byte nextByte) {
        byteArr[counterRead] = nextByte;
        counterRead++;
        BasePacket packet = null;
        //handle size of bytearr
        if (counterRead == 2) {
            opCode = getOpCode(Arrays.copyOf(byteArr, 2));

            // directory listing
            if (opCode == 6) {
                return new DIRQPacket();
            } else if (opCode == 10) {
                //disconnect
                return new DISCPacket();
            }
        }


        if (!haveEndByte.contains(opCode) && opCode != 0) {
            if (opCode == 4) {
                return new ACKPacket();
            } else if (opCode == 3) {
                //todo  data packet
            }
        } else if (continueRead(nextByte)) {
            packet = createPacket(opCode, byteArr);
        }
        return packet;
    }


    public BasePacket createPacket(short opCode, byte[] bytes) {
        BasePacket packet = null;
        switch (opCode) {
            case 1:
                packet = new RRQWRQPacket(bytes,opCode);
                break;
            case 2:
                packet = new RRQWRQPacket(bytes,opCode);
                break;
            case 5:
                //todo maybe we don't need this case
                packet = new ERRORPacket((short) 0);
                break;
            case 7:
                packet = new LOGRQPacket(bytes);
                break;
            case 8:
                packet = new DELRQPacket(bytes);
                break;
            case 9:
                packet = new BCASTPacket(bytes);
                break;
        }
        return packet;
    }


    //return true if finish reading
    private boolean continueRead(byte nextByte) {
        BasePacket packet = null;
        if (nextByte != '0') {
            byteArr[counterRead] = nextByte;
            counterRead++;
            return false;
        }
        return true;

    }

    @Override
    public byte[] encode(BasePacket message) {
        opCode = message.getOpCode();
        switch (opCode) {
            case 3:
                //todo data
                break;
            case 4:
                byteArr=encodeACK(message);
                break;
            case 5:
                byteArr=encodeERROR((ERRORPacket)message);
                break;
            case 9:

                break;
        }
        return byteArr;
    }

    public byte[] encodeACK(BasePacket packet){
        byte[] bytes=new byte[4];//todo size
        byte[] opCodeByte=shortToBytes(opCode);
        byte[] blockBytes=shortToBytes(((ACKPacket)packet).getBlockNum());

        System.arraycopy(opCodeByte,0,bytes,0,opCodeByte.length);
        System.arraycopy(blockBytes,0,bytes,opCodeByte.length, blockBytes.length);
        return bytes;
    }
    public byte[] encodeERROR(ERRORPacket packet){
        byte[] bytes=new byte[1000];//todo size
        byte[] opCodeByte=shortToBytes(opCode);
        byte[] errorCode=shortToBytes(packet.getErrorCode());
        byte[] errorMsg=packet.getErrMsg().getBytes();

        System.arraycopy(opCodeByte,0,bytes,0,opCodeByte.length);
        System.arraycopy(errorCode,0,bytes,opCodeByte.length, errorCode.length);
        System.arraycopy(errorMsg,0,bytes,errorCode.length + opCodeByte.length, errorMsg.length);
        bytes[opCodeByte.length+errorCode.length+errorMsg.length]='0';
        //todo function to merge arrays.

        return bytes;
    }

    public short getOpCode(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
}
