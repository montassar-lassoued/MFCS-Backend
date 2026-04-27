package com.IntraConnect.views.requests;

import com.IntraConnect._enum.Response;
import com.IntraConnect.listViews.actionServices.IntraConnectServiceMultiRequest;

import java.util.LinkedHashMap;
import java.util.List;

public class ReconnectController extends IntraConnectServiceMultiRequest {


    @Override
    public Response handle(List<LinkedHashMap<String, Object>> payload) {
        return Response.OK;
    }

}
