package bgu.spl171.net.impl.TFTP;


import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.Packets.*;
import bgu.spl171.net.srv.BaseServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BaseProtocol implements BidiMessagingProtocol<BasePacket> {
    private MessageEncoderDecoder messageEncoderDecoder;
    private ConnectionsImpl<BasePacket> connections;
    private int connectionId;
    private boolean shuoldTerminate;
    private HashMap<Short,DATAPacket> dataMap;


    public BaseProtocol(){

    }

    @Override
    public void start(int connectionId, Connections<BasePacket> connections) {
        this.connectionId=connectionId;
        this.connections=(ConnectionsImpl) connections;
        shuoldTerminate=false;
        ((ConnectionsImpl) connections).addConnection(connectionId);
        dataMap=new HashMap<>();

    }

    @Override
    public void process(BasePacket message) {

        //todo delete comment.
       // short opCode = getOpCode(Arrays.copyOf(byteMessage, 2));

        short opCode = message.getOpCode();
        switch (opCode) {
            case 1:

                break;
            case 2:
                //Path path=
                //check if file doesnt exist
                connections.send(connectionId,new ACKPacket());
                break;
            case 3:
                writeData((DATAPacket) message);
                break;
            case 6:
                break;
            case 7:
                boolean Connected=connections.isLogedIn(connectionId);
                if(!Connected){
                    connections.logIn(connectionId,((LOGRQPacket)message).getUserName());
                    connections.send(connectionId,new ACKPacket());
                }else {
                    //todo specific error
                    connections.send(connectionId,new ERRORPacket((short) 7));
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


    public boolean writeData(DATAPacket dpacket){
        if(dpacket.getPacketSize()==512){
            dataMap.put(dpacket.getBlockNum(),dpacket);
        }else{

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
