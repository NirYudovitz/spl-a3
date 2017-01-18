package bgu.spl171.net.impl.TFTP;


import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.Packets.*;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;

public class BaseProtocol<T> implements BidiMessagingProtocol<BasePacket> {
    private ConnectionsImpl<BasePacket> connections;
    private int connectionId;
    private boolean shuoldTerminate;
    private HashMap<Short, DATAPacket> dataMap;
    private String fileName;
    private File file;
    private boolean shouldSendMoreData;
    private boolean logedIn;


    public BaseProtocol() {

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


    public int getConnectionId(){
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
                    connections.send(connectionId, new ERRORPacket((short) 6,"user name is alredy exist"));
                }

            } else {
                connections.send(connectionId, new ERRORPacket((short) 6));
            }
        } else {
            switch (opCode) {
                case 1:
                    String currentWriteFileName = ((RRQWRQPacket) message).getFileName();

                    if (fileExist(currentWriteFileName)) {
                        connections.send(connectionId, new ERRORPacket((short) 5));
                    } else {
                        this.fileName = currentWriteFileName;
                        connections.addFile(fileName);
                        connections.send(connectionId, new ACKPacket());
                    }

                    break;
                case 2:
                    //RRQ
                    //                Path path= Paths.get("//Files"+fileName);

                    String currentReadFileName = ((RRQWRQPacket) message).getFileName();
                    if (!fileExist(currentReadFileName)) {
                        connections.send(connectionId, new ERRORPacket((short) 1));
                    } else {
                        this.fileName = currentReadFileName;
                        sendData(0);
                    }
                    //todo log in?

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
                    //todo case files is null
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
                    //todo specific error for already logged in?
                    connections.send(connectionId, new ERRORPacket((short) 7));
                    break;
                case 8:
                    //A DELRQ packet is used to request the deletion of a file in the server.
                    String currentFileNameDelete = ((DELRQPacket) message).getFileName();
                    if (fileExist(currentFileNameDelete)) {
                        Path path = Paths.get("//Files" + fileName);
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
                    }
                case 10:
                    connections.send(connectionId, new ACKPacket());
                    connections.disconnect(connectionId);
                    break;
                default:
                    System.out.println("wrong op code in  process");


            }
        }

    }


    private void sendData(int numBlock) {
        byte[] data = null;
        long leftTosend = (file.length()) - ((numBlock) * 512);
        if (leftTosend > 512) {
            shouldSendMoreData = true;
            data = new byte[512];
        } else {
            shouldSendMoreData = false;
            data = new byte[(int) leftTosend];
        }
        FileInputStream stream = null;
        BufferedInputStream bufStream = null;
        try {
            stream = new FileInputStream(file);
            bufStream = new BufferedInputStream(stream);
            //todo delete comment
//            bufStream.skip((int)(numBlock)*512);
            //add bytes to data array acording to array size.
            bufStream.read(data, (numBlock) * 512, data.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
                bufStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            DATAPacket dataPacket = new DATAPacket(data);
            dataPacket.setBlockNum((short) (numBlock + 1));
            connections.send(connectionId, dataPacket);
        }
    }


    private boolean fileExist(String currentFileName) {
//        file = new File("//Files/" + currentFileName);
//        return file.exists();
        return connections.isFileExist(currentFileName);

    }

    public void writeData(DATAPacket dpacket) {
        dataMap.put(dpacket.getBlockNum(), dpacket);
        if (dpacket.getPacketSize() != 512) {
            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(file);
                int lastBlock = dpacket.getBlockNum();
                for (int i = 1; i <= lastBlock; i++) {
                    System.out.println("writing to file");
                    stream.write(dataMap.get(i).getData());
                }
                connections.completeFile(fileName);
                broadCast(true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
        connections.send(connectionId, new ACKPacket(dpacket.getBlockNum()));

    }

    /**
     * send broadcast message if delete/add file.
     *
     * @param add is true was added,false if need to delete file.
     */
    private void broadCast(boolean add) {
        try {
            byte[] filenamebytes = fileName.getBytes("UTF-8");
            BCASTPacket bcastPacket = new BCASTPacket(filenamebytes);
            bcastPacket.setFileAdded(add);
            connections.broadcast(bcastPacket);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
