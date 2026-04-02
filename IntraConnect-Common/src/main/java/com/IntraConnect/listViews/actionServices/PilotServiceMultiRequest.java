package com.IntraConnect.listViews.actionServices;

import com.IntraConnect.intf.PilotServiceRequest;
import com.IntraConnect.listViews.FieldMeta;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class PilotServiceMultiRequest implements PilotServiceRequest {

    public abstract Object handle(List<LinkedHashMap<String, Object>> payload);

    @Override
    public List<FieldMeta> viewData() {
        return null;
    }
}
