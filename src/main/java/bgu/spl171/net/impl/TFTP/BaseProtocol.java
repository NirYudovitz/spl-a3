package bgu.spl171.net.impl.TFTP;


import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;

public class BaseProtocol implements BidiMessagingProtocol{
    @Override
    public void start(int connectionId, Connections connections) {

    }

    @Override
    public void process(Object message) {

    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
