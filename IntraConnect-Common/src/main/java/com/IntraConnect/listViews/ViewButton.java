package com.IntraConnect.listViews;

import com.IntraConnect.intf.PilotServiceRequest;

public record ViewButton(Buttons button, String label, PilotServiceRequest requestService) {
}