package bgu.spl171.net.impl.TFTP;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.Packets.*;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.tools.javac.util.ArrayUtils;
import com.sun.tools.javac.util.ByteBuffer;
import sun.jvm.hotspot.runtime.Bytes;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class BidiEncoderDecoder<T> implements MessageEncoderDecoder<BasePacket> {
    private short opCode;
    private int packetSize;
    private byte[] byteArr;
    private int counterRead;
    private static final Set<Integer> haveEndByte = new HashSet<Integer>(Arrays.asList(1, 2, 5, 7, 8, 9));

    //adding end byte to the bytes array
    private final byte[] endByte = new byte[]{0};

    public BidiEncoderDecoder() {
        System.out.println("inside BidiEncoderDecoder c-tor");
        this.counterRead = 0;
        this.opCode = opCode;
        byteArr = new byte[1024]; // todo size?
    }

    @Override
    public BasePacket decodeNextByte(byte nextByte) {
        byteArr[counterRead] = nextByte;
        counterRead++;
        BasePacket packet = null;


        //initialize op code.
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

        if (!haveEndByte.contains((int) opCode) && opCode != 0) {
            if (opCode == 4) {
                return new ACKPacket();
            } else if (opCode == 3) {
                packet = createDataPacket();
            }
        } else if (!shouldContinueRead(nextByte) && opCode != 0) {
            packet = createPacket(opCode, byteArr);
        }
        return packet;
    }

    public DATAPacket createDataPacket() {
        DATAPacket dPacket = null;
        // make a one bigger


        if (counterRead == 4) {
            //size of data and first six bytes.
            packetSize = bytesToShort(byteArr) + 6;
        } else if (counterRead == packetSize) {
            //todo divide packet
            dPacket = new DATAPacket(Arrays.copyOf(byteArr, packetSize));
        }

        return dPacket;
    }

    public BasePacket createPacket(short opCode, byte[] bytes) {
        BasePacket packet = null;
        switch (opCode) {
            //Read request.
            case 1:
                String fileNameWRQ = bytesArrToString((Arrays.copyOfRange(bytes, 2, counterRead-1)));
                packet = new RRQWRQPacket(bytes, opCode,fileNameWRQ);
                String fileName = bytesArrToString((Arrays.copyOfRange(bytes, 2, counterRead-1)));
                ((RRQWRQPacket) (packet)).setFileName(fileName);


                break;
            //Write request
            case 2:
                String fileNameRRQ = bytesArrToString((Arrays.copyOfRange(bytes, 2, counterRead-1)));
                packet = new RRQWRQPacket(bytes, opCode,fileNameRRQ);
                break;
            //Error request.
            case 5:
                //todo - check if insert err msg different from value code optional
                int errorCode = bytesToShort(Arrays.copyOfRange(bytes,2,4));
                packet = new ERRORPacket((short) errorCode);
                break;
            //Login request
            case 7:
                packet = new LOGRQPacket();
                String userName = bytesArrToString((Arrays.copyOfRange(bytes, 2, counterRead - 1)));
                ((LOGRQPacket) (packet)).setUserName(userName);

                break;
            //Delete request
            case 8:
                String fileNameDelrq = bytesArrToString((Arrays.copyOfRange(bytes, 2, counterRead-1)));
                packet = new DELRQPacket(fileNameDelrq);
                break;
            //Broadcast request
            case 9:
                String fileNameBcast = bytesArrToString((Arrays.copyOfRange(bytes, 3, counterRead-1)));
                packet = new BCASTPacket(bytes, (short) bytes[2],fileNameBcast);
                break;
            default:
                System.out.println("Wrong OpCode");
        }
        return packet;
    }


    public String bytesArrToString(byte[] bytes) {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
        return result;
    }

    //return true if finish byte-0 is reading
    private boolean shouldContinueRead(byte nextByte) {
        return (nextByte != 0);
    }

    @Override
    public byte[] encode(BasePacket message) {
        opCode = message.getOpCode();
        switch (opCode) {
            //todo : should be more packets? example DELRQ ?
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
        fileadded[0] = bpacket.isFileAdded() ? (byte) 1 : (byte) 0;

        byte[] fileNameBytes = null;
        //todo utf8
        try {
            fileNameBytes = bpacket.getFileName().getBytes("UTF-8");
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
        byte[] errorMsg = null;
        try {
            errorMsg = packet.getErrMsg().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
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


    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
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
