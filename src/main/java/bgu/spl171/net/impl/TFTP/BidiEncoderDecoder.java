package bgu.spl171.net.impl.TFTP;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.Packets.*;


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
        this.counterRead = 0;
        this.opCode = opCode;
        byteArr = new byte[1024];
    }


    @Override
    public BasePacket decodeNextByte(byte nextByte) {
        byteArr[counterRead] = nextByte;
        counterRead++;
        BasePacket packet = null;

        if (counterRead == 1) {
            opCode = 0;
        }
        //initialize op code.
        if (counterRead == 2) {
            opCode = getOpCode(Arrays.copyOf(byteArr, 2));

            // directory listing
            if (opCode == 6) {
                counterRead = 0;
                opCode = 0;
                return new DIRQPacket();
            } else if (opCode == 10) {
                //disconnect
                opCode = 0;
                counterRead = 0;
                return new DISCPacket();
            }
        }

        if (!haveEndByte.contains((int) opCode) && opCode != 0) {
            if (opCode == 4) {
                if (counterRead == 4) {
                    counterRead = 0;
                    opCode = 0;
                    byte[] blockNumArr = new byte[2];
                    blockNumArr[0] = byteArr[2];
                    blockNumArr[1] = byteArr[3];
                    short blockNum = bytesToShort(blockNumArr);
                    return new ACKPacket(blockNum);
                }

            } else if (opCode == 3) {
                packet = createDataPacket();
            }
        } else if (!shouldContinueRead(nextByte) && opCode != 0) {
            packet = createPacket(opCode, byteArr);
        }
        if (packet != null) {
            counterRead = 0;
        }
        return packet;
    }

    public DATAPacket createDataPacket() {
        DATAPacket dPacket = null;
        // make a one bigger


        if (counterRead == 4) {
            //size of data and first six bytes.
            byte[] packetSizeArr = new byte[2];
            packetSizeArr[0] = byteArr[2];
            packetSizeArr[1] = byteArr[3];
            packetSize = bytesToShort(packetSizeArr) + 6;
        } else if (counterRead == packetSize) {
            byte[] byteBlockeNumArr = new byte[2];
            byteBlockeNumArr[0] = byteArr[4];
            byteBlockeNumArr[1] = byteArr[5];
            byte[] data = new byte[packetSize - 6];
            for (int i = 0; i < packetSize - 6; i++) {
                data[i] = byteArr[i + 6];
            }
            short blocknum = bytesToShort(byteBlockeNumArr);

            dPacket = new DATAPacket(blocknum, Arrays.copyOf(data, packetSize - 6));
        }

        return dPacket;
    }

    public BasePacket createPacket(short opCode, byte[] bytes) {
        BasePacket packet = null;
        switch (opCode) {
            //Read request.
            case 1:
                String fileNameWRQ = bytesArrToString((Arrays.copyOfRange(bytes, 2, counterRead - 1)));
                packet = new RRQWRQPacket(bytes, opCode, fileNameWRQ);
                String fileName = bytesArrToString((Arrays.copyOfRange(bytes, 2, counterRead - 1)));
                ((RRQWRQPacket) (packet)).setFileName(fileName);


                break;
            //Write request
            case 2:
                String fileNameRRQ = bytesArrToString((Arrays.copyOfRange(bytes, 2, counterRead - 1)));
                packet = new RRQWRQPacket(bytes, opCode, fileNameRRQ);
                break;
            //Error request.
            case 5:
                int errorCode = bytesToShort(Arrays.copyOfRange(bytes, 2, 4));
                packet = new ERRORPacket((short) errorCode);
                break;
            //Login request
            case 7:
                String userName = bytesArrToString((Arrays.copyOfRange(bytes, 2, counterRead - 1)));
                packet = new LOGRQPacket(userName);

                break;
            //Delete request
            case 8:
                String fileNameDelrq = bytesArrToString((Arrays.copyOfRange(bytes, 2, counterRead - 1)));
                packet = new DELRQPacket(fileNameDelrq);
                break;
            //Broadcast request
            case 9:
                String fileNameBcast = bytesArrToString((Arrays.copyOfRange(bytes, 3, counterRead - 1)));
                packet = new BCASTPacket(bytes, (short) bytes[2], fileNameBcast);
                break;
            default:
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
        return (nextByte != '\0');
    }

    @Override
    public byte[] encode(BasePacket message) {
        opCode = message.getOpCode();
        switch (opCode) {
            case 3:
                return encodeDataPacket((DATAPacket) message);
            case 4:
                return encodeACK((ACKPacket) message);
            case 5:
                return encodeERROR((ERRORPacket) message);
            case 9:
                return encodeBCAST((BCASTPacket) message);

            default:
                return null;
        }
    }

    public byte[] encodeDataPacket(DATAPacket dpacket) {
        short packetSize = dpacket.getPacketSize();

        byte[] opCodeByte = shortToBytes(opCode);
        byte[] packetSizeBytes = shortToBytes(packetSize);
        byte[] blockNumberBytes = shortToBytes(dpacket.getBlockNum());
        return mergeArrays(opCodeByte, packetSizeBytes, blockNumberBytes, dpacket.getData());
    }

    public byte[] encodeBCAST(BCASTPacket bpacket) {
        byte[] fileadded = new byte[1];


        byte[] opCodeByte = shortToBytes(opCode);
        fileadded[0] = bpacket.isFileAdded() ? (byte) 1 : (byte) 0;

        byte[] fileNameBytes = null;
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


    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }


    /**
     * merge multiple byre arrays to one array.
     *
     * @param arrays is byte arrays.
     * @return merged Array.
     */
    public static byte[] mergeArrays(byte[]... arrays) {
        // Count the number of arrays passed for merging and the total size of resulting array
        int arrCount = 0;
        int count = 0;
        for (byte[] array : arrays) {
            arrCount++;
            count += array.length;
        }
//        System.out.println("Arrays passed for merging : " + arrCount);
//        System.out.println("Array size of resultig array : " + count);

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
