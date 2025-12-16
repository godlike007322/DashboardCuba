package com.company.untitled16.web.screens.notes;

import com.haulmont.cuba.gui.screen.*;
import com.company.untitled16.entity.Notes;

@UiController("untitled16_Notes.browse")
@UiDescriptor("notes-browse.xml")
@LookupComponent("notesesTable")
@LoadDataBeforeShow
public class NotesBrowse extends StandardLookup<Notes> {
}