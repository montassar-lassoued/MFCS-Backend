package com.IntraConnect.intf;

import com.IntraConnect._enum.Response;
import com.IntraConnect.listViews.FieldMeta;

import java.util.List;

public interface IntraConnectServiceRequest <T>{
	Response handle(T payload);
    List<FieldMeta> viewData();
}
