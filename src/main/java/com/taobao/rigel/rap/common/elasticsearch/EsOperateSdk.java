package com.taobao.rigel.rap.common.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.taobao.rigel.rap.common.bo.EsAction;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * elasticsearch操作SDK
 *
 */
public class EsOperateSdk {

    private static Logger logger = LoggerFactory.getLogger(EsOperateSdk.class);

    /**
     * es集群节点,格式为=>
     * ip1:port1
     * ip2:port2...
     * 默认为本地es
     */
    private List<String> esClusterNodes;

    /**
     * es集群名称
     */
    private String esClusterName;

    /**
     * es所有操作的超时时间,单位秒
     */
    private int esOperateTimeoutSeconds;

    /**
     * es transport client
     */
    private TransportClient client;

    private long defaultTimeout;

    public void init() throws UnknownHostException {
        if (client == null) {
            // 以transport方式连接es
            client = new TransportClient(ImmutableSettings
                    .settingsBuilder()
                    .put("cluster.name", esClusterName)
                    // 自动嗅探其他es节点
                    .put("client.transport.sniff", true)
                    .put("client.transport.ignore_cluster_name", false)
                    .put("client.transport.ping_timeout", "5s")
                    .put("client.transport.nodes_sampler_interval", "5s")
            );

            if (CollectionUtils.isEmpty(esClusterNodes)) {
              throw new IllegalArgumentException("clusterNodes不能为空");
            }
            Map<String, String> nodes = Splitter.on(',')
                    .trimResults()
                    .omitEmptyStrings()
                    .withKeyValueSeparator(':')
                    .split(StringUtils.join(esClusterNodes,","));

            if (nodes.isEmpty()) {
                throw new IllegalArgumentException("clusterNodes格式不正确！要求ip1:port1,ip2:port2");
            }

            for (Map.Entry<String, String> entry : nodes.entrySet()) {
                String host = entry.getKey();
                int port = Integer.parseInt(entry.getValue());
                client.addTransportAddresses(new InetSocketTransportAddress(InetAddress.getByName(host), port));
            }
            if (esOperateTimeoutSeconds <= 0) {
                esOperateTimeoutSeconds = 10;
            }
            defaultTimeout = TimeUnit.SECONDS.toMillis(esOperateTimeoutSeconds);
        }
    }

    /**
     * 获取es client
     */
    public TransportClient esClient() {
        return client;
    }

    /**
     * 保存文档
     */
    public void save(EsOperateParam param) {
        // 参数检查
        // 调用es client
        ListenableActionFuture<IndexResponse> future = client.prepareIndex(param.getIndexName(),
                param.getTypeName(),
                param.getId())
                .setSource(JSON.toJSONString(param.getDocument())).execute();
        // 获取结果
//        doGet(param, future);
        // 设置回调
        callback(param, future);
    }

    /**
     * 删除文档
     */
    public void delete(EsOperateParam param) {
        // 参数检查
        // 调用es client
        ListenableActionFuture<DeleteResponse> future = client.prepareDelete(param.getIndexName(),
                param.getTypeName(),
                param.getId())
                .execute();
        // 获取结果
//        doGet(param, future);
        // 设置回调
        callback(param, future);
    }

    /**
     * 更新文档
     */
    public void update(EsOperateParam param) {
        // 参数检查
        param.checkUpdate();
        // 调用es client
        ListenableActionFuture<UpdateResponse> future;
        if (param.getDocument() == null) {
            future = client.prepareUpdate(param.getIndexName(), param.getTypeName(),
                    param.getId())
                    .setDoc(new IndexRequest().source(param.getFieldValues())).execute();
        } else {
            future = client.prepareUpdate(param.getIndexName(), param.getTypeName(),
                    param.getId())
                    .setDoc(JSON.toJSONString(param.getDocument())).execute();
        }
        // 获取结果
//        doGet(param, future);
        // 设置回调
        callback(param, future);
    }

    /**
     * 根据id查询
     */
    public <T> T queryById(EsOperateParam param, Class<T> documentClass) throws ElasticsearchException {
        // 参数检查
        param.checkQueryById(documentClass);
        // 调用es client
        ListenableActionFuture<GetResponse> future = client.prepareGet(param.getIndexName(),
                param.getTypeName(),
                param.getId())
                .execute();
        // 获取结果
        return JSON.parseObject(doGet(param, future).getSourceAsString(), documentClass);
    }

    /**
     * 查询,目前只支持针对单个属性的查询
     */
//    public <T> List<T> query(EsOperateParam param, Class<T> documentClass) throws ElasticsearchException {
//        // 参数检查
//        param.checkQuery(documentClass);
//        // 调用es client
//        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(param.getIndexName())
//                .setTypes(param.getTypeName());
//        Map<String, Object> searchFields = param.getSearchFields();
//
//        MatchQueryBuilder MatchQueryBuilder = null
//        searchFields.entrySet().stream().forEach(field->{
//            if (param.isFuzzy()) {
//                // 模糊匹配
//                if (field.getValue() instanceof String) {
//                    searchFields.put(field.getKey(),"*" + QueryParser.escape((String) field.getValue()) + "*")
//                }
//            }
//
//        });
//
//        ListenableActionFuture<SearchResponse> future = searchRequestBuilder
//                .setQuery(QueryBuilders.boolQuery()
//                        .must(QueryBuilders.matchPhraseQuery(param.getSearchField(), value)))
//                .setFrom(param.getFrom())
//                .setSize(param.getSize())
//                .execute();
//        // 获取结果
//        SearchHits hits = doGet(param, future).getHits();
//        if (hits.getTotalHits() == 0) {
//            return Collections.emptyList();
//        }
//        List<T> results = new ArrayList<>();
//        for (SearchHit hit : hits) {
//            results.add(JSON.parseObject(hit.getSourceAsString(), documentClass));
//        }
//        return results;
//    }


    /**
     * 查询，多个查询条件 or
     */
    public <T> List<T> shouldQuery(EsOperateParam param, Class<T> documentClass) throws ElasticsearchException {
        // 参数检查
        param.checkQuery(documentClass);
        // 调用es client
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(param.getIndexName())
                .setTypes(param.getTypeName())
                .setSearchType(SearchType.DEFAULT);
        // 组装查询参数
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        param.getSearchFields().entrySet().stream().forEach(e->{
            queryBuilder.should(QueryBuilders.matchPhraseQuery(e.getKey(),e.getValue().toString()));
        });

        ListenableActionFuture<SearchResponse> future = searchRequestBuilder
                .setQuery(queryBuilder)
                .setFrom(param.getFrom())
                .setSize(param.getSize())
                .execute();
        // 获取结果
        SearchHits hits = doGet(param, future).getHits();
        if (hits.getTotalHits() == 0) {
            return Collections.emptyList();
        }
        List<T> results = new ArrayList<>();
        for (SearchHit hit : hits) {
            results.add(JSON.parseObject(hit.getSourceAsString(), documentClass));
        }
        return results;
    }


    /**
     * 批量删除
     * @param param
     */
    public void batchDelete(EsOperateParam param){
        // 参数校验
        param.checkFieldValues();
        // 构建参数
        BulkRequestBuilder bulk = client.prepareBulk();
        param.getFieldValues().entrySet()
                .stream()
                .map(e->client.prepareDelete(param.getIndexName(), param.getTypeName(),e.getKey()))
                .forEach(e-> bulk.add(e));

        ListenableActionFuture<BulkResponse> future = bulk.execute();
        callback(param, future);
    }

    /**
     * 批量新增
     * @param param
     */
    public void batchSave(EsOperateParam param){
        // 参数校验
        param.checkFieldValues();
        // 构建参数
        BulkRequestBuilder bulk = client.prepareBulk();
        param.getFieldValues().entrySet()
                .stream()
                .forEach(e->{
                    IndexRequestBuilder indexRequestBuilder = client
                            .prepareIndex(param.getIndexName(), param.getTypeName(), e.getKey())
                            .setSource(JSON.parseObject(JSON.toJSONString(e.getValue())));
                    bulk.add(indexRequestBuilder);
                });

        ListenableActionFuture<BulkResponse> future = bulk.execute();
        callback(param, future);
    }


    /**
     * 模板方法，使用callback，套用了最佳实践
     * 比较适合增删改等es操作
     *
     * @param template es操作模板函数
     * @param callback 回调函数
     */
    public <Response extends ActionResponse> void templateWithCallback(EsOperateTemplate<Response> template,
                                                                       final EsOperateCallback callback) {
        // 参数校验
        // 实际调用
        ListenableActionFuture<Response> future = template.template(esClient());
        final EsOperateCallback realCallback;
        if (callback == null) {
            realCallback = EsOperateDefaultCallback.DEFAULT;
        } else {
            realCallback = callback;
        }
        // 设置callback
        future.addListener(new ActionListener<Response>() {
            @Override
            public void onResponse(Response response) {
                realCallback.onSuccess(response);
            }

            @Override
            public void onFailure(Throwable e) {
                realCallback.onFail(e, EsOperateParam.EMPTY);
            }
        });
    }

    /**
     * 模板方法，使用超时阻塞获取异步调用结果，套用了最佳实践
     * 比较适合查询类es操作
     *
     * @param template       es操作模板函数
     * @param timeoutSeconds 超时时间，单位秒
     */
    public <Response extends ActionResponse> Response templateWithTimeout(EsOperateTemplate<Response> template,
                                                                          int timeoutSeconds) throws ElasticsearchException {
        // 参数校验
        // 实际调用
        ListenableActionFuture<Response> future = template.template(esClient());
        long timeout;
        if (timeoutSeconds <= 0) {
            timeout = defaultTimeout;
        } else {
            timeout = TimeUnit.SECONDS.toMillis(timeoutSeconds);
        }
        // 超时获取结果
        return future.actionGet(timeout);
    }

    // 获取异步调用结果
    private <T> T doGet(EsOperateParam param, ListenableActionFuture<T> future) throws ElasticsearchException {
        if (param.isSync()) {
            return future.actionGet();
        } else {
            int timeoutSeconds = param.getTimeoutSeconds();
            return future.actionGet(timeoutSeconds == 0 ? defaultTimeout : TimeUnit.SECONDS.toMillis(timeoutSeconds));
        }
    }

    // 获取异步调用结果
    private <T> T doGet(EsSearchParam param, ListenableActionFuture<T> future) throws ElasticsearchException {
        if (param.isSync()) {
            return future.actionGet();
        } else {
            int timeoutSeconds = param.getTimeoutSeconds();
            return future.actionGet(timeoutSeconds == 0 ? defaultTimeout : TimeUnit.SECONDS.toMillis(timeoutSeconds));
        }
    }

    // 设置回调
    private <T extends ActionResponse> void callback(final EsOperateParam param, ListenableActionFuture<T> future) {
        final EsOperateCallback callback;
        if (param.getCallback() == null) {
            callback = EsOperateDefaultCallback.DEFAULT;
        } else {
            callback = param.getCallback();
        }
        future.addListener(new ActionListener<T>() {
            @Override
            public void onResponse(T t) {
                callback.onSuccess(t);
            }

            @Override
            public void onFailure(Throwable e) {
                callback.onFail(e, param);
            }
        });
    }

    public List<String> getEsClusterNodes() {
        return esClusterNodes;
    }

    public void setEsClusterNodes(List<String> esClusterNodes) {
        this.esClusterNodes = esClusterNodes;
    }

    public String getEsClusterName() {
        return esClusterName;
    }

    public void setEsClusterName(String esClusterName) {
        this.esClusterName = esClusterName;
    }

    public int getEsOperateTimeoutSeconds() {
        return esOperateTimeoutSeconds;
    }

    public void setEsOperateTimeoutSeconds(int esOperateTimeoutSeconds) {
        this.esOperateTimeoutSeconds = esOperateTimeoutSeconds;
    }

    /**
     * map转对象
     *
     * @param map
     * @param documentClass
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    private static <T> T mapToObject(Map<String, SearchHitField> map, Class<T> documentClass) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (map == null) {
            return null;
        }
        Map<String, Object> source = new HashMap();
        for (Map.Entry<String, SearchHitField> entry : map.entrySet()) {
            SearchHitField field = entry.getValue();
            if (field.getValues().size() > 1) {
                source.put(field.getName(), field.getValues());
            } else {
                source.put(field.getName(), field.getValue());
            }
        }
        T obj = documentClass.newInstance();
        BeanUtils.populate(obj, source);
        return obj;
    }

    /**
     * 查询索引列表
     *
     * @return
     */
    public String[] getIndices() {
        ClusterStateResponse response = client.admin().cluster()
                .prepareState()
                .execute().actionGet();
        //获取所有索引
        return response.getState().getMetaData().getConcreteAllIndices();
    }

    /**
     * 查询指定 index下的所有type
     *
     * @param indexName
     * @return
     */
    public String[] getTypes(String indexName) {
        if (StringUtils.isBlank(indexName)) {
            return new String[0];
        }
        GetMappingsResponse mapping = client.admin().indices().prepareGetMappings(indexName).get();
        ImmutableOpenMap<String, MappingMetaData> data = mapping.getMappings().get(indexName);
        Object[] keys = data.keys().toArray();
        String[] types = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            types[i] = String.valueOf(keys[i]);
        }
        return types;
    }

    public Map<String, String> getFields(String indexName, String typeName) {
        Map<String, String> fields = new HashMap<>();
        try {
            GetMappingsResponse mapping = client.admin().indices().prepareGetMappings(indexName).get();
            Map props = mapping.getMappings().get(indexName).get(typeName).getSourceAsMap();
            if (props == null || !props.containsKey("properties")) {
                return fields;
            }
            Map<String, Object> fieldMappings = (Map) props.get("properties");
            for (Map.Entry<String, Object> entry : fieldMappings.entrySet()) {
                Map fieldIndexRecordMapping = (Map) fieldMappings.get(entry.getKey());
                String type = String.valueOf(fieldIndexRecordMapping.get("type"));
                fields.put(entry.getKey(), type);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException("es字段解析失败", e);
        }
        return fields;
    }


    public static void main(String[] args) {
        List<String> value = Lists.newArrayList();
        value.add("192.168.12.1:9000");
        value.add("192.168.12.1:9300");
        value.add("192.168.12.1:9400");

        StringUtils.join(value, ",");
        EsAction esAction = new EsAction();
        esAction.setDescription("2");
        esAction.setId(1L);
        esAction.setName("sdf");
        Map maps = JSON.parseObject(JSON.toJSONString(esAction));

        for (Object map : maps.entrySet()){
            System.out.println(((Map.Entry)map).getKey()+"     " + ((Map.Entry)map).getValue());
        }
    }
}
