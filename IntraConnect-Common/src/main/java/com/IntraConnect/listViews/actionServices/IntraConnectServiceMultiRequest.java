package com.IntraConnect.listViews.actionServices;

import com.IntraConnect._enum.Response;
import com.IntraConnect.intf.IntraConnectServiceRequest;
import com.IntraConnect.listViews.FieldMeta;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class IntraConnectServiceMultiRequest implements IntraConnectServiceRequest<List<LinkedHashMap<String, Object>>> {
	
	@Override
	public abstract Response handle(List<LinkedHashMap<String, Object>> payload);

    @Override
    public List<FieldMeta> viewData() {
        return null;
    }
}
