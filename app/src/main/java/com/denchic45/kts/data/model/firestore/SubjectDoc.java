package com.denchic45.kts.data.model.firestore;

import com.denchic45.kts.data.model.DocModel;

import java.util.List;

public class SubjectDoc implements DocModel {

    protected String uuid;
    private String name;
    private String iconUrl;
    private String colorName;
    private boolean special;
    private List<String> searchKeys;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    public List<String> getSearchKeys() {
        return searchKeys;
    }

    public void setSearchKeys(List<String> searchKeys) {
        this.searchKeys = searchKeys;
    }
}
