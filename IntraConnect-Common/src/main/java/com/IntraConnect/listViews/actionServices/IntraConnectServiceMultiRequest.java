package com.IntraConnect.listViews.actionServices;

import com.IntraConnect.intf.IntraConnectServiceRequest;
import com.IntraConnect.listViews.FieldMeta;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class IntraConnectServiceMultiRequest implements IntraConnectServiceRequest {

    public abstract Object handle(List<LinkedHashMap<String, Object>> payload);

    @Override
    public List<FieldMeta> viewData() {
        return null;
    }
}
