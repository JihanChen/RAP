package com.taobao.rigel.rap.common.elasticsearch;


import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * es操作参数类
 */
public final class EsOperateParam implements Serializable {

    private static final long serialVersionUID = 2532509826091658212L;

    // 只能通过builder类创建
    private EsOperateParam() {
    }

    public static final EsOperateParam EMPTY = new EsOperateParam();

    /**
     * 文档id,即实体id
     */
    private String id;

    /**
     * 文档对象，即实体
     */
    private Object document;

    private String indexName;

    private String typeName;

    /**
     * 回调函数
     */
    private EsOperateCallback callback;

    /**
     * 超时时间,此次调用的超时时间.
     * 优先级大于默认的超时时间，默认为0时将使用默认的超时时间
     */
    private int timeoutSeconds = 0;

    /**
     * 是否同步获取,默认都是异步超时获取
     */
    private boolean sync = false;

    /**
     * 实体属性、值集合,通常用于更新多个字段
     */
    private Map<String, Object> fieldValues = new HashMap<>();

    /**
     * 查询参数
     */
    private Map<String, Object> searchFields = new HashMap<>();

    /**
     * 是否模糊匹配
     */
    private boolean fuzzy = false;

    /**
     * 从第几页开始查询
     */
    private int from = 0;

    /**
     * 查询数量
     */
    private int size = 10;

    public static SaveBuilder saveBuilder() {
        return new SaveBuilder();
    }

    public static DeleteBuilder deleteBuilder() {
        return new DeleteBuilder();
    }

    public static UpdateBuilder updateBuilder() {
        return new UpdateBuilder();
    }

    public static QueryBuilder queryBuilder() {
        return new QueryBuilder();
    }



    // 检查用于更新的入参属性配置
    public void checkUpdate() {
        // document obj和fieldValues2者至少其一不为空，有限使用document对象
        if (this.document == null && (fieldValues == null || fieldValues.isEmpty())) {
            throw new RuntimeException("文档对象或者属性键值不能为空");
        }
    }
    // 检查用于更新的入参属性配置
    public void checkQuery() {
        // document obj和fieldValues2者至少其一不为空，有限使用document对象
        if (this.document == null && (searchFields == null || searchFields.isEmpty())) {
            throw new RuntimeException("文档对象或者属性键值不能为空");
        }
    }
    // 检查用于查询的入参属性配置
    public void checkQueryById(Class documentClass) {
        checkDocumentClass(documentClass);
    }

    // 检查用于更新的入参属性配置
    public void checkFieldValues() {
        // document obj和fieldValues2者至少其一不为空，有限使用document对象
        if (fieldValues == null || fieldValues.isEmpty()) {
            throw new RuntimeException("文档对象或者属性键值不能为空");
        }
    }


    // 检查用于查询的入参属性配置
    public void checkQuery(Class documentClass) {
        checkDocumentClass(documentClass);
    }

    private void checkDocumentClass(Class documentClass) {
        if (documentClass == null) {
            throw new RuntimeException("文档Class不能为为空");
        }
    }
    // 检查用于查询的入参属性配置
    public void checksea(Class documentClass) {
        checkDocumentClass(documentClass);
    }


    public static class SaveBuilder {

        private EsOperateParam param = new EsOperateParam();

        public SaveBuilder id(Long id) {
            this.param.id = id == null ? null : id.toString();
            return this;
        }

        public SaveBuilder id(String id) {
            this.param.id = id;
            return this;
        }

        public SaveBuilder indexName(String indexName) {
            this.param.indexName = indexName;
            return this;
        }

        public SaveBuilder typeName(String typeName) {
            this.param.typeName = typeName;
            return this;
        }

        public SaveBuilder timeoutSeconds(int timeoutSeconds) {
            this.param.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public SaveBuilder sync(boolean sync) {
            this.param.sync = sync;
            return this;
        }

        public SaveBuilder document(Object document) {
            this.param.document = document;
            return this;
        }

        public SaveBuilder callback(EsOperateCallback callback) {
            this.param.callback = callback;
            return this;
        }

        public EsOperateParam build() {
            return param;
        }

    }

    public static class DeleteBuilder {
        private EsOperateParam param = new EsOperateParam();

        public DeleteBuilder id(Long id) {
            this.param.id = id == null ? null : id.toString();
            return this;
        }

        public DeleteBuilder id(String id) {
            this.param.id = id;
            return this;
        }
        public DeleteBuilder values(String key,Object value) {
            this.param.fieldValues.put(key, value);
            return this;
        }
        public DeleteBuilder values(Map<String, Object> values) {
            this.param.fieldValues.putAll(values);
            return this;
        }

        public DeleteBuilder indexName(String indexName) {
            this.param.indexName = indexName;
            return this;
        }

        public DeleteBuilder typeName(String typeName) {
            this.param.typeName = typeName;
            return this;
        }

        public DeleteBuilder timeoutSeconds(int timeoutSeconds) {
            this.param.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public DeleteBuilder sync(boolean sync) {
            this.param.sync = sync;
            return this;
        }

        public DeleteBuilder callback(EsOperateCallback callback) {
            this.param.callback = callback;
            return this;
        }

        public EsOperateParam build() {
            return param;
        }
    }

    public static class UpdateBuilder {
        private EsOperateParam param = new EsOperateParam();

        public UpdateBuilder id(Long id) {
            this.param.id = id == null ? null : id.toString();
            return this;
        }

        public UpdateBuilder id(String id) {
            this.param.id = id;
            return this;
        }

        public UpdateBuilder indexName(String indexName) {
            this.param.indexName = indexName;
            return this;
        }

        public UpdateBuilder typeName(String typeName) {
            this.param.typeName = typeName;
            return this;
        }

        public UpdateBuilder timeoutSeconds(int timeoutSeconds) {
            this.param.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public UpdateBuilder sync(boolean sync) {
            this.param.sync = sync;
            return this;
        }

        public UpdateBuilder fieldValue(String field, Object value) {
            this.param.fieldValues.put(field, value);
            return this;
        }

        public UpdateBuilder fieldValues(Map<String, Object> fieldValues) {
            this.param.fieldValues.putAll(fieldValues);
            return this;
        }

        public UpdateBuilder document(Object document) {
            this.param.document = document;
            return this;
        }

        public UpdateBuilder callback(EsOperateCallback callback) {
            this.param.callback = callback;
            return this;
        }

        public EsOperateParam build() {
            return param;
        }
    }

    public static class QueryBuilder {
        private EsOperateParam param = new EsOperateParam();

        public QueryBuilder id(Long id) {
            this.param.id = id == null ? null : id.toString();
            return this;
        }

        public QueryBuilder id(String id) {
            this.param.id = id;
            return this;
        }

        public QueryBuilder indexName(String indexName) {
            this.param.indexName = indexName;
            return this;
        }

        public QueryBuilder typeName(String typeName) {
            this.param.typeName = typeName;
            return this;
        }

        public QueryBuilder timeoutSeconds(int timeoutSeconds) {
            this.param.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public QueryBuilder sync(boolean sync) {
            this.param.sync = sync;
            return this;
        }

        public QueryBuilder fuzzy(boolean fuzzy) {
            this.param.fuzzy = fuzzy;
            return this;
        }

        public QueryBuilder from(int from) {
            this.param.from = from;
            return this;
        }

        public QueryBuilder size(int size) {
            this.param.size = size;
            return this;
        }

        public QueryBuilder searchValue(String field, Object value) {
            this.param.searchFields.put(field, value);
            return this;
        }
        public EsOperateParam build() {
            return param;
        }
    }


    public String getId() {
        return id;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public boolean isSync() {
        return sync;
    }

    public Object getDocument() {
        return document;
    }

    public Map<String, Object> getFieldValues() {
        return fieldValues;
    }

    public boolean isFuzzy() {
        return fuzzy;
    }


    public int getFrom() {
        return from;
    }

    public int getSize() {
        return size;
    }

    public Map<String, Object> getSearchFields() {
        return searchFields;
    }

    public void setSearchFields(Map<String, Object> searchFields) {
        this.searchFields = searchFields;
    }

    public EsOperateCallback getCallback() {
        return callback;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("document", document)
                .append("indexName", indexName)
                .append("typeName", typeName)
                .append("timeoutSeconds", timeoutSeconds)
                .append("sync", sync)
                .append("fieldValues", fieldValues)
                .append("fuzzy", fuzzy)
                .append("searchFields", searchFields)
                .append("from", from)
                .append("size", size)
                .toString();
    }
}
