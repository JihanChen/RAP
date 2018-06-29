package com.taobao.rigel.rap.common.bo;

import java.io.Serializable;

/**
 * es 存储对象
 */
public class EsAction implements Serializable {

    private static final long serialVersionUID = -2930077012828334313L;


    /**
     * 项目id
     */
    private Long projectId;

    /**
     * action id
     */
    private Long id;

    /**
     * action id
     */
    private String name;

    /**
     * 请求url
     */
    private String requestUrl;

    private String description;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}