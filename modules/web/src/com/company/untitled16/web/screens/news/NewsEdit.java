package com.company.untitled16.web.screens.news;


import com.haulmont.cuba.gui.components.DateField;
import com.haulmont.cuba.gui.components.HBoxLayout;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.screen.*;
import com.company.untitled16.entity.News;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;

@UiController("untitled16_News.edit")
@UiDescriptor("news-edit.xml")
@EditedEntityContainer("newsDc")
@LoadDataBeforeShow
public class NewsEdit extends StandardEditor<News> {
    @Inject
    private HBoxLayout editActions;
    @Inject
    private DateField<Date> publishDateField;
    @Inject
    private TextArea<String> shortTextField;
    @Inject
    private TextField<String> titleField;
    @Inject
    private TextArea<String> fullText;

    @Subscribe
    public void onInit(InitEvent event) {
        ScreenOptions options = event.getOptions();
        if (options instanceof MapScreenOptions) {
            Map<String, Object> params = ((MapScreenOptions) options).getParams();
            Boolean viewOnly = (Boolean) params.get("viewOnly");

            if (Boolean.TRUE.equals(viewOnly)) {
                enableViewMode();
            }
        }
    }


    private void enableViewMode() {
        editActions.setVisible(false);
        publishDateField.setEditable(false);
        shortTextField.setEditable(false);
        titleField.setEditable(false);
        fullText.setEditable(false);
    }


}