package com.windchat.im.socket;


import com.windchat.proto.core.CoreProto;

import java.nio.ByteBuffer;

public class TransportPackage {

    public String protoVersion = "1";
    public String action;
    public CoreProto.TransportPackageData data = null;

    // 这个定义在这个类里不是最合适，最好在Request类里，但是先兼容一下
    public boolean isRequestAndResponseMode = false;

    public TransportPackage(String action, byte[] data) throws Exception {
        this.action = action;
        this.data = CoreProto.TransportPackageData.parseFrom(data);
    }

    public TransportPackage(String action, CoreProto.TransportPackageData data) {
        this.action = action;
        this.data = data;
    }

    public TransportPackage() {

    }

    public byte[] toByteArray() {

        byte[] serializedData;
        if (null == this.data) {
            serializedData = "".getBytes();
        } else {
            serializedData = this.data.toByteArray();
        }
//        byte[] serializedData = null == this.data ? "".getBytes() : this.data.toByteArray();

        String segHeader = "*3\r\n";
        String segProtocolVersion = "$" + this.protoVersion.length() + "\r\n" + this.protoVersion + "\r\n";
        String segAction = "$" + this.action.length() + "\r\n" + this.action + "\r\n";
        String segDataPrefix = "$" + serializedData.length + "\r\n";

        int length = segHeader.length() + segProtocolVersion.length() + segAction.length() + segDataPrefix.length();
        length = length + serializedData.length + 2;

        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(segHeader.getBytes());
        buffer.put(segProtocolVersion.getBytes());
        buffer.put(segAction.getBytes());

        buffer.put(segDataPrefix.getBytes());
        buffer.put(serializedData);
        buffer.put("\r\n".getBytes());


        byte[] ret = buffer.array();
        return ret;
    }
}
