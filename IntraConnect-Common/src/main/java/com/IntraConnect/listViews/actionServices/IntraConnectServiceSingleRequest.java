package com.IntraConnect.listViews.actionServices;

import com.IntraConnect.intf.IntraConnectServiceRequest;
import com.IntraConnect.listViews.FieldMeta;

import java.util.List;
import java.util.Map;

public abstract class IntraConnectServiceSingleRequest implements IntraConnectServiceRequest {
    public abstract Object handle(Map<String, Object> payload);
    protected List<FieldMeta> viewData(Map<String, Object> payload){
        return List.of();
    }
}
