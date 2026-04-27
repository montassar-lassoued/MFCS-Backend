package com.IntraConnect.listViews.actionServices;

import com.IntraConnect._enum.Response;
import com.IntraConnect.intf.IntraConnectServiceRequest;
import com.IntraConnect.listViews.FieldMeta;

import java.util.List;
import java.util.Map;

public abstract class IntraConnectServiceSingleRequest implements IntraConnectServiceRequest<Map<String, Object>> {
	
	@Override
    public abstract Response handle(Map<String, Object> payload);
	
	@Override
	public List<FieldMeta> viewData() {
		return List.of();
	}
}
