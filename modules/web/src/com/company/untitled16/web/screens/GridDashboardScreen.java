package com.company.untitled16.web.screens;

import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.web.gui.components.JavaScriptComponent;

import javax.inject.Inject;

@UiController("untitled16_GridDashboardScreen")
@UiDescriptor("grid-dashboard-screen.xml")
public class GridDashboardScreen extends Screen {

    @Inject
    private JavaScriptComponent gridDashboard;

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        gridDashboard.callFunction("initGrid");
    }
}
