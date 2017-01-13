package bgu.spl171.net.impl.TFTP;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.impl.Packets.BasePacket;
import bgu.spl171.net.impl.Packets.Packet;

/**
 * Created by Nirdun on 13.1.2017.
 */
public class BidiEncoderDecoder implements MessageEncoderDecoder<BasePacket> {
    @Override
    public BasePacket decodeNextByte(byte nextByte) {



    }

    @Override
    public byte[] encode(BasePacket message) {
        return new byte[0];
    }
}
