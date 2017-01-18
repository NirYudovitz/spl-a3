
import bgu.spl171.net.impl.TFTP.BaseProtocol;
import bgu.spl171.net.impl.TFTP.BidiEncoderDecoder;
import bgu.spl171.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl171.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl171.net.srv.Server;

public class mainServer {
    public static void main(String[] args) {

        if(args.length==0||args.length>1){
            System.out.println("Usage: <port>");
            System.exit(1);
        }
        String s=args[0];
        int port=-1;
        try{
            port=Integer.parseInt(s);
        }catch (NumberFormatException e){
            System.out.println("Usage: <port> only");
            System.exit(1);
        }
        if(port<1||port>65535){
            System.out.println("Usage: <port>  and port should be in range 1 through 65535");
            System.exit(1);
        }
        System.out.println("Thread Per Client server has being started on port:"+port);

        Server.threadPerClient(
                7777, //port
                () -> new BaseProtocol(), //protocol factory
                ()-> new BidiEncoderDecoder()//message encoder decoder factory
        ).serve();

    }
}
