package com.IntraConnect.viewCommand;

import com.IntraConnect.listViews.UIButton;

import java.util.LinkedHashMap;
import java.util.List;

public record  ViewListCommand (String menu, UIButton action, List<LinkedHashMap<String,Object>> payload) {}
