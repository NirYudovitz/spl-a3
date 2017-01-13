package bgu.spl171.net.impl.TFTP;

import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.srv.ConnectionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;


public class ConnectionsImpl<T> implements Connections<T> {
    private Map<Integer, ConnectionHandler<T>> connectionsMap;
    private Supplier<ConnectionHandler<T>> connectionHandlerFactory;

    public ConnectionsImpl(Supplier<ConnectionHandler<T>> connectionHandlerFactory) {
        this.connectionHandlerFactory = connectionHandlerFactory;
        connectionsMap = new HashMap<>();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if(connectionsMap.containsKey(connectionId)) {
            connectionsMap.get(connectionId).send(msg);
            return true;
        }
        return  false;
    }

    @Override
    public void broadcast(T msg) {
        for (Integer id : connectionsMap.keySet()) {
            send(id, msg);
            System.out.println("id is: = " + id);
        }

    }

    @Override
    public void disconnect(int connectionId) throws IOException {
        try {
            connectionsMap.get(connectionId).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectionsMap.remove(connectionId);
    }

    public void addConnection(int connectionId){
        connectionsMap.put(connectionId, connectionHandlerFactory.get());
    }
}
