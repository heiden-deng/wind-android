package com.windchat.im.socket;

import com.google.protobuf.AbstractMessageLite;
import com.windchat.proto.core.CoreProto;

public class TransportPackageForRequest extends TransportPackage {

    public TransportPackageForRequest(String action, byte[] data) throws Exception {
        super(action, data);
    }

    public TransportPackageForRequest(String action, CoreProto.TransportPackageData data) {
        this.action = action;
        this.data = data;
    }

    public TransportPackageForRequest(String action, AbstractMessageLite request) {
        CoreProto.TransportPackageData pData = CoreProto.TransportPackageData.newBuilder()
                .setData(request.toByteString())
                .build();
        this.action = action;
        this.data = pData;
    }


}
