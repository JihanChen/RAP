package com.taobao.rigel.rap.project.vo;

import java.util.Map;


public class ProjectVO implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String desc;
    private String status;
    private String accounts;
    private Boolean isManagable;
    private Map<String, Object> creator;
    private Boolean related;
    private int groupId;
    private int corporationId;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccounts() {
        return accounts;
    }

    public void setAccounts(String accounts) {
        this.accounts = accounts;
    }

    public Boolean getManagable() {
        return isManagable;
    }

    public void setManagable(Boolean managable) {
        isManagable = managable;
    }

    public Map<String, Object> getCreator() {
        return creator;
    }

    public void setCreator(Map<String, Object> creator) {
        this.creator = creator;
    }

    public Boolean getRelated() {
        return related;
    }

    public void setRelated(Boolean related) {
        this.related = related;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(int corporationId) {
        this.corporationId = corporationId;
    }
}
