package bgu.spl171.net.impl.TFTP;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.Packets.*;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.tools.javac.util.ArrayUtils;
import com.sun.tools.javac.util.ByteBuffer;
import sun.jvm.hotspot.runtime.Bytes;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class BidiEncoderDecoder implements MessageEncoderDecoder<BasePacket> {
    private short opCode;
    private int packetSize;
    private byte[] byteArr;
    private int counterRead;
    private static final Set<Integer> haveEndByte = new HashSet<Integer>(Arrays.asList(1, 2, 5, 7, 8, 9));

    //adding end byte to the bytes array
    private final byte[] endByte = new byte[]{'0'};

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

        //handle size of byteArr
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
                packet = createDataPacket();
            }
        } else if (!shouldContinueRead(nextByte)) {
            packet = createPacket(opCode, byteArr);
        }
        return packet;
    }

    public DATAPacket createDataPacket() {
        DATAPacket dPacket = null;

        if (counterRead == 4) {
            //size of data and first six bytes.
            packetSize = bytesToShort(byteArr, 2) + 6;
        } else if (counterRead == packetSize) {
            //todo divide packet
            dPacket = new DATAPacket(byteArr);
        }

        return dPacket;
    }

    public BasePacket createPacket(short opCode, byte[] bytes) {
        BasePacket packet = null;
        switch (opCode) {
            case 1:
                packet = new RRQWRQPacket(bytes, opCode);
                break;
            case 2:
                packet = new RRQWRQPacket(bytes, opCode);
                break;
            case 5:
                int errorCode = bytesToShort(bytes, 2);
                packet = new ERRORPacket((short) errorCode);
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
            default:
                System.out.println("Wrong OpCode");
        }
        return packet;
    }


    //return true if finish byte-0 is reading
    private boolean shouldContinueRead(byte nextByte) {
        return (nextByte != '0');
    }

    @Override
    public byte[] encode(BasePacket message) {
        opCode = message.getOpCode();
        switch (opCode) {
            case 3:
                byteArr = encodeDataPacket((DATAPacket) message);
                break;
            case 4:
                byteArr = encodeACK((ACKPacket) message);
                break;
            case 5:
                byteArr = encodeERROR((ERRORPacket) message);
                break;
            case 9:
                byteArr = encodeBCAST((BCASTPacket) message);
                break;
            default:
                System.out.println("Wrong OpCode");
        }
        return byteArr;
    }

    public byte[] encodeDataPacket(DATAPacket dpacket) {
        short packetSize = dpacket.getPacketSize();

        //todo - check id bytes ok
        System.out.println("size of data packet in encodeDataPacket fun : " + packetSize);
        byte[] opCodeByte = shortToBytes(opCode);
        byte[] packetSizeBytes = shortToBytes(packetSize);
        byte[] blockNumberBytes = shortToBytes(dpacket.getBlockNum());
        return mergeArrays(opCodeByte, packetSizeBytes, blockNumberBytes, dpacket.getData());
    }

    public byte[] encodeBCAST(BCASTPacket bpacket) {
        byte[] fileadded = new byte[1];


        byte[] opCodeByte = shortToBytes(opCode);
        ////todo check true false
        fileadded[0] = bpacket.isFileAdded() ? (byte) '1' : (byte) '0';

        byte[] fileNameBytes = null;
        //todo utf8
        try {
            fileNameBytes = bpacket.getFilename().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return mergeArrays(opCodeByte, fileadded, fileNameBytes, endByte);
    }


    public byte[] encodeACK(ACKPacket packet) {
        byte[] opCodeByte = shortToBytes(opCode);
        byte[] blockBytes = shortToBytes(packet.getBlockNum());

        //todo delete comments
//        System.arraycopy(opCodeByte, 0, bytes, 0, opCodeByte.length);
//        System.arraycopy(blockBytes, 0, bytes, opCodeByte.length, blockBytes.length);
        return mergeArrays(opCodeByte, blockBytes);
    }

    public byte[] encodeERROR(ERRORPacket packet) {
        byte[] opCodeByte = shortToBytes(opCode);
        byte[] errorCode = shortToBytes(packet.getErrorCode());
        byte[] errorMsg=null;
        try {
            errorMsg = packet.getErrMsg().getBytes("UTF-8");
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }


        return mergeArrays(opCodeByte, errorCode, errorMsg, endByte);


        //todo delete comments
//        System.arraycopy(opCodeByte, 0, bytes, 0, opCodeByte.length);
//        System.arraycopy(errorCode, 0, bytes, opCodeByte.length, errorCode.length);
//        System.arraycopy(errorMsg, 0, bytes, errorCode.length + opCodeByte.length, errorMsg.length);
//        bytes[opCodeByte.length + errorCode.length + errorMsg.length] = '0';
        //todo function to merge arrays.
    }

    public short getOpCode(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }


    private short bytesToShort(byte[] byteArr, int startPos) {
        System.out.println("inside byte to short- check");
        short result = (short) ((byteArr[startPos] & 0xff) << 8);
        result += (short) (byteArr[startPos + 1] & 0xff);
        return result;
    }


    /**
     * merge multiple byre arrays to one array.
     *
     * @param arrays is byte arrays.
     * @return merged Array.
     */
    //todo check if private?
    public static byte[] mergeArrays(byte[]... arrays) {
        // Count the number of arrays passed for merging and the total size of resulting array
        int arrCount = 0;
        int count = 0;
        for (byte[] array : arrays) {
            arrCount++;
            count += array.length;
        }
        System.out.println("Arrays passed for merging : " + arrCount);
        System.out.println("Array size of resultig array : " + count);

        // Create new array and copy all array contents
        int start = 0;
        byte[] mergedArray = new byte[count];
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, mergedArray, start, array.length);
            start += array.length;
        }
        return mergedArray;
    }


}
