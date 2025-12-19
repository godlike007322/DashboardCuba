package com.company.untitled16.web.screens.recentdoc;

import com.haulmont.cuba.gui.screen.*;
import com.company.untitled16.entity.RecentDoc;

@UiController("untitled16_RecentDoc.browse")
@UiDescriptor("recent-doc-browse.xml")
@LookupComponent("recentDocsTable")
@LoadDataBeforeShow
public class RecentDocBrowse extends StandardLookup<RecentDoc> {
}