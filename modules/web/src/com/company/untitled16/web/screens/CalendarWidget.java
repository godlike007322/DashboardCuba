package com.company.untitled16.web.screens;

import com.haulmont.addon.dashboard.model.Widget;
import com.haulmont.addon.dashboard.web.annotation.DashboardWidget;
import com.haulmont.addon.dashboard.web.events.DashboardEvent;
import com.haulmont.addon.dashboard.web.widget.RefreshableWidget;
import com.haulmont.cuba.gui.screen.ScreenFragment;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.web.gui.components.JavaScriptComponent;


import javax.inject.Inject;
import java.io.Serializable;

@UiController("untitled16_CalendarWidget")
@UiDescriptor("calendar-widget.xml")
@DashboardWidget(name = "Календарь")
public class CalendarWidget extends ScreenFragment implements RefreshableWidget {

    @Inject
    private JavaScriptComponent calendar;

    @Subscribe
    protected void onInit(InitEvent event) {
        // ---- здесь просто копируешь ту же инициализацию, что уже делал для календаря ----
        CalendarState state = new CalendarState();
        state.displayYear = true;
        state.displayEvent = true;
        state.disableEventDetails = false;
        state.disableEmptyDetails = false;
        state.eventsJson = "[]"; // или подгружаешь реальные события из сервиса/БД

        calendar.setState(state);

        // пример: реакция на выбор даты (мы уже делали onDateSelect в коннекторе)
        calendar.addFunction("onDateSelect", arguments -> {
            String isoDate = arguments.getArguments().getString(0);
            // здесь можешь открыть экран создания заметки на выбранную дату и т.п.
        });
    }

    @Override
    public void refresh(DashboardEvent dashboardEvent) {

    }


    public static class CalendarState implements Serializable {
        public boolean displayYear;
        public boolean displayEvent;
        public boolean disableEventDetails;
        public boolean disableEmptyDetails;
        public String eventsJson;
    }
}