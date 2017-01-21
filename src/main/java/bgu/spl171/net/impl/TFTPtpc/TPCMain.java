package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.impl.TFTP.BidiProtocol;
import bgu.spl171.net.impl.TFTP.BidiEncoderDecoder;
import bgu.spl171.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("You must enter a port!");
            return;
        }
        int port = Integer.valueOf(args[0]);
        Server.threadPerClient(port, BidiProtocol::new, BidiEncoderDecoder::new).serve();
    }
}
