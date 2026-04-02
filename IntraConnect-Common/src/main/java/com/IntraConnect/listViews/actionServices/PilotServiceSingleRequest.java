package com.IntraConnect.listViews.actionServices;

import com.IntraConnect.intf.PilotServiceRequest;
import com.IntraConnect.listViews.FieldMeta;

import java.util.List;
import java.util.Map;

public abstract class PilotServiceSingleRequest implements PilotServiceRequest {
    public abstract Object handle(Map<String, Object> payload);
    protected List<FieldMeta> viewData(Map<String, Object> payload){
        return List.of();
    }
}
