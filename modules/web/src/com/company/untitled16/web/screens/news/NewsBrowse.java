package com.company.untitled16.web.screens.news;

import com.haulmont.cuba.gui.screen.*;
import com.company.untitled16.entity.News;

@UiController("untitled16_News.browse")
@UiDescriptor("news-browse.xml")
@LookupComponent("newsTable")
@LoadDataBeforeShow
public class NewsBrowse extends StandardLookup<News> {
}