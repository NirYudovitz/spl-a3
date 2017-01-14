package bgu.spl171.net.impl.TFTP;

import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.srv.ConnectionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


public class ConnectionsImpl<T> implements Connections<T> {
    private Map<Integer, ConnectionHandler<T>> connectionsMap;
    private Map<Integer, String> logedInMap;
    private Supplier<ConnectionHandler<T>> connectionHandlerFactory;
    private ConcurrentHashMap<String, Boolean> filesCompleted;

    public ConnectionsImpl(Supplier<ConnectionHandler<T>> connectionHandlerFactory) {
        this.connectionHandlerFactory = connectionHandlerFactory;
        connectionsMap = new HashMap<>();
        filesCompleted = new ConcurrentHashMap<>();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if (connectionsMap.containsKey(connectionId)) {
            connectionsMap.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    //todo is file exist
    public void addFile(String fileName) {
        filesCompleted.put(fileName, false);
    }

    public void deleteFile(String fileName) {
        filesCompleted.remove(fileName);
    }

    public void completeFile(String fileName) {
        filesCompleted.put(fileName, true);
    }

    /**
     *
     * @return the files that completed as a string
     */
    public String allCompletedFiles(){
        String files=null;
        for (String key : filesCompleted.keySet()) {
            if(filesCompleted.get(key)){
                files+=key+'\0';
            }
            System.out.println("Key = " + key);
        }
        return files;
    }

    @Override
    public void broadcast(T msg) {
        for (Integer id : logedInMap.keySet()) {
            send(id, msg);
            System.out.println("id is: = " + id);
        }

    }

    @Override
    public void disconnect(int connectionId) {
        try {
            connectionsMap.get(connectionId).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectionsMap.remove(connectionId);
        logedInMap.remove(connectionId);
    }


    public boolean isLogedIn(int connectionId) {
        return logedInMap.containsKey(connectionId);
    }

    public void addConnection(int connectionId) {
        connectionsMap.put(connectionId, connectionHandlerFactory.get());
    }

    public void logIn(int connectionId, String userName) {
        logedInMap.put(connectionId, userName);
    }
}
