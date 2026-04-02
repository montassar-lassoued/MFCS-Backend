package com.IntraConnect.viewCommand;

import com.IntraConnect.listViews.UIButton;

import java.util.LinkedHashMap;

public record ViewCommand(String menu, UIButton action, LinkedHashMap<String,Object> payload) {}
