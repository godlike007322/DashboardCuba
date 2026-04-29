package com.company.untitled16.web.screens.notes;

import com.company.untitled16.service.RecentDocsService;
import com.haulmont.cuba.gui.components.DateField;
import com.haulmont.cuba.gui.screen.*;
import com.company.untitled16.entity.Notes;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Map;

@UiController("untitled16_Notes.edit")
@UiDescriptor("notes-edit.xml")
@EditedEntityContainer("notesDc")
@LoadDataBeforeShow
public class NotesEdit extends StandardEditor<Notes> {
    private boolean lockDate = false;
    private LocalDate fixedDate;
    @Inject
    private DateField<LocalDate> noteDate;
    @Inject
    private RecentDocsService recentDocsService;


    @Subscribe
    public void onInit(InitEvent event) {
        ScreenOptions opts = event.getOptions();
        if (opts instanceof MapScreenOptions) {
            Map<String, Object> p = ((MapScreenOptions) opts).getParams();


            Object lock = p.get("lockDate");
            lockDate = Boolean.TRUE.equals(lock);

            Object d = p.get("fixedDate");
            if (d instanceof LocalDate) fixedDate = (LocalDate) d;
            else if (d instanceof String) fixedDate = LocalDate.parse((String) d);
        }

    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        boolean editable = !lockDate;
        noteDate.setEditable(editable);
        noteDate.setEnabled(editable); // можно оставить только enabled или только editable
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        Notes n = getEditedEntity();
        if (n != null && n.getId() != null) {
            recentDocsService.register("untitled16_Notes", n.getId(), n.getText() != null ? n.getText() : "");
        }
    }


}