package com.company.untitled16.web.screens;

import com.company.untitled16.entity.Notes;
import com.company.untitled16.web.screens.notes.NotesWithDateBrowse;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.gui.*;
import com.haulmont.cuba.gui.components.CssLayout;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.web.gui.components.JavaScriptComponent;
import com.haulmont.cuba.gui.screen.EditorScreen;
import com.haulmont.cuba.gui.screen.StandardOutcome;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@UiController("untitled16_DashboardScreenNotes")
@UiDescriptor("dashboard-screen-notes.xml")
public class DashboardScreenNotes extends Screen {

    @Inject
    private CssLayout gridRoot;

    @Inject
    private UiComponents uiComponents;

    @Inject
    private Fragments fragments;

    @Inject
    private JavaScriptComponent gridJs;

@Inject
private Dialogs dialogs;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        // 1. CUBA CssLayout, просто задаём styleName, без unwrap
        gridRoot.setStyleName("grid-stack");


        // === Плитка с заметками ===
        CssLayout notesItem = uiComponents.create(CssLayout.NAME);
        notesItem.setStyleName("grid-stack-item");
        notesItem.setId("widget-notes");

        CssLayout notesContent = uiComponents.create(CssLayout.NAME);
        notesContent.setStyleName("grid-stack-item-content");
        notesContent.setSizeFull();
        notesItem.add(notesContent);

// 1) СЕРАЯ ПОЛОСА-РУЧКА СВЕРХУ
        CssLayout notesHandle = uiComponents.create(CssLayout.NAME);
        notesHandle.setStyleName("notes-drag-handle widget-drag-handle");
        notesHandle.setWidth("100%");
        notesHandle.setHeight("24px"); // можешь подстроить

// подпись "Заметки" внутри
        com.haulmont.cuba.gui.components.Label<String> notesCaption =
                uiComponents.create(com.haulmont.cuba.gui.components.Label.NAME);
        notesCaption.setValue("Заметки");
        notesHandle.add(notesCaption);

        notesContent.add(notesHandle);

// 2) Сам виджет заметок (фрагмент с таблицей)
        NotesWidget notesWidget = fragments.create(this, NotesWidget.class);
        notesContent.add(notesWidget.getFragment());

// кладём плитку в грид
        gridRoot.add(notesItem);


        // === НОВАЯ плитка с часами ===
        CssLayout clockItem = uiComponents.create(CssLayout.NAME);
        clockItem.setStyleName("grid-stack-item");
        clockItem.setId("widget-clock");


        CssLayout clockContent = uiComponents.create(CssLayout.NAME);
        // важный класс, по нему JS найдёт часы
        clockContent.setStyleName("grid-stack-item-content clock-widget-container");
        clockContent.setSizeFull();

        clockItem.add(clockContent);

        gridRoot.add(clockItem);

        CssLayout newsItem = uiComponents.create(CssLayout.NAME);
        newsItem.setStyleName("grid-stack-item");
        newsItem.setId("widget-news");

        CssLayout newsContent = uiComponents.create(CssLayout.NAME);
        newsContent.setStyleName("grid-stack-item-content");
        newsContent.setSizeFull();

        newsItem.add(newsContent);

        NewsWidget newsWidget = fragments.create(this, NewsWidget.class);
        newsContent.add(newsWidget.getFragment());

        gridRoot.add(newsItem);


        // === ПЛИТКА С КАЛЕНДАРЁМ ===
        CssLayout calItem = uiComponents.create(CssLayout.NAME);
        calItem.setStyleName("grid-stack-item");
        calItem.setId("widget-calendar");

// общий контент (делаем колонкой: handle + body)
        CssLayout calContent = uiComponents.create(CssLayout.NAME);
        calContent.setStyleName("grid-stack-item-content widget-flex"); // widget-flex добавим в CSS
        calContent.setSizeFull();
        calItem.add(calContent);

// ручка для перетаскивания (важно! у тебя draggable.handle = '.widget-drag-handle')
        CssLayout calHandle = uiComponents.create(CssLayout.NAME);
        calHandle.setStyleName("widget-drag-handle");
        calHandle.setWidth("100%");
        calHandle.setHeight("24px");

        com.haulmont.cuba.gui.components.Label<String> calCaption =
                uiComponents.create(com.haulmont.cuba.gui.components.Label.NAME);
        calCaption.setValue("Календарь");
        calHandle.add(calCaption);

// тело календаря (сюда инициализируем simpleCalendar)
        CssLayout calBody = uiComponents.create(CssLayout.NAME);
        calBody.setStyleName("calendar-widget-container");
        calBody.setSizeFull();

        calContent.add(calHandle);
        calContent.add(calBody);

        gridRoot.add(calItem);


    }

    @Inject
    private Notifications notifications;
    @Inject
    private ScreenBuilders screenBuilders;
    @Inject
    private DataManager dataManager;

    @Subscribe
    public void onInit(InitEvent event) {

        // JS -> Java: дай заметки за месяц, чтобы календарь показал их в своём "окошке"
        gridJs.addFunction("requestNotesForMonth", cb -> {
            int year  = (int) cb.getArguments().getNumber(0);
            int month = (int) cb.getArguments().getNumber(1); // ожидаем 1..12

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end   = start.plusMonths(1);

            List<Notes> notes = dataManager.load(Notes.class)
                    .query("select e from untitled16_Notes e " +
                            "where e.noteDate is not null " +
                            "and e.noteDate >= :start and e.noteDate < :end " +
                            "order by e.noteDate, e.createTs")
                    .parameter("start", start)
                    .parameter("end", end)
                    .list();

            List<Map<String, Object>> events = new ArrayList<>();
            for (Notes n : notes) {
                events.add(ParamsMap.of(
                        "startDate", n.getNoteDate().toString(),
                        "endDate",   n.getNoteDate().toString(),
                        "summary",   shorten(n.getText(), 60),
                        "noteId",    n.getId().toString()
                ));
            }

            String json = new com.google.gson.Gson().toJson(events);
            gridJs.callFunction("applyCalendarEvents", json);
        });

        // JS -> Java: клик по событию в окне календаря -> открыть заметку
        gridJs.addFunction("openNote", cb -> {
            UUID id = UUID.fromString(cb.getArguments().getString(0));
            Notes note = dataManager.load(Notes.class).id(id).one();

            screenBuilders.editor(Notes.class, this)
                    .editEntity(note)
                    .withOpenMode(OpenMode.DIALOG)
                    .show();
        });


        gridJs.addFunction("createNoteForDate", cb -> {
            LocalDate date = LocalDate.parse(cb.getArguments().getString(0));

            Notes n = dataManager.create(Notes.class);
            n.setNoteDate(date);

            MapScreenOptions opts = new MapScreenOptions(ParamsMap.of(
                    "lockDate", true,
                    "fixedDate", date
            ));

            Screen editor = screenBuilders.editor(Notes.class, this)
                    .newEntity(n)
                    .withOpenMode(OpenMode.DIALOG)
                    .withOptions(opts)
                    .build();

            editor.addAfterCloseListener(ev -> {
                if (ev.closedWith(StandardOutcome.COMMIT)) {

                    Notes saved = dataManager.load(Notes.class)
                            .id(n.getId())
                            .one();

                    String summary = shorten(saved.getText(), 60);
                    if (summary == null || summary.trim().isEmpty()) summary = "(без текста)";

                    List<Map<String, Object>> one = new ArrayList<>();
                    one.add(ParamsMap.of(
                            "startDate", saved.getNoteDate().toString(),
                            "endDate",   saved.getNoteDate().toString(),
                            "summary",   summary,
                            "noteId",    saved.getId().toString()
                    ));

                    String json = new com.google.gson.Gson().toJson(one);
                    gridJs.callFunction("applyEventsAndRefresh", json, saved.getNoteDate().toString());

                }
            });

            editor.show();
        });



        gridJs.addFunction("confirmDeleteNote", cb -> {
            String noteId = cb.getArguments().getString(0);
            String isoDate = cb.getArguments().getString(1);

            dialogs.createOptionDialog()
                    .withCaption("Удалить заметку?")
                    .withMessage("Удалить безвозвратно?")
                    .withActions(
                            new DialogAction(DialogAction.Type.YES).withHandler(e -> {
                                UUID id = UUID.fromString(noteId);
                                Notes n = dataManager.load(Notes.class).id(id).optional().orElse(null);
                                if (n != null) dataManager.remove(n);

                                gridJs.callFunction("removeCalendarEvent", noteId, isoDate);
                            }),
                            new DialogAction(DialogAction.Type.NO)
                    )
                    .show();
        });



    }

    private String shorten(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() <= max ? s : (s.substring(0, max - 1) + "…");
    }





    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        gridJs.callFunction("initGrid");
    }
}
