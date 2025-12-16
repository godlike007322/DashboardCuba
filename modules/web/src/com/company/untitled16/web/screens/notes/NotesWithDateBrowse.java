package com.company.untitled16.web.screens.notes;

import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.screen.*;
import com.company.untitled16.entity.Notes;

import javax.inject.Inject;
import java.time.LocalDate;

@UiController("untitled16_NotesWithDate.browse")
@UiDescriptor("notes-with-date.xml")
@LookupComponent("notesesTable")
@LoadDataBeforeShow
public class NotesWithDateBrowse extends StandardLookup<Notes> {


    private LocalDate selectedDate;
    @Inject
    private CollectionLoader<Notes> notesesDl;


    @Subscribe
    public void onInit(InitEvent event) {

        MapScreenOptions opts = (MapScreenOptions) event.getOptions();
        selectedDate = (LocalDate) opts.getParams().get("date");
        notesesDl.setParameter("date", selectedDate);

    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        notesesDl.load();

    }

    @Install(to = "notesesTable.create", subject = "initializer")
    private void notesesTableCreateInitializer(Notes notes) {

        notes.setNoteDate(selectedDate);

    }


}