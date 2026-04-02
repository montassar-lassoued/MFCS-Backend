package com.IntraConnect.views.requests;

import com.IntraConnect.listViews.FieldMeta;
import com.IntraConnect.listViews.actionServices.PilotServiceSingleRequest;

import java.util.List;
import java.util.Map;

public class DeleteController extends PilotServiceSingleRequest {


    @Override
    public Object handle(Map<String, Object> payload) {
        return null;
    }

    @Override
    public List<FieldMeta> viewData() {
        return List.of();
    }
}
