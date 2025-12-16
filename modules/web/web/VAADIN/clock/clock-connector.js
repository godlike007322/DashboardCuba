// initFunctionName в XML: com_company_project_web_ui_components_jscomponent_Clock
com_company_project_web_ui_components_jscomponent_Clock = function () {
    var connector = this;
    var element = connector.getElement();

    // Внутрь компонента помещаем div с часами
    element.innerHTML = "<div class=\"clock-widget\"></div>";
    var $clock = $('.clock-widget', element);

    function pad(n) {
        return n < 10 ? '0' + n : '' + n;
    }

    function updateClock() {
        var now = new Date();
        var hh = pad(now.getHours());
        var mm = pad(now.getMinutes());
        var ss = pad(now.getSeconds());
        $clock.text(hh + ":" + mm + ":" + ss);
    }

    // onStateChange вызовется при инициализации компонента
    this.onStateChange = function () {
        updateClock();

        // чтобы не плодить несколько таймеров, сохраняем интервал в поле connector
        if (!connector._clockInterval) {
            connector._clockInterval = setInterval(updateClock, 1000);
        }
    };
};
