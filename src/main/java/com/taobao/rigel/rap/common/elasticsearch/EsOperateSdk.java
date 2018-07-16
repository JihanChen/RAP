package com.taobao.rigel.rap.common.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ListenableActionFuture;
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
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * elasticsearch操作SDK
 *
 */
public class EsOperateSdk {

    private static Logger logger = LoggerFactory.getLogger(EsOperateSdk.class);

    /**
     * es集群节点,格式为=>ip1:port1,ip2:port2...
     * 默认为本地es
     */
    private String esClusterNodes;

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
            Settings settings = Settings.builder().put("cluster.name", esClusterName).put("client.transport.ignore_cluster_name", true).build();
            client = new PreBuiltTransportClient(settings);
            Map<String, String> nodes = Splitter.on(',')
                    .trimResults()
                    .omitEmptyStrings()
                    .withKeyValueSeparator(":")
                    .split(esClusterNodes);
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
     *
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
                            .setSource();
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
            public void onFailure(Exception e) {
                realCallback.onFail(e, EsOperateParam.EMPTY);
            }
        });
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
            public void onFailure(Exception e) {
                callback.onFail(e, param);
            }
        });
    }

    public String getEsClusterNodes() {
        return esClusterNodes;
    }

    public void setEsClusterNodes(String esClusterNodes) {
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


}
