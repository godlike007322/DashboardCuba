com_haulmont_sampler_web_ui_components_jscomponent_DatePicker = function () {
    var connector = this;
    var element = connector.getElement();
    element.innerHTML = "<input type='text' class='datepicker'/>";

    var input = element.querySelector(".datepicker");
    var calendar;

    this.onStateChange = function () {
        var data = this.getState().data;

        calendar = flatpickr(input, {
            dateFormat: data.dateFormat || "Y-m-d",
            defaultDate: data.defaultDate || null,
            onChange: function(selectedDates, dateStr) {
                connector.valueChanged(dateStr);
            }
        });

        connector.showValue = function () {
            input.value = calendar.input.value;
        };
    };
};
