package com.IntraConnect.listViews.viewBuilder;

import com.IntraConnect.listViews.RequestMode;
import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.UIButton;
import com.IntraConnect.listViews.actionServices.PilotServiceMultiRequest;

import java.util.List;

public final class PilotViewButtonFactory {

    private PilotViewButtonFactory() {}

    public static List<UIButton> details(List<ViewButton> buttons) {
        return buttons.stream()
                .map(PilotViewButtonFactory::mapDetails)
                .toList();
    }

    public static List<UIButton> view(List<ViewButton> buttons) {
        return buttons.stream()
                .map(PilotViewButtonFactory::mapView)
                .toList();
    }

    private static UIButton mapDetails(ViewButton dtlButton) {
        return switch (dtlButton.button()) {
            case CREATE -> new UIButton("CREATE", dtlButton.label(), "create", View.DETAILS, RequestMode.SINGLE);
            case EDIT   -> new UIButton("EDIT", dtlButton.label(), "edit", View.DETAILS, RequestMode.SINGLE);
            case OPEN   -> new UIButton("OPEN", dtlButton.label(), "visibility", View.DETAILS, RequestMode.SINGLE);
            case DELETE -> new UIButton("DELETE", dtlButton.label(), "delete_forever", View.DETAILS, RequestMode.SINGLE);
            case CONNECT -> new UIButton("CONNECT", dtlButton.label(), "wifi", View.DETAILS, RequestMode.SINGLE);
            case DISCONNECT -> new UIButton("DISCONNECT", dtlButton.label(), "wifi_off", View.DETAILS, RequestMode.SINGLE);
            case CUSTOM -> new UIButton("CUSTOM", dtlButton.label(), "sort", View.DETAILS, RequestMode.SINGLE);
        };
    }
//settings_remote
    private static UIButton mapView(ViewButton dtlButton) {
        RequestMode mode = RequestMode.SINGLE;
        if( dtlButton.requestService() instanceof PilotServiceMultiRequest){
            mode = RequestMode.MULTI;
        }
        return switch (dtlButton.button()) {
            case CREATE -> new UIButton("CREATE", dtlButton.label(), "add_box", View.MAIN, mode);
            case EDIT   -> new UIButton("EDIT", dtlButton.label(), "edit", View.MAIN, mode);
            case OPEN   -> new UIButton("OPEN", dtlButton.label(), "visibility", View.MAIN, mode);
            case DELETE -> new UIButton("DELETE", dtlButton.label(), "delete_forever", View.MAIN, mode);
            case CONNECT -> new UIButton("CONNECT", dtlButton.label(), "wifi",  View.MAIN, mode);
            case DISCONNECT -> new UIButton("DISCONNECT", dtlButton.label(), "wifi_off",  View.MAIN, mode);
            case CUSTOM -> new UIButton("CUSTOM", dtlButton.label(), "sort",  View.MAIN, mode);
        };
    }
}

