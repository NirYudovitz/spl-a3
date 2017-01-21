package bgu.spl171.net.impl.TFTPreactor;

import bgu.spl171.net.impl.TFTP.BidiProtocol;
import bgu.spl171.net.impl.TFTP.BidiEncoderDecoder;
import bgu.spl171.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("You must enter a port!");
            return;
        }
        int port = Integer.valueOf(args[0]);
        Server.reactor(4,
                port, //port
                () -> new BidiProtocol(), //protocol factory
                ()-> new BidiEncoderDecoder()//message encoder decoder factory
        ).serve();

    }
}
