package com.company.untitled16.web.screens;

import com.company.untitled16.entity.Notes;
import com.company.untitled16.entity.News;
import com.company.untitled16.service.RecentDocsService;
import com.google.gson.Gson;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.web.gui.components.JavaScriptComponent;
import org.jsoup.Jsoup;

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
    @Inject private RecentDocsService recentDocService;

    private final Gson gson = new Gson();

    private String toPlainText(String html) {
        if (html == null) return "";
        String text = Jsoup.parse(html).text();
        text = text.replace('\u00A0', ' ');
        return text.trim();
    }

    private void sendRecentToJs(int limit) {
        List<RecentDocsService.RecentDocInfo> recents = recentDocService.loadLast(limit);

        List<Map<String, Object>> items = new ArrayList<>();
        for (RecentDocsService.RecentDocInfo r : recents) {
            items.add(ParamsMap.of(
                    "entityName", r.getEntityName(),
                    "entityId", r.getEntityId() != null ? r.getEntityId().toString() : null,
                    "caption", r.getCaption(),
                    // чтобы JS делал new Date(ms)
                    "visitedTs", r.getVisitedTs() != null ? r.getVisitedTs().getTime() : null
            ));
        }

        gridJs.callFunction("applyRecentDocs", gson.toJson(items));
    }
    private void handleMissingRecent(String entityName, UUID id, String caption) {
        dialogs.createMessageDialog()
                .withCaption("Недоступно")
                .withMessage(caption + " Документ недоступен. Документ будет удален из списка последних.")
                .show();

        recentDocService.remove(entityName, id); // метод remove добавим в сервис
        sendRecentToJs(10);
    }


    @Subscribe
    public void onInit(InitEvent event) {

        // ===== RECENT: JS -> Java: запросить последние документы
        gridJs.addFunction("requestRecentDocs", cb -> {
            int limit = 10;
            try {
                if (cb.getArguments() != null && cb.getArguments().length() > 0) {
                    limit = (int) cb.getArguments().getNumber(0);
                }
            } catch (Exception ignored) {}
            sendRecentToJs(limit);
        });

        // ===== RECENT: JS -> Java: открыть документ из списка
        gridJs.addFunction("openRecentDoc", cb -> {
            String entityName = cb.getArguments().getString(0);
            UUID id = UUID.fromString(cb.getArguments().getString(1));

            if ("untitled16_News".equals(entityName)) {
                Optional<News> opt = dataManager.load(News.class)
                        .id(id)
                        .view(new View(News.class).addProperty("title").addProperty("fullText"))
                        .optional();

                if (!opt.isPresent()) {
                    handleMissingRecent(entityName, id, "Новость");
                    return;
                }

                News n = opt.get();
                recentDocService.register("untitled16_News", n.getId(), n.getTitle());
                sendRecentToJs(10);

                dialogs.createMessageDialog()
                        .withCaption(n.getTitle() != null ? n.getTitle() : "Новость")
                        .withMessage(n.getFullText() != null ? n.getFullText() : "")
                        .show();
                return;
            }

            if ("untitled16_Notes".equals(entityName)) {
                Optional<Notes> opt = dataManager.load(Notes.class).id(id).optional();

                if (!opt.isPresent()) {
                    handleMissingRecent(entityName, id, "Заметка");
                    return;
                }

                Notes note = opt.get();
                recentDocService.register("untitled16_Notes", note.getId(), shorten(toPlainText(note.getText()), 80));
                sendRecentToJs(10);

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
                return;
            }

            dialogs.createMessageDialog()
                    .withCaption("Неизвестный тип")
                    .withMessage("Не знаю как открыть: " + entityName)
                    .show();
        });

        // ===== NEWS: запросить список новостей
        gridJs.addFunction("requestNews", cb -> {
            List<News> newsList = dataManager.load(News.class)
                    .query("select e from untitled16_News e order by e.createTs desc")
                    .view(new View(News.class)
                            .addProperty("title")
                            .addProperty("shortText"))
                    .maxResults(15)
                    .list();

            List<Map<String, Object>> items = new ArrayList<>();
            for (News n : newsList) {
                items.add(ParamsMap.of(
                        "id", n.getId().toString(),
                        "title", n.getTitle(),
                        "shortText", shorten(n.getShortText(), 220)
                ));
            }

            gridJs.callFunction("applyNews", gson.toJson(items));
        });

        // ===== NEWS: открыть новость
        gridJs.addFunction("openNews", cb -> {
            UUID id = UUID.fromString(cb.getArguments().getString(0));

            Optional<News> opt = dataManager.load(News.class)
                    .id(id)
                    .view(new View(News.class)
                            .addProperty("title")
                            .addProperty("fullText"))
                    .optional();

            if (!opt.isPresent()) {
                dialogs.createMessageDialog()
                        .withCaption("Недоступно")
                        .withMessage("Новость удалена или нет прав.")
                        .show();

                recentDocService.remove("untitled16_News", id); // если она была в recent
                sendRecentToJs(10);
                return;
            }

            News n = opt.get();

            recentDocService.register("untitled16_News", n.getId(), n.getTitle());
            sendRecentToJs(10);

            dialogs.createMessageDialog()
                    .withCaption(n.getTitle() != null ? n.getTitle() : "Новость")
                    .withMessage(n.getFullText() != null ? n.getFullText() : "")
                    .show();
        });

        // ===== календарь: события на месяц
        gridJs.addFunction("requestNotesForMonth", cb -> {
            int year = (int) cb.getArguments().getNumber(0);
            int month = (int) cb.getArguments().getNumber(1);

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.plusMonths(1);

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
                        "endDate", n.getNoteDate().toString(),
                        "summary", shorten(toPlainText(n.getText()), 60),
                        "noteId", n.getId().toString()
                ));
            }

            gridJs.callFunction("applyCalendarEvents", gson.toJson(events));
        });

        // ===== заметки: список на день
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
                        "summary", shorten(toPlainText(n.getText()), 120)
                ));
            }

            gridJs.callFunction("applyDayNotes", day.toString(), gson.toJson(items));
        });

        // ===== открыть заметку
        gridJs.addFunction("openNote", cb -> {
            UUID id = UUID.fromString(cb.getArguments().getString(0));

            Optional<Notes> opt = dataManager.load(Notes.class).id(id).optional();
            if (!opt.isPresent()) {
                dialogs.createMessageDialog()
                        .withCaption("Недоступно")
                        .withMessage("Заметка удалена или нет прав.")
                        .show();

                recentDocService.remove("untitled16_Notes", id);
                sendRecentToJs(10);
                return;
            }

            Notes note = opt.get();

            recentDocService.register("untitled16_Notes", note.getId(),
                    shorten(toPlainText(note.getText()), 80));
            sendRecentToJs(10);

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

        // ===== создать заметку
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
                    sendRecentToJs(10);
                }
            });

            editor.show();
        });

        // ===== удалить заметку
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
                                sendRecentToJs(10);
                            }),
                            new DialogAction(DialogAction.Type.NO)
                    )
                    .show();
        });
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
