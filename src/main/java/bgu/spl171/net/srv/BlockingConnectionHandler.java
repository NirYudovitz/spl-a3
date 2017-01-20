package bgu.spl171.net.srv;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.MessagingProtocol;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.impl.Packets.BasePacket;
import bgu.spl171.net.impl.TFTP.BidiEncoderDecoder;
import bgu.spl171.net.impl.TFTP.ConnectionsImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

//TPC
public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private ConnectionsImpl connections;
    private int connectionId;


    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol,
                                     ConnectionsImpl connections,int connectionId) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connectionId=connectionId;
        this.connections=connections;
    }

    @Override
    public void run() {

        protocol.start(connectionId,connections);
        connections.addConnection(connectionId,this);

        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {

                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        try { //just for automatic closing

            out = new BufferedOutputStream(sock.getOutputStream());
            out.write(encdec.encode(msg));
            out.flush();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
