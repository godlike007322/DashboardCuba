package com.company.untitled16.web.screens;

import com.company.untitled16.entity.News;
import com.company.untitled16.web.screens.news.NewsEdit;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.VBoxLayout;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.components.CssLayout;

import javax.inject.Inject;
import java.text.SimpleDateFormat;

@UiController("untitled16_NewsWidget")
@UiDescriptor("news-widget.xml")
public class NewsWidget extends ScreenFragment {
    @Inject
    private CollectionLoader<News> newsDl;
    @Inject
    private VBoxLayout newsBox;
    @Inject
    private CollectionContainer<News> newsDc;
    @Inject
    private UiComponents uiComponents;
    @Inject
    private ScreenBuilders screenBuilders;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd.MM.yyyy");


    @Subscribe
    public void onInit(InitEvent event) {
        newsDl.setMaxResults(5);
        newsDl.load();
        renderNews();
        

    }

    private void renderNews() {
        newsBox.removeAll();

        if (newsDc.getItems().isEmpty()) {
            Label<String> emptyLabel = uiComponents.create(Label.TYPE_STRING);
            emptyLabel.setValue("Новостей пока нет");
            emptyLabel.setStyleName("news-empty");
            newsBox.add(emptyLabel);
            return;
        }
        for (News news : newsDc.getItems()) {
            // Карточка новости – вертикальный контейнер
            VBoxLayout card = uiComponents.create(VBoxLayout.NAME);
            card.setStyleName("news-card");
            card.setWidthFull();
            card.setSpacing(true);

            // Заголовок
            Label<String> titleLabel = uiComponents.create(Label.TYPE_STRING);
            titleLabel.setValue(news.getTitle());
            titleLabel.setStyleName("news-title");

            // Дата
            Label<String> dateLabel = uiComponents.create(Label.TYPE_STRING);
            if (news.getPublishDate() != null) {
                dateLabel.setValue(dateFormat.format(news.getPublishDate()));
            } else {
                dateLabel.setValue("");
            }
            dateLabel.setStyleName("news-date");

            // Короткий текст
            Label<String> textLabel = uiComponents.create(Label.TYPE_STRING);
            textLabel.setValue(news.getShortText());
            textLabel.setStyleName("news-text");

            card.add(titleLabel);
            card.add(dateLabel);
            card.add(textLabel);

            // >>> КЛИК ПО КАРТОЧКЕ – ОТКРЫВАЕМ ЭКРАН РЕДАКТИРОВАНИЯ В ДИАЛОГЕ <<<
            card.addLayoutClickListener(clickEvent -> {
                MapScreenOptions options = new MapScreenOptions(
                        ParamsMap.of("viewOnly", true)
                );

                NewsEdit editor = (NewsEdit) screenBuilders
                        .editor(News.class, this)
                        .withScreenClass(NewsEdit.class)   // явно говорим, какой экран
                        .editEntity(news)
                        .withOpenMode(OpenMode.DIALOG)
                        .withOptions(options)
                        .build();

                editor.show();
            });
            newsBox.add(card);
        }
    }
}
