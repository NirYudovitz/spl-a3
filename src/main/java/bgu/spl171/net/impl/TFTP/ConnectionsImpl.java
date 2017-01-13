package bgu.spl171.net.impl.TFTP;

import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.srv.ConnectionHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ConnectionsImpl<T> implements Connections<T> {
    private Map<Integer, ConnectionHandler<T>> connectionsMap;

    public ConnectionsImpl() {
        this.connectionsMap = new HashMap<>();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        connectionsMap.get(connectionId).send(msg);


    }

    @Override
    public void broadcast(T msg) {
        for (Integer id : connectionsMap.keySet()) {
            send(id, msg);
            System.out.println("id is: = " + id);
        }

    }

    @Override
    public void disconnect(int connectionId) {
        connectionsMap.remove(connectionId);
    }

    public void addConnection(int connectionId){
        connectionsMap.put(connectionId, new ConnectionHandler<T>());
    }
}
