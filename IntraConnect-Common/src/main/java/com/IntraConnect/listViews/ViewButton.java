package com.IntraConnect.listViews;

import com.IntraConnect.intf.IntraConnectServiceRequest;

public record ViewButton(Buttons button, String label, IntraConnectServiceRequest requestService) {
}