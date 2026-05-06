package com.company.untitled16.web.screens.taskregistry;

import com.haulmont.cuba.core.entity.KeyValueEntity;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.Route;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.GroupTable;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.model.KeyValueCollectionContainer;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@UiController("untitled16_TaskRegistry")
@Route(path = "main", root = true)
@UiDescriptor("task-registry.xml")
public class TaskRegistry extends Screen {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String CURRENT_USER = "Петров П. П.";
    private static final String SIDE_ITEM_STYLE = "task-side-item";
    private static final String SIDE_ITEM_ACTIVE_STYLE = "task-side-item task-side-item-active";
    private static final String PAGE_BUTTON_STYLE = "task-page-button";
    private static final String PAGE_BUTTON_ACTIVE_STYLE = "task-page-button task-page-button-active";

    @Inject
    private KeyValueCollectionContainer tasksDc;
    @Inject
    private GroupTable<KeyValueEntity> tasksTable;
    @Inject
    private Notifications notifications;

    @Inject
    private TextField<String> projectNumberField;
    @Inject
    private TextField<String> registrationNumberField;
    @Inject
    private TextField<String> descriptionField;
    @Inject
    private TextField<String> initiatorField;
    @Inject
    private TextField<String> assigneeField;
    @Inject
    private TextField<String> dateFromField;
    @Inject
    private TextField<String> dateToField;

    @Inject
    private Label<String> totalCounter;
    @Inject
    private Label<String> inProgressCounter;
    @Inject
    private Label<String> overdueCounter;
    @Inject
    private Label<String> completedCounter;
    @Inject
    private Label<String> paginationTotalLabel;
    @Inject
    private Button pageSizeBtn;
    @Inject
    private Label<String> activeGroupChip;
    @Inject
    private Label<String> activeTypeChip;
    @Inject
    private Component documentPreview;
    @Inject
    private Label<String> documentPreviewTitle;
    @Inject
    private Label<String> documentPreviewTask;
    @Inject
    private Label<String> documentPreviewStatus;
    @Inject
    private Label<String> documentPreviewDeadline;

    @Inject
    private Button currentTasksBtn;
    @Inject
    private Button completedByMeBtn;
    @Inject
    private Button assignedByMeBtn;
    @Inject
    private Button overdueBtn;
    @Inject
    private Button archiveBtn;
    @Inject
    private Button allTypesBtn;
    @Inject
    private Button ordersBtn;
    @Inject
    private Button approvalsBtn;
    @Inject
    private Button readingsBtn;
    @Inject
    private Button signingBtn;

    @Inject
    private Button firstPageBtn;
    @Inject
    private Button prevPageBtn;
    @Inject
    private Button nextPageBtn;
    @Inject
    private Button lastPageBtn;

    @Inject
    private Button page1Btn;
    @Inject
    private Button page2Btn;
    @Inject
    private Button page3Btn;
    @Inject
    private Button page4Btn;
    @Inject
    private Button page5Btn;

    private List<KeyValueEntity> allTasks = Collections.emptyList();
    private List<KeyValueEntity> filteredTasks = Collections.emptyList();
    private int currentPage = 1;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private String taskGroupFilter = "Текущие";
    private String taskTypeFilter = "Согласование";

    @Subscribe
    public void onInit(InitEvent event) {
        allTasks = createMockTasks();
        filteredTasks = new ArrayList<>(allTasks);
        configureTableStyles();
        applyFilters();
    }

    @Subscribe("searchBtn")
    public void onSearchBtnClick(Button.ClickEvent event) {
        currentPage = 1;
        applyFilters();
    }

    @Subscribe("resetBtn")
    public void onResetBtnClick(Button.ClickEvent event) {
        projectNumberField.setValue(null);
        registrationNumberField.setValue(null);
        descriptionField.setValue(null);
        initiatorField.setValue(null);
        assigneeField.setValue(null);
        dateFromField.setValue(null);
        dateToField.setValue(null);
        taskGroupFilter = "Текущие";
        taskTypeFilter = "Согласование";
        currentPage = 1;
        applyFilters();
    }

    @Subscribe("moreFiltersBtn")
    public void onMoreFiltersBtnClick(Button.ClickEvent event) {
        notifications.create()
                .withCaption("Дополнительные фильтры")
                .withDescription("Прототип: здесь можно добавить приоритет, подразделение, автора резолюции и контрольный срок.")
                .show();
    }

    @Subscribe("openTaskBtn")
    public void onOpenTaskBtnClick(Button.ClickEvent event) {
        openSelectedTask();
    }

    @Subscribe("closeDocumentPreviewBtn")
    public void onCloseDocumentPreviewBtnClick(Button.ClickEvent event) {
        documentPreview.setVisible(false);
    }

    @Subscribe("createTaskBtn")
    public void onCreateTaskBtnClick(Button.ClickEvent event) {
        notifications.create()
                .withCaption("Создать задачу")
                .withDescription("Прототип: открытие формы создания новой задачи.")
                .show();
    }

    @Subscribe("assignTaskBtn")
    public void onAssignTaskBtnClick(Button.ClickEvent event) {
        KeyValueEntity task = tasksTable.getSingleSelected();
        notifications.create()
                .withCaption("Назначить")
                .withDescription(task == null ? "Выберите задачу для назначения." : "Исполнитель: " + value(task, "assignee"))
                .show();
    }

    @Subscribe("changeStatusBtn")
    public void onChangeStatusBtnClick(Button.ClickEvent event) {
        KeyValueEntity task = tasksTable.getSingleSelected();
        notifications.create()
                .withCaption("Изменить статус")
                .withDescription(task == null ? "Выберите задачу для изменения статуса." : "Текущий статус: " + value(task, "status"))
                .show();
    }

    @Subscribe("exportBtn")
    public void onExportBtnClick(Button.ClickEvent event) {
        notifications.create()
                .withCaption("Экспорт")
                .withDescription("Выгрузка " + filteredTasks.size() + " строк с учетом текущих фильтров.")
                .show();
    }

    @Subscribe("currentTasksBtn")
    public void onCurrentTasksBtnClick(Button.ClickEvent event) {
        setTaskGroupFilter("Текущие");
    }

    @Subscribe("completedByMeBtn")
    public void onCompletedByMeBtnClick(Button.ClickEvent event) {
        setTaskGroupFilter("Завершенные мной");
    }

    @Subscribe("assignedByMeBtn")
    public void onAssignedByMeBtnClick(Button.ClickEvent event) {
        setTaskGroupFilter("Назначенные мной");
    }

    @Subscribe("overdueBtn")
    public void onOverdueBtnClick(Button.ClickEvent event) {
        setTaskGroupFilter("Просроченные");
    }

    @Subscribe("archiveBtn")
    public void onArchiveBtnClick(Button.ClickEvent event) {
        setTaskGroupFilter("Архив");
    }

    @Subscribe("allTypesBtn")
    public void onAllTypesBtnClick(Button.ClickEvent event) {
        setTaskTypeFilter("Все типы");
    }

    @Subscribe("ordersBtn")
    public void onOrdersBtnClick(Button.ClickEvent event) {
        setTaskTypeFilter("Поручение");
    }

    @Subscribe("approvalsBtn")
    public void onApprovalsBtnClick(Button.ClickEvent event) {
        setTaskTypeFilter("Согласование");
    }

    @Subscribe("readingsBtn")
    public void onReadingsBtnClick(Button.ClickEvent event) {
        setTaskTypeFilter("Ознакомление");
    }

    @Subscribe("signingBtn")
    public void onSigningBtnClick(Button.ClickEvent event) {
        setTaskTypeFilter("Подписание");
    }

    @Subscribe("firstPageBtn")
    public void onFirstPageBtnClick(Button.ClickEvent event) {
        goToPage(1);
    }

    @Subscribe("prevPageBtn")
    public void onPrevPageBtnClick(Button.ClickEvent event) {
        goToPage(currentPage - 1);
    }

    @Subscribe("nextPageBtn")
    public void onNextPageBtnClick(Button.ClickEvent event) {
        goToPage(currentPage + 1);
    }

    @Subscribe("lastPageBtn")
    public void onLastPageBtnClick(Button.ClickEvent event) {
        goToPage(pageCount());
    }

    @Subscribe("pageSizeBtn")
    public void onPageSizeBtnClick(Button.ClickEvent event) {
        if (pageSize == 10) {
            pageSize = 20;
        } else if (pageSize == 20) {
            pageSize = 50;
        } else {
            pageSize = 10;
        }
        currentPage = 1;
        goToPage(currentPage);
    }

    @Subscribe("page1Btn")
    public void onPage1BtnClick(Button.ClickEvent event) {
        goToPage(pageNumber(page1Btn));
    }

    @Subscribe("page2Btn")
    public void onPage2BtnClick(Button.ClickEvent event) {
        goToPage(pageNumber(page2Btn));
    }

    @Subscribe("page3Btn")
    public void onPage3BtnClick(Button.ClickEvent event) {
        goToPage(pageNumber(page3Btn));
    }

    @Subscribe("page4Btn")
    public void onPage4BtnClick(Button.ClickEvent event) {
        goToPage(pageNumber(page4Btn));
    }

    @Subscribe("page5Btn")
    public void onPage5BtnClick(Button.ClickEvent event) {
        goToPage(pageNumber(page5Btn));
    }

    private void setTaskGroupFilter(String taskGroupFilter) {
        this.taskGroupFilter = taskGroupFilter;
        currentPage = 1;
        applyFilters();
    }

    private void setTaskTypeFilter(String taskTypeFilter) {
        this.taskTypeFilter = taskTypeFilter;
        currentPage = 1;
        applyFilters();
    }

    private void applyFilters() {
        filteredTasks = allTasks.stream()
                .filter(this::matchesSideFilters)
                .filter(task -> contains(task, "projectNumber", projectNumberField.getValue()))
                .filter(task -> contains(task, "registrationNumber", registrationNumberField.getValue()))
                .filter(task -> contains(task, "taskName", descriptionField.getValue()) || contains(task, "document", descriptionField.getValue()))
                .filter(task -> contains(task, "initiator", initiatorField.getValue()))
                .filter(task -> contains(task, "assignee", assigneeField.getValue()))
                .filter(task -> contains(task, "deadline", dateFromField.getValue()))
                .filter(task -> contains(task, "deadline", dateToField.getValue()))
                .collect(Collectors.toList());
        activeGroupChip.setValue(taskGroupFilter + "   ×");
        activeTypeChip.setValue(typeChipCaption() + "   ×");
        updateSideMenuStyles();
        goToPage(currentPage);
        updateCounters();
    }

    private boolean matchesSideFilters(KeyValueEntity task) {
        String status = value(task, "status");
        if ("Текущие".equals(taskGroupFilter) && ("Завершена".equals(status) || "Архив".equals(status))) {
            return false;
        }
        if ("Завершенные мной".equals(taskGroupFilter) && !"Завершена".equals(status)) {
            return false;
        }
        if ("Назначенные мной".equals(taskGroupFilter) && !CURRENT_USER.equals(value(task, "initiator"))) {
            return false;
        }
        if ("Просроченные".equals(taskGroupFilter) && !Boolean.TRUE.equals(task.getValue("overdue"))) {
            return false;
        }
        if ("Архив".equals(taskGroupFilter) && !"Архив".equals(status)) {
            return false;
        }
        return "Все типы".equals(taskTypeFilter) || taskTypeFilter.equals(value(task, "taskType"));
    }

    private void goToPage(int page) {
        int pages = pageCount();
        currentPage = Math.max(1, Math.min(page, pages));
        int from = Math.min((currentPage - 1) * pageSize, filteredTasks.size());
        int to = Math.min(from + pageSize, filteredTasks.size());
        tasksDc.setItems(new ArrayList<>(filteredTasks.subList(from, to)));
        updatePagination(pages);
    }

    private int pageCount() {
        return Math.max(1, (int) Math.ceil(filteredTasks.size() / (double) pageSize));
    }

    private void updatePagination(int pages) {
        paginationTotalLabel.setValue("Всего: " + filteredTasks.size());
        pageSizeBtn.setCaption(pageSize + " ▾");
        firstPageBtn.setEnabled(currentPage > 1);
        prevPageBtn.setEnabled(currentPage > 1);
        nextPageBtn.setEnabled(currentPage < pages);
        lastPageBtn.setEnabled(currentPage < pages);

        Button[] buttons = {page1Btn, page2Btn, page3Btn, page4Btn, page5Btn};
        int firstVisiblePage = Math.max(1, Math.min(currentPage - 2, Math.max(1, pages - 4)));
        for (int i = 0; i < buttons.length; i++) {
            int page = firstVisiblePage + i;
            buttons[i].setCaption(page <= pages ? String.valueOf(page) : "");
            buttons[i].setEnabled(page <= pages);
            buttons[i].setStyleName(page == currentPage ? PAGE_BUTTON_ACTIVE_STYLE : PAGE_BUTTON_STYLE);
        }
    }

    private String typeChipCaption() {
        if ("Все типы".equals(taskTypeFilter)) {
            return "Все типы";
        }
        if ("Согласование".equals(taskTypeFilter)) {
            return "Согласования";
        }
        if ("Поручение".equals(taskTypeFilter)) {
            return "Поручения";
        }
        if ("Ознакомление".equals(taskTypeFilter)) {
            return "Ознакомления";
        }
        if ("Подписание".equals(taskTypeFilter)) {
            return "Подписание";
        }
        return taskTypeFilter;
    }

    private void updateSideMenuStyles() {
        currentTasksBtn.setStyleName("Текущие".equals(taskGroupFilter) ? SIDE_ITEM_ACTIVE_STYLE : SIDE_ITEM_STYLE);
        completedByMeBtn.setStyleName("Завершенные мной".equals(taskGroupFilter) ? SIDE_ITEM_ACTIVE_STYLE : SIDE_ITEM_STYLE);
        assignedByMeBtn.setStyleName("Назначенные мной".equals(taskGroupFilter) ? SIDE_ITEM_ACTIVE_STYLE : SIDE_ITEM_STYLE);
        overdueBtn.setStyleName("Просроченные".equals(taskGroupFilter) ? SIDE_ITEM_ACTIVE_STYLE : SIDE_ITEM_STYLE);
        archiveBtn.setStyleName("Архив".equals(taskGroupFilter) ? SIDE_ITEM_ACTIVE_STYLE : SIDE_ITEM_STYLE);

        allTypesBtn.setStyleName("Все типы".equals(taskTypeFilter) ? SIDE_ITEM_ACTIVE_STYLE : SIDE_ITEM_STYLE);
        ordersBtn.setStyleName("Поручение".equals(taskTypeFilter) ? SIDE_ITEM_ACTIVE_STYLE : SIDE_ITEM_STYLE);
        approvalsBtn.setStyleName("Согласование".equals(taskTypeFilter) ? SIDE_ITEM_ACTIVE_STYLE : SIDE_ITEM_STYLE);
        readingsBtn.setStyleName("Ознакомление".equals(taskTypeFilter) ? SIDE_ITEM_ACTIVE_STYLE : SIDE_ITEM_STYLE);
        signingBtn.setStyleName("Подписание".equals(taskTypeFilter) ? SIDE_ITEM_ACTIVE_STYLE : SIDE_ITEM_STYLE);
    }

    private void updateCounters() {
        totalCounter.setValue(String.valueOf(filteredTasks.size()));
        inProgressCounter.setValue(String.valueOf(countByStatus("В работе") + countByStatus("На согласовании")));
        overdueCounter.setValue(String.valueOf(filteredTasks.stream().filter(task -> Boolean.TRUE.equals(task.getValue("overdue"))).count()));
        completedCounter.setValue(String.valueOf(countByStatus("Завершена")));
    }

    private long countByStatus(String status) {
        return filteredTasks.stream().filter(task -> status.equals(value(task, "status"))).count();
    }

    private void openSelectedTask() {
        KeyValueEntity task = tasksTable.getSingleSelected();
        if (task == null) {
            notifications.create()
                    .withCaption("Открыть документ")
                    .withDescription("Выберите строку в таблице и нажмите «Открыть».")
                    .show();
            return;
        }

        documentPreviewTitle.setValue("Документ: " + value(task, "document") + " · " + value(task, "registrationNumber"));
        documentPreviewTask.setValue("Задача: " + value(task, "taskName") + " · Исполнитель: " + value(task, "assignee"));
        documentPreviewStatus.setValue("Статус: " + value(task, "status"));
        documentPreviewDeadline.setValue("Срок: " + value(task, "deadline") + " · Создана: " + value(task, "created"));
        documentPreview.setVisible(true);

        notifications.create()
                .withCaption("Документ открыт")
                .withDescription(value(task, "document"))
                .show();
    }

    private int pageNumber(Button button) {
        try {
            return Integer.parseInt(button.getCaption());
        } catch (NumberFormatException ignored) {
            return currentPage;
        }
    }

    private boolean contains(KeyValueEntity task, String property, String filterValue) {
        if (filterValue == null || filterValue.trim().isEmpty()) {
            return true;
        }
        return value(task, property).toLowerCase(Locale.ROOT).contains(filterValue.trim().toLowerCase(Locale.ROOT));
    }

    private String value(KeyValueEntity task, String property) {
        Object value = task.getValue(property);
        return value == null ? "" : String.valueOf(value);
    }

    private void configureTableStyles() {
        tasksTable.setStyleProvider((task, property) -> {
            if (task == null) {
                return null;
            }

            if (property == null && Boolean.TRUE.equals(task.getValue("overdue"))) {
                return "task-row-overdue";
            }

            if ("selected".equals(property)) {
                return "task-check-cell";
            }

            if ("status".equals(property)) {
                String status = task.getValue("status");
                if ("Просрочена".equals(status)) {
                    return "task-status-overdue";
                }
                if ("Завершена".equals(status)) {
                    return "task-status-completed";
                }
                if ("В работе".equals(status)) {
                    return "task-status-progress";
                }
                if ("На согласовании".equals(status)) {
                    return "task-status-approval";
                }
            }

            if ("deadline".equals(property) && Boolean.TRUE.equals(task.getValue("overdue"))) {
                return "task-deadline-overdue";
            }

            return null;
        });
    }

    private List<KeyValueEntity> createMockTasks() {
        List<KeyValueEntity> tasks = new ArrayList<>();
        tasks.add(task("PRJ-2024-001", "1", "Согласовать договор поставки", "Договор поставки №12/24", "Согласование", "P-2024-00123", "Иванов И. И.", "Петров П. П.", "В работе", "28.05.2024", "14.05.2024 10:15", false));
        tasks.add(task("PRJ-2024-002", "2", "Согласовать техническое задание", "ТЗ на разработку", "Согласование", "P-2024-00124", "Смирнова О. А.", "Кузнецов А. В.", "На согласовании", "30.05.2024", "15.05.2024 09:42", false));
        tasks.add(task("PRJ-2024-003", "3", "Согласовать бюджет проекта", "Бюджет проекта", "Согласование", "P-2024-00125", "Иванов И. И.", "Сидорова Е. В.", "Просрочена", "20.05.2024", "10.05.2024 16:30", true));
        tasks.add(task("PRJ-2024-004", "4", "Согласовать план работ", "План работ", "Согласование", "P-2024-00126", "Петров П. П.", "Иванова М. С.", "В работе", "31.05.2024", "16.05.2024 11:05", false));
        tasks.add(task("PRJ-2024-005", "5", "Согласовать спецификацию", "Спецификация", "Согласование", "P-2024-00127", "Кузнецов А. В.", "Петров П. П.", "На согласовании", "02.06.2024", "17.05.2024 14:20", false));
        tasks.add(task("PRJ-2024-006", "6", "Согласовать коммерческое предложение", "Коммерческое предложение", "Поручение", "P-2024-00128", "Смирнова О. А.", "Кузнецов А. В.", "Завершена", "18.05.2024", "08.05.2024 13:12", false));
        tasks.add(task("PRJ-2024-007", "7", "Согласовать изменения договора", "Доп. соглашение №1", "Согласование", "P-2024-00129", "Иванов И. И.", "Сидорова Е. В.", "В работе", "04.06.2024", "18.05.2024 10:50", false));
        tasks.add(task("PRJ-2024-008", "8", "Согласовать отчет по проекту", "Отчет по проекту", "Ознакомление", "P-2024-00130", "Петров П. П.", "Иванова М. С.", "На согласовании", "05.06.2024", "19.05.2024 09:25", false));
        tasks.add(task("PRJ-2024-009", "9", "Согласовать закупку оборудования", "Заявка на закупку", "Согласование", "P-2024-00131", "Кузнецов А. В.", "Петров П. П.", "Просрочена", "22.05.2024", "12.05.2024 15:40", true));
        tasks.add(task("PRJ-2024-010", "10", "Согласовать кадровые изменения", "Приказ о кадровых изменениях", "Поручение", "P-2024-00132", "Смирнова О. А.", "Сидорова Е. В.", "Завершена", "15.05.2024", "07.05.2024 12:00", false));
        tasks.add(task("PRJ-2024-011", "11", "Проверить комплектность документов", "Опись документов", "Согласование", "P-2024-00133", "Петров П. П.", "Иванов И. И.", "В работе", "06.06.2024", "20.05.2024 09:10", false));
        tasks.add(task("PRJ-2024-012", "12", "Подписать акт приемки", "Акт приемки №44", "Подписание", "P-2024-00134", "Сидорова Е. В.", "Петров П. П.", "На согласовании", "07.06.2024", "20.05.2024 11:30", false));
        tasks.add(task("PRJ-2024-013", "13", "Ознакомиться с протоколом", "Протокол совещания", "Ознакомление", "P-2024-00135", "Иванова М. С.", "Кузнецов А. В.", "В работе", "08.06.2024", "21.05.2024 08:45", false));
        tasks.add(task("PRJ-2024-014", "14", "Согласовать график платежей", "График платежей", "Согласование", "P-2024-00136", "Петров П. П.", "Смирнова О. А.", "Просрочена", "23.05.2024", "13.05.2024 14:05", true));
        tasks.add(task("PRJ-2024-015", "15", "Назначить ответственного", "Резолюция руководителя", "Согласование", "P-2024-00137", "Петров П. П.", "Сидорова Е. В.", "В работе", "10.06.2024", "22.05.2024 10:25", false));
        tasks.add(task("PRJ-2024-016", "16", "Подписать доп. соглашение", "Доп. соглашение №2", "Подписание", "P-2024-00138", "Иванов И. И.", "Петров П. П.", "Завершена", "19.05.2024", "09.05.2024 16:55", false));
        tasks.add(task("PRJ-2024-017", "17", "Согласовать служебную записку", "Служебная записка", "Согласование", "P-2024-00139", "Смирнова О. А.", "Иванова М. С.", "На согласовании", "11.06.2024", "23.05.2024 13:35", false));
        tasks.add(task("PRJ-2024-018", "18", "Ознакомиться с приказом", "Приказ №78", "Ознакомление", "P-2024-00140", "Кузнецов А. В.", "Петров П. П.", "Архив", "12.06.2024", "24.05.2024 12:20", false));
        tasks.add(task("PRJ-2024-019", "19", "Согласовать заявку на командировку", "Заявка на командировку", "Согласование", "P-2024-00141", "Петров П. П.", "Кузнецов А. В.", "В работе", "13.06.2024", "25.05.2024 15:15", false));
        tasks.add(task("PRJ-2024-020", "20", "Подготовить лист согласования", "Лист согласования", "Согласование", "P-2024-00142", "Иванова М. С.", "Смирнова О. А.", "В работе", "14.06.2024", "26.05.2024 17:00", false));
        return tasks;
    }

    private KeyValueEntity task(String projectNumber,
                                String rowNumber,
                                String taskName,
                                String document,
                                String taskType,
                                String registrationNumber,
                                String initiator,
                                String assignee,
                                String status,
                                String deadline,
                                String created,
                                boolean overdue) {
        KeyValueEntity task = new KeyValueEntity();
        task.setValue("selected", "□");
        task.setValue("projectNumber", projectNumber);
        task.setValue("rowNumber", rowNumber);
        task.setValue("taskName", taskName);
        task.setValue("document", document);
        task.setValue("taskType", taskType);
        task.setValue("registrationNumber", registrationNumber);
        task.setValue("initiator", initiator);
        task.setValue("assignee", assignee);
        task.setValue("status", status);
        task.setValue("deadline", deadline);
        task.setValue("created", created);
        task.setValue("overdue", overdue);
        return task;
    }
}
