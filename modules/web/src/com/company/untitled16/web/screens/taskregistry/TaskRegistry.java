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
        tasks.add(task("1", "Согласовать договор поставки", "Договор поставки №12/24", "P-2024-00123", "Иванов И. И.", "Петров П. П.", "В работе", "28.05.2024", "14.05.2024 10:15", false));
        tasks.add(task("2", "Согласовать техническое задание", "ТЗ на разработку", "P-2024-00124", "Смирнова О. А.", "Кузнецов А. В.", "На согласовании", "30.05.2024", "15.05.2024 09:42", false));
        tasks.add(task("3", "Согласовать бюджет проекта", "Бюджет проекта", "P-2024-00125", "Иванов И. И.", "Сидорова Е. В.", "Просрочена", "20.05.2024", "10.05.2024 16:30", true));
        tasks.add(task("4", "Согласовать план работ", "План работ", "P-2024-00126", "Петров П. П.", "Иванова М. С.", "В работе", "31.05.2024", "16.05.2024 11:05", false));
        tasks.add(task("5", "Согласовать спецификацию", "Спецификация", "P-2024-00127", "Кузнецов А. В.", "Петров П. П.", "На согласовании", "02.06.2024", "17.05.2024 14:20", false));
        tasks.add(task("6", "Согласовать коммерческое предложение", "Коммерческое предложение", "P-2024-00128", "Смирнова О. А.", "Кузнецов А. В.", "Завершена", "18.05.2024", "08.05.2024 13:12", false));
        tasks.add(task("7", "Согласовать изменения договора", "Доп. соглашение №1", "P-2024-00129", "Иванов И. И.", "Сидорова Е. В.", "В работе", "04.06.2024", "18.05.2024 10:50", false));
        tasks.add(task("8", "Согласовать отчет по проекту", "Отчет по проекту", "P-2024-00130", "Петров П. П.", "Иванова М. С.", "На согласовании", "05.06.2024", "19.05.2024 09:25", false));
        tasks.add(task("9", "Согласовать закупку оборудования", "Заявка на закупку", "P-2024-00131", "Кузнецов А. В.", "Петров П. П.", "Просрочена", "22.05.2024", "12.05.2024 15:40", true));
        tasks.add(task("10", "Согласовать кадровые изменения", "Приказ о кадровых изменениях", "P-2024-00132", "Смирнова О. А.", "Сидорова Е. В.", "Завершена", "15.05.2024", "07.05.2024 12:00", false));
        return tasks;
    }

    private KeyValueEntity task(String rowNumber,
                                String taskName,
                                String document,
                                String registrationNumber,
                                String initiator,
                                String assignee,
                                String status,
                                String deadline,
                                String created,
                                boolean overdue) {
        KeyValueEntity task = new KeyValueEntity();
        task.setValue("selected", "□");
        task.setValue("rowNumber", rowNumber);
        task.setValue("taskName", taskName);
        task.setValue("document", document);
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
