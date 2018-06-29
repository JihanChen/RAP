package com.taobao.rigel.rap.common.elasticsearch;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.client.transport.TransportClient;

/**
 * Es操作模板函数
 *
 */
public interface EsOperateTemplate<Response extends ActionResponse> {

    ListenableActionFuture<Response> template(TransportClient client);
}
