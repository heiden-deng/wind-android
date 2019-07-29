package com.akaxin.client.socket;

import com.akaxin.proto.core.CoreProto;

/**
 * Created by sssl on 08/06/2018.
 */

public class TransportPackageForResponse extends TransportPackage {

    public TransportPackageForResponse(String action, byte[] data) throws Exception {
        super(action, data);
    }

    public TransportPackageForResponse(String action, CoreProto.TransportPackageData data) throws Exception {
        super(action, data);
    }

}
