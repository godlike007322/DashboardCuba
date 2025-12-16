package com.company.untitled16.web.screens;

import com.company.untitled16.entity.Notes;
import com.google.gson.Gson;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.app.core.inputdialog.DialogActions;
import com.haulmont.cuba.gui.app.core.inputdialog.DialogOutcome;
import com.haulmont.cuba.gui.app.core.inputdialog.InputParameter;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.web.gui.components.JavaScriptComponent;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;

@UiController("untitled16_DashboardScreenNotes")
@UiDescriptor("dashboard-screen-notes.xml")
public class DashboardScreenNotes extends Screen {

    @Inject private JavaScriptComponent gridJs;
    @Inject private DataManager dataManager;
    @Inject private ScreenBuilders screenBuilders;
    @Inject private Dialogs dialogs;
    @Inject private Notifications notifications;
    @Inject private UiComponents uiComponents;

    private final Gson gson = new Gson();

    // ===== список виджетов (ID должен совпадать с тем, что JS ждёт)
    private enum WidgetKind {
        CLOCK("widget-clock", "Часы"),
        NOTES("widget-notes", "Заметки"),
        CALENDAR("widget-calendar", "Календарь"),
        NEWS("widget-news", "Новости");

        private final String id;
        private final String caption;

        WidgetKind(String id, String caption) {
            this.id = id;
            this.caption = caption;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return caption; // чтобы в LookupField показывалось красиво
        }
    }

    @Subscribe
    public void onInit(InitEvent event) {

        // ===== JS -> Java: открыть окно выбора виджета
        // JS будет передавать JSON-массив текущих id, чтобы скрыть уже добавленные
        gridJs.addFunction("showAddWidgetDialog", cb -> {
            String existingJson = "[]";
            try {
                if (cb.getArguments() != null && cb.getArguments().length() > 0) {
                    existingJson = cb.getArguments().getString(0);
                }
            } catch (Exception ignored) {}

            Set<String> existingIds = new HashSet<>();
            try {
                String[] arr = gson.fromJson(existingJson, String[].class);
                if (arr != null) existingIds.addAll(Arrays.asList(arr));
            } catch (Exception ignored) {}

            showAddWidgetDialog(existingIds);
        });

        // ===== JS -> Java: события на месяц (для календаря)
        gridJs.addFunction("requestNotesForMonth", cb -> {
            int year  = (int) cb.getArguments().getNumber(0);
            int month = (int) cb.getArguments().getNumber(1); // 1..12

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

            gridJs.callFunction("applyCalendarEvents", gson.toJson(events));
        });

        // ===== JS -> Java: список заметок на конкретный день (если у тебя есть виджет заметок по дню)
        gridJs.addFunction("requestNotesForDay", cb -> {
            LocalDate day = LocalDate.parse(cb.getArguments().getString(0));

            List<Notes> notes = dataManager.load(Notes.class)
                    .query("select e from untitled16_Notes e where e.noteDate = :d order by e.createTs desc")
                    .parameter("d", day)
                    .list();

            List<Map<String, Object>> items = new ArrayList<>();
            for (Notes n : notes) {
                items.add(ParamsMap.of(
                        "noteId", n.getId().toString(),
                        "summary", shorten(n.getText(), 120)
                ));
            }

            gridJs.callFunction("applyDayNotes", day.toString(), gson.toJson(items));
        });

        // ===== JS -> Java: открыть заметку
        gridJs.addFunction("openNote", cb -> {
            UUID id = UUID.fromString(cb.getArguments().getString(0));
            Notes note = dataManager.load(Notes.class).id(id).one();

            Screen editor = screenBuilders.editor(Notes.class, this)
                    .editEntity(note)
                    .withOpenMode(OpenMode.DIALOG)
                    .build();

            editor.addAfterCloseListener(e2 -> {
                if (e2.closedWith(StandardOutcome.COMMIT) && note.getNoteDate() != null) {
                    gridJs.callFunction("refreshAfterNoteChange", note.getNoteDate().toString());
                }
            });

            editor.show();
        });

        // ===== JS -> Java: создать заметку на дату
        gridJs.addFunction("createNoteForDate", cb -> {
            LocalDate date = LocalDate.parse(cb.getArguments().getString(0));

            Notes n = dataManager.create(Notes.class);
            n.setNoteDate(date);

            Screen editor = screenBuilders.editor(Notes.class, this)
                    .newEntity(n)
                    .withOpenMode(OpenMode.DIALOG)
                    .build();

            editor.addAfterCloseListener(e2 -> {
                if (e2.closedWith(StandardOutcome.COMMIT)) {
                    gridJs.callFunction("refreshAfterNoteChange", date.toString());
                }
            });

            editor.show();
        });

        // ===== JS -> Java: удалить заметку (с подтверждением)
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

                                gridJs.callFunction("noteDeleted", noteId, isoDate);
                            }),
                            new DialogAction(DialogAction.Type.NO)
                    )
                    .show();
        });
    }

    private void showAddWidgetDialog(Set<String> existingIds) {
        List<WidgetKind> available = new ArrayList<>();
        for (WidgetKind w : WidgetKind.values()) {
            if (!existingIds.contains(w.getId())) available.add(w);
        }

        if (available.isEmpty()) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption("Все виджеты уже добавлены")
                    .show();
            return;
        }

        dialogs.createInputDialog(this)
                .withCaption("Добавить виджет")
                .withParameters(
                        InputParameter.parameter("widget")
                                .withField(() -> {
                                    LookupField<WidgetKind> lf = uiComponents.create(LookupField.of(WidgetKind.class));
                                    lf.setCaption("Виджет");
                                    lf.setOptionsList(available);
                                    lf.setRequired(true);
                                    lf.setWidthFull();
                                    lf.setValue(available.get(0));
                                    return lf;
                                })
                )
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (!closeEvent.closedWith(DialogOutcome.OK)) return;

                    WidgetKind w = closeEvent.getValue("widget");
                    if (w == null) return;

                    // Java -> JS: добавить выбранный виджет
                    gridJs.callFunction("addWidgetById", w.getId());
                })
                .show();
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        gridJs.callFunction("initDashboard");
    }

    private String shorten(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "(без текста)";
        return s.length() <= max ? s : (s.substring(0, max - 1) + "…");
    }
}
