package bgu.spl171.net.impl.TFTP;


import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.Packets.*;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashMap;

public class BidiProtocol<T> implements BidiMessagingProtocol<BasePacket> {
    private ConnectionsImpl<BasePacket> connections;
    private int connectionId;
    private boolean shuoldTerminate;
    private HashMap<Short, DATAPacket> dataMap;
    private String fileName;
    private File file;
    private boolean shouldSendMoreData;
    private boolean logedIn;


    public BidiProtocol() {

    }

    @Override
    public void start(int connectionId, Connections<BasePacket> connections) {
        this.connectionId = connectionId;
        this.connections = (ConnectionsImpl) connections;
        shuoldTerminate = false;
        dataMap = new HashMap<>();
        shouldSendMoreData = false;
        this.logedIn = false;
    }


    public int getConnectionId() {
        return this.connectionId;
    }

    @Override
    public void process(BasePacket message) {
        //handle conncetin id++
        //handle logrq user name is already exist
        logedIn = connections.isLogedIn(connectionId);
        short opCode = message.getOpCode();
        if (!logedIn) {
            if (opCode == 7) {
                if (!connections.isNameExist(((LOGRQPacket) (message)).getUserName())) {
                    connections.logIn(connectionId, ((LOGRQPacket) message).getUserName());
                    connections.send(connectionId, new ACKPacket());
                } else {
                    connections.send(connectionId, new ERRORPacket((short) 6, "user name is alredy exist"));
                }

            } else {
                connections.send(connectionId, new ERRORPacket((short) 6));
            }
        } else {
            switch (opCode) {
                case 1:
                    String currentReadFileName = ((RRQWRQPacket) message).getFileName();
                    if (!fileExist(currentReadFileName)) {
                        connections.send(connectionId, new ERRORPacket((short) 1));
                    } else {
                        this.fileName = currentReadFileName;
                        file = new File("Files" + File.separator + fileName);
                        sendData(0);
                    }

                    break;
                case 2:
                    String currentWriteFileName = ((RRQWRQPacket) message).getFileName();

                    if (connections.isFileExistORDurinUpload(currentWriteFileName)) {
                        connections.send(connectionId, new ERRORPacket((short) 5));
                    } else {
                        this.fileName = currentWriteFileName;
                        file = new File("Files" + File.separator + fileName);
                        connections.addFile(fileName);
                        connections.send(connectionId, new ACKPacket());
                    }


                    break;
                case 3:
                    writeData((DATAPacket) message);
                    break;
                case 4:
                    if (shouldSendMoreData) {
                        sendData(((ACKPacket) message).getBlockNum());
                    }
                    break;
                case 6:
                    String files = connections.allCompletedFiles();
                    if (files != null) {
                        try {
                            DATAPacket dataPacket = new DATAPacket(files.getBytes("UTF-8"));
                            dataPacket.setBlockNum((short) 1);
                            connections.send(connectionId, dataPacket);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 7:
                    connections.send(connectionId, new ERRORPacket((short) 7));
                    break;
                case 8:
                    //A DELRQ packet is used to request the deletion of a file in the server.
//                    String currentFileNameDelete = ((DELRQPacket) message).getFileName();
                    fileName = ((DELRQPacket) message).getFileName();
                    if (fileExist(fileName)) {
                        Path path = Paths.get("Files" + File.separator + fileName);
//                        file= new File("src/main/java/Files/" + fileName);
                        connections.deleteFile(fileName);

                        try {
                            Files.delete(path);
                        } catch (NoSuchFileException x) {
                            System.err.format("%s: no such" + " file or directory%n", path);
                        } catch (DirectoryNotEmptyException x) {
                            System.err.format("%s not empty%n", path);
                        } catch (IOException x) {
                            // File permission problems are caught here.
                            System.err.println(x);
                        }
                        broadCast(false);
                    } else {
                        connections.send(connectionId, new ERRORPacket((short) 1));
                    }
                    break;
                case 10:
                    connections.send(connectionId, new ACKPacket());
                    connections.disconnect(connectionId);
                    shuoldTerminate=true;
                    break;
                default:

            }
        }

    }


    private void sendData(int numBlock) {

        byte[] data = new byte[512];
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            //add bytes to data array acording to array size.
            stream.skip(numBlock * 512);
            int countRead = stream.read(data, 0, 512);

            if (countRead <= 0) {
                data = new byte[0];
                shouldSendMoreData = false;
            } else if (countRead < 512) {
                data = Arrays.copyOf(data, countRead);
                shouldSendMoreData = false;

            } else if (countRead == 512) {
                shouldSendMoreData = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            connections.send(connectionId,new ERRORPacket((short)2));
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            DATAPacket dataPacket = new DATAPacket(data);
            dataPacket.setBlockNum((short) (numBlock + 1));
            connections.send(connectionId, dataPacket);
        }
    }


    private boolean fileExist(String currentFileName) {
        return connections.isFileExist(currentFileName);

    }

    public void writeData(DATAPacket dpacket) {
//        dataMap.put(dpacket.getBlockNum(), dpacket);
        connections.send(connectionId, new ACKPacket(dpacket.getBlockNum()));

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file, true);
                byte[] data = dpacket.getData();
                stream.write(data, 0, dpacket.getPacketSize());

            if(dpacket.getPacketSize()<512){
                connections.completeFile(fileName);
                broadCast(true);
            }
        } catch (Exception e) {
//            e.printStackTrace();
            connections.send(connectionId,new ERRORPacket((short)2));
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * send broadcast message if delete/add file.
     *
     * @param add is true was added,false if need to delete file.
     */
    private void broadCast(boolean add) {
        BCASTPacket bcastPacket = new BCASTPacket(fileName);
        bcastPacket.setFileAdded(add);
        connections.broadcast(bcastPacket);

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
