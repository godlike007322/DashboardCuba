// initFunctionName в xml: com_company_project_web_ui_components_jscomponent_Calendar
com_company_project_web_ui_components_jscomponent_Calendar = function () {
    var connector = this;
    var element = connector.getElement();

    element.innerHTML = "<div class=\"simple-calendar-container\"></div>";
    var $container = $('.simple-calendar-container', element);

    var $calendar;

    // будет вызываться при каждом изменении state с сервера
    this.onStateChange = function () {
        var data = this.getState().data;
console.log("Calendar connector RU loaded");
        var events = [];
        if (data.eventsJson) {
            try {
                events = JSON.parse(data.eventsJson);
            } catch (e) {
                if (window.console) console.error("Error parsing eventsJson", e);
            }
        }

        // чтобы не плодить несколько календарей
        $container.empty();

        $calendar = $container.simpleCalendar({
         months: ['январь','февраль','март','апрель','май','июнь','июль','август','сентябрь','октябрь','ноябрь','декабрь'],
                    days: ['Вс','Пн','Вт','Ср','Чт','Пт','Сб'],
            displayYear: data.displayYear,
            displayEvent: data.displayEvent,
            disableEventDetails: data.disableEventDetails,
            disableEmptyDetails: data.disableEmptyDetails,
            fixedStartDay: true,
            events: events,

            onDateSelect: function (date, evts) {
                // дергаем функцию, которую добавили в Java (calendar.addFunction("onDateSelect", ...))
                if (connector.onDateSelect) {
                    connector.onDateSelect(date.toISOString());
                }
            },

            onEventSelect: function () {
                var ev = $(this).data('event');
                if (connector.onEventSelect && ev) {
                    connector.onEventSelect(JSON.stringify(ev));
                }
            }
        });

        // пример функции, которую можно вызвать с сервера
        connector.showToday = function () {
            var today = new Date();
            alert("Today is: " + today.toDateString());
        };
    };
};
