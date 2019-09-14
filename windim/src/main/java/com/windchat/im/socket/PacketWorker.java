package com.windchat.im.socket;

import java.net.Socket;

/**
 * Created by yichao on 2017/10/12.
 */

public class PacketWorker {

    private PacketWriter packetWriter;
    private PacketReader packetReader;

    public PacketWorker(Connection conn) {

        if (packetWriter == null) {
            packetWriter = new PacketWriter(conn);
        }
        if (packetReader == null) {
            packetReader = new PacketReader(conn);
        }
    }

    public PacketWriter getPacketWriter() {
        return packetWriter;
    }

    public void start(Socket socket) throws Exception {
        packetReader.startup(socket.getInputStream());
        packetWriter.startup(socket.getOutputStream());
    }

    public void stopAndRelease() {
        try{
            this.packetReader.shutdown();
            this.packetWriter.shutdown();

            this.packetWriter = null;
            this.packetReader = null;
        }catch (Exception e) {
            this.packetWriter = null;
            this.packetReader = null;
        }
    }
}
