package bgu.spl171.net.impl.TFTP;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.Packets.*;
import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Nirdun on 13.1.2017.
 */
public class BidiEncoderDecoder implements MessageEncoderDecoder<BasePacket> {
    private short opCode;
    private byte[] byteArr;
    private int counterRead;
    private static final Set<Integer> haveEndByte = new HashSet<Integer>(Arrays.asList(1, 2, 5, 7, 8, 9));

    public BidiEncoderDecoder() {
        System.out.println("inside bidiencoderdecoder c-tor");
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
        } else if (continueRead(nextByte)){
            packet= createPacket(opCode,byteArr);
        }
        return packet;
    }


    public BasePacket createPacket(short opCode,byte[] bytes){
        BasePacket packet=null;
        switch (opCode){
            case 1:
                packet= new RRQWRQPacket(bytes);
                break;
            case 2:
                packet= new RRQWRQPacket(bytes);
                break;
            case 5:
                //todo maybe we don't need this case
                packet= new ERRORPacket(0);
                break;
            case 7:
                packet= new LOGRQPacket(bytes);
                break;
            case 8:
                packet= new DELRQPacket(bytes);
                break;
            case 9:
                packet= new BCASTPacket(bytes);
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

    }


    public short getOpCode(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }
}
