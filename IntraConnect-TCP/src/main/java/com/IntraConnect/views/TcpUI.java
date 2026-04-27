package com.IntraConnect.views;

import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.views.requests.ControllerDetails;
import com.IntraConnect.views.requests.DataReprocess;
import com.IntraConnect.views.requests.DeleteController;
import com.IntraConnect.views.requests.ReconnectController;
import com.IntraConnect.intf.IntraConnectViewFactory;
import com.IntraConnect.listViews.Buttons;
import com.IntraConnect.listViews.IntraConnectView;
import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectViewDetails;

import java.util.List;


public class TcpUI extends IntraConnectView {

    @Override
    public List<MenuItem> listViews() {

        List<MenuItem> items = List.of(
                new MenuItem("connectable", createTableController()),
                new MenuItem("transfer-in",createTransferInController()),
                new MenuItem("transfer-out", createTransferOutController()),
                new MenuItem("transfer-in-out", createTransferInOut())
        );

        return items;
    }

    private IntraConnectViewDetails createTransferInController() {

        String query= "SELECT TRANSFER_IN.ID, CONTROLLER.NAME, TRANSFER_IN._date, TRANSFER_IN.processed, TRANSFER_IN.content " +
                "FROM TRANSFER_IN LEFT JOIN CONTROLLER ON (CONTROLLER.ID = TRANSFER_IN.CONTROLLER_ID)";

        return IntraConnectViewDetails.tableView()
                .query(query)
                .addViewButton(Buttons.CUSTOM,"Erneut-verarbeiten", new DataReprocess())
                .build();
    }
    private IntraConnectViewDetails createTransferOutController() {

        String query= "SELECT TRANSFER_OUT.ID, CONTROLLER.NAME, TRANSFER_OUT._date, TRANSFER_OUT.processed, TRANSFER_OUT.content " +
                "FROM TRANSFER_OUT LEFT JOIN CONTROLLER ON (CONTROLLER.ID = TRANSFER_OUT.CONTROLLER_ID)";

        return IntraConnectViewDetails.tableView()
                .query(query)
                .addViewButton(Buttons.CUSTOM,"Erneut verarbeiten", new DataReprocess())
                .build();
    }

    private IntraConnectViewDetails createTransferInOut() {

        String query= "SELECT TRANSFER_OUT.ID, CONTROLLER.NAME, TRANSFER_OUT._date, TRANSFER_OUT.processed, TRANSFER_OUT.content " +
                "FROM TRANSFER_OUT LEFT JOIN CONTROLLER ON (CONTROLLER.ID = TRANSFER_OUT.CONTROLLER_ID)";

        return IntraConnectViewDetails.tableView()
                .query(query)
                .addOpenDetailsButton("Details", new ControllerDetails())
                .addViewButton(Buttons.CUSTOM,"Custom 1", new DataReprocess())
                .addViewButton(Buttons.CUSTOM,"Custom 2", new DataReprocess())
                .build();
    }

    private IntraConnectViewDetails createTableController() {

        String query= "SELECT ID, NAME, DESCRIPTION, CONNECTED FROM CONTROLLER";

        return IntraConnectViewDetails.tableView()
                .query(query)
                .addOpenDetailsButton("detail", new ControllerDetails())
                .addDeleteDetailsButton("Delete", new DeleteController())
                .addViewButton(Buttons.CONNECT,"Reconnect", new ReconnectController())
                .build();
    }

    @Override
    public IntraConnectViewFactory getClassType() {
        return TcpUI::new;
    }
}
