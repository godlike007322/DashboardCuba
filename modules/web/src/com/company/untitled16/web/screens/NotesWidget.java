package com.company.untitled16.web.screens;

import com.company.untitled16.entity.Notes;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.actions.list.CreateAction;
import com.haulmont.cuba.gui.actions.list.EditAction;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.screen.*;

import javax.inject.Inject;

@UiController("untitled16_NotesWidget")
@UiDescriptor("notes-widget.xml")
public class NotesWidget extends ScreenFragment {
    @Inject
    private Table<Notes> notesTable;
    @Inject
    private CollectionLoader<Notes> notesDl;
    @Inject
    private ScreenBuilders screenBuilders;

    @Subscribe
    public void onInit(InitEvent event) {
        notesDl.load();

        CreateAction createAction = (CreateAction) notesTable.getAction("create");
        createAction.setScreenId("untitled16_Notes.edit");

        // EDIT → тот же редактор
        EditAction editAction = (EditAction) notesTable.getAction("edit");
        editAction.setScreenId("untitled16_Notes.edit");


    }

    @Subscribe("notesTable.create")
    public void onNotesTableCreate(Action.ActionPerformedEvent event) {
        final Screen screen = screenBuilders.editor(Notes.class, this).newEntity().withOpenMode(OpenMode.DIALOG).build();
        screen.addAfterCloseListener(afterCloseEvent -> notesDl.load());


        screen.show();
    }

    @Subscribe("notesTable.edit")
    public void onNotesTableEdit(Action.ActionPerformedEvent event) {
        Notes selected = notesTable.getSingleSelected();
        if (selected != null) {
            final Screen screen = screenBuilders.editor(Notes.class, this)
                    .editEntity(notesTable.getSingleSelected()).withOpenMode(OpenMode.DIALOG).build();
            screen.addAfterCloseListener(afterCloseEvent -> notesDl.load());

            screen.show();
        }
    }


}