package com.company.untitled16.web.screens;

import com.company.untitled16.web.JavaScriptComponentSample;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.web.gui.components.JavaScriptComponent;

import javax.inject.Inject;
import java.io.Serializable;

@UiController("untitled16_")
@UiDescriptor("new-screen.xml")
public class NewScreen extends Screen {


    @Inject
    private JavaScriptComponent timePicker;

    @Inject
    private JavaScriptComponent calendar;

    @Inject
    private Notifications notifications;


    @Subscribe
    public void onInit(InitEvent event) {
        JavaScriptComponentSample.TimePickerState state = new JavaScriptComponentSample.TimePickerState();
        state.now = "12:35:57";
        state.showSeconds = true;
        state.twentyFour = true;

        timePicker.addFunction("onBeforeShow", callbackEvent ->
                notifications.create()
                        .withCaption("Before Show Event")
                        .withPosition(Notifications.Position.MIDDLE_RIGHT)
                        .show());

        timePicker.addFunction("onShow", callbackEvent ->
                notifications.create()
                        .withCaption("Show Event")
                        .show());

        timePicker.setState(state);



        SimpleCalendarState status = new SimpleCalendarState();
        status.displayYear = true;
        status.displayEvent = true;
        status.disableEventDetails = false;
        status.disableEmptyDetails = false;

        // пример: одно событие на сегодня
        status.eventsJson =
                "[{" +
                        "\"startDate\": \"" + java.time.Instant.now().toString() + "\"," +
                        "\"endDate\": \"" + java.time.Instant.now().toString() + "\"," +
                        "\"summary\": \"Event from CUBA\"" +
                        "}]";

        // функции, которые будет вызывать JS-коннектор
        calendar.addFunction("onDateSelect", callbackEvent -> {
            String isoDate = callbackEvent.getArguments().getString(0);
            notifications.create()
                    .withCaption("Selected date: " + isoDate)
                    .withPosition(Notifications.Position.MIDDLE_RIGHT)
                    .show();
        });

        calendar.addFunction("onEventSelect", callbackEvent -> {
            String eventJson = callbackEvent.getArguments().getString(0);
            notifications.create()
                    .withCaption("Selected event: " + eventJson)
                    .withPosition(Notifications.Position.MIDDLE_RIGHT)
                    .show();
        });

        calendar.setState(status);
    }


    /** state, который CUBA сериализует в this.getState().data в JS */
    public static class SimpleCalendarState implements Serializable {
        public String eventsJson;          // JSON-массив событий
        public boolean displayYear = true;
        public boolean displayEvent = true;
        public boolean disableEventDetails = false;
        public boolean disableEmptyDetails = false;


    }



    @Subscribe("showValueBtn")
    public void onShowValueBtnClick(Button.ClickEvent event) {
        timePicker.callFunction("showValue");

    }

    public static class TimePickerState implements Serializable {
        public String now;           // hh:mm 24 hour format only, defaults to current time
        public boolean twentyFour;   // Display 24 hour format
        public boolean showSeconds;  // Show seconds or not
    }



}