package com.company.untitled16.web.screens.taskregistry;

import com.haulmont.cuba.core.entity.KeyValueEntity;
import com.haulmont.cuba.gui.components.GroupTable;
import com.haulmont.cuba.gui.model.KeyValueCollectionContainer;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@UiController("untitled16_TaskRegistry")
@UiDescriptor("task-registry.xml")
public class TaskRegistry extends Screen {

    @Inject
    private KeyValueCollectionContainer tasksDc;

    @Inject
    private GroupTable<KeyValueEntity> tasksTable;

    @Subscribe
    public void onInit(InitEvent event) {
        tasksDc.setItems(createMockTasks());
        tasksTable.setStyleProvider((task, property) -> {
            if (task == null) {
                return null;
            }

            if (property == null && Boolean.TRUE.equals(task.getValue("overdue"))) {
                return "task-row-overdue";
            }

            if ("status".equals(property)) {
                String status = task.getValue("status");
                if ("Просрочено".equals(status)) {
                    return "task-status-overdue";
                }
                if ("Завершено".equals(status)) {
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
        tasks.add(task("PRJ-2026-014", "TASK-0001", "Согласовать паспорт проекта по модернизации", "Согласование", "Анна Орлова", "Иван Петров", "На согласовании", "08.05.2026", false));
        tasks.add(task("PRJ-2026-018", "TASK-0002", "Подготовить пакет документов для комиссии", "Поручение", "Сергей Волков", "Мария Смирнова", "В работе", "12.05.2026", false));
        tasks.add(task("PRJ-2026-021", "TASK-0003", "Ознакомиться с новой редакцией регламента", "Ознакомление", "Ольга Кузнецова", "Дмитрий Соколов", "Просрочено", "30.04.2026", true));
        tasks.add(task("PRJ-2026-009", "TASK-0004", "Подписать акт выполненных работ", "Подписание", "Иван Петров", "Елена Морозова", "В работе", "15.05.2026", false));
        tasks.add(task("PRJ-2026-030", "TASK-0005", "Согласовать бюджет закупочной процедуры", "Согласование", "Мария Смирнова", "Павел Никитин", "Просрочено", "02.05.2026", true));
        tasks.add(task("PRJ-2026-012", "TASK-0006", "Проверить замечания юридического отдела", "Поручение", "Дмитрий Соколов", "Анна Орлова", "В работе", "20.05.2026", false));
        tasks.add(task("PRJ-2026-026", "TASK-0007", "Закрыть контрольную точку этапа внедрения", "Согласование", "Елена Морозова", "Сергей Волков", "Завершено", "28.04.2026", false));
        tasks.add(task("PRJ-2026-033", "TASK-0008", "Сформировать итоговый отчет для руководителя", "Поручение", "Павел Никитин", "Ольга Кузнецова", "Завершено", "05.05.2026", false));
        return tasks;
    }

    private KeyValueEntity task(String projectNumber,
                                String registrationNumber,
                                String description,
                                String taskType,
                                String initiator,
                                String assignee,
                                String status,
                                String deadline,
                                boolean overdue) {
        KeyValueEntity task = new KeyValueEntity();
        task.setValue("projectNumber", projectNumber);
        task.setValue("registrationNumber", registrationNumber);
        task.setValue("description", description);
        task.setValue("taskType", taskType);
        task.setValue("initiator", initiator);
        task.setValue("assignee", assignee);
        task.setValue("status", status);
        task.setValue("deadline", deadline);
        task.setValue("overdue", overdue);
        return task;
    }
}
