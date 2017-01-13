package bgu.spl171.net.impl.TFTP;


import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.Packets.ACKPacket;
import bgu.spl171.net.impl.Packets.BasePacket;
import bgu.spl171.net.impl.Packets.ERRORPacket;
import bgu.spl171.net.impl.Packets.LOGRQPacket;
import bgu.spl171.net.srv.BaseServer;

import java.util.ArrayList;
import java.util.Arrays;

public class BaseProtocol implements BidiMessagingProtocol<BasePacket> {
    MessageEncoderDecoder messageEncoderDecoder;
    ConnectionsImpl<BasePacket> connections;
    int connectionId;
    boolean shuoldTerminate;

    public BaseProtocol(){
        shuoldTerminate=false;
    }

    @Override
    public void start(int connectionId, Connections<BasePacket> connections) {
        this.connectionId=connectionId;
        this.connections=(ConnectionsImpl) connections;

    }

    @Override
    public void process(BasePacket message) {
        byte[] byteMessage = messageEncoderDecoder.encode(message);
        //todo check copy.
       // short opCode = getOpCode(Arrays.copyOf(byteMessage, 2));
        short opCode = message.getOpCode();
        switch (opCode) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 6:
                break;
            case 7:
                boolean Connected=connections.isConnected(connectionId);
                if(!Connected){
                    connections.addConnection(connectionId);
                    connections.send(connectionId,new ACKPacket());
                }else {
                    //todo specific error
                    connections.send(connectionId,new ERRORPacket(7));
                }
                break;
            case 8:
                //A DELRQ packet is used to request the deletion of a file in the server.
            case 10:
                connections.send(connectionId,new ACKPacket());
                connections.disconnect(connectionId);
                break;
            default:


        }

    }

    @Override
    public boolean shouldTerminate() {
        return shuoldTerminate;
    }

    public short getOpCode(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }
}
