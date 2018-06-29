package com.taobao.rigel.rap.common.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taobao.rigel.rap.common.bo.EsAction;
import com.taobao.rigel.rap.common.elasticsearch.EsOperateCallback;
import com.taobao.rigel.rap.common.elasticsearch.EsOperateParam;
import com.taobao.rigel.rap.common.elasticsearch.EsOperateSdk;
import com.taobao.rigel.rap.project.bo.Action;
import com.taobao.rigel.rap.project.bo.Project;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * es 操作工具类
 *
 * 搜索接口名称
 */
public class EsUtils {

    private static final Logger logger = LoggerFactory.getLogger(EsUtils.class);
    public static final String INDEX_NAME = "rap_project";
    public static final String INDEX_TYPE = "rap_project_type";
    private EsOperateSdk esOperateSdk;

    private EsUtils(EsOperateSdk esOperateSdkValue){
        this.esOperateSdk = esOperateSdkValue;
    }
    public static EsUtils instance(EsOperateSdk esOperateSdk){
        return new EsUtils(esOperateSdk);
    }

    /**
     * 保存接口信息到es
     * @param action
     * @param projectId
     */
    public void createSearchIndex(Action action,int projectId){
        try {

            EsAction esProject = new EsAction();
            esProject.setProjectId(Long.valueOf(projectId));
            esProject.setId(Long.valueOf(action.getId()));
            esProject.setName(action.getName());
            esProject.setDescription(action.getDescription());
            esProject.setRequestUrl(action.getRequestUrl());
            esOperateSdk.save(EsOperateParam.saveBuilder()
                    .indexName(INDEX_NAME)
                    .typeName(INDEX_TYPE)
                    .id(esProject.getId())
                    .document(esProject)
                    .callback(new EsOperateCallback() {
                        public void onSuccess(ActionResponse result) {
                            logger.info("【es保存index】=>【SUCCESS】 项目projectId：{}，接口actionId：{}",esProject.getProjectId(), esProject.getId());
                        }
                        public void onFail(Throwable e, EsOperateParam param) {
                            logger.info("【es保存index】=>【ERROR】 参数：{} 原因：{}",param,e);
                        }
                    })
                    .build()
            );


        }catch (Exception e){
            logger.error("调用[createSearchIndex] 保存es数据出现异常！",e);
        }

    }

    /**
     * 更新es 数据
     * @param action
     * @param projectId
     */
    public void updateSearchIndex(Action action,int projectId){
        try {
            EsAction esProject = new EsAction();
            esProject.setProjectId(Long.valueOf(projectId));
            esProject.setId(Long.valueOf(action.getId()));
            esProject.setName(action.getName());
            esProject.setDescription(action.getDescription());
            esProject.setRequestUrl(action.getRequestUrl());

            esOperateSdk.update(EsOperateParam.updateBuilder()
                    .indexName(INDEX_NAME)
                    .typeName(INDEX_TYPE)
                    .id(esProject.getId())
                    .document(esProject)
                    .callback(new EsOperateCallback() {
                        @Override
                        public void onSuccess(ActionResponse result) {
                            logger.info("【es更新index】=>【SUCCESS】项目projectId：{}，接口actionId：{}",esProject.getProjectId(), esProject.getId());
                        }

                        @Override
                        public void onFail(Throwable e, EsOperateParam param) {
                            logger.info("【es更新index】=>【ERROR】入参:{} 原因：{}", param,e);
                        }
                    })
                    .build());
        }catch (Exception e){
            logger.error("调用[updateSearchIndex] 更新es数据出现异常！",e);

        }

    }

    /**
     * 查询es信息
     * @param queryvalue
     * @return
     */
    public List<EsAction> queryProjectByActionValue(String queryvalue){
        // 根据条件查询
        try {
            List<EsAction> esProject = esOperateSdk.shouldQuery(EsOperateParam.queryBuilder()
                    .indexName(INDEX_NAME)
                    .typeName(INDEX_TYPE)
                    .searchValue("name", queryvalue)
                    .searchValue("requestUrl", queryvalue)
                    .build(), EsAction.class);
            return esProject;
        }catch (Exception e){
            logger.error("调用[queryProjectByActionValue] 查询es数据出现异常！",e);
        }
        return Lists.newArrayList();
    }


    public void deleteSearchIndex(Long actionId){
        try {
            esOperateSdk.delete(EsOperateParam.deleteBuilder()
                    .indexName(INDEX_NAME)
                    .typeName(INDEX_TYPE)
                    .id(actionId)
                    .callback(new EsOperateCallback() {
                        @Override
                        public void onSuccess(ActionResponse result) {
                            logger.info("【es删除index】=>【SUCCESS】接口actionId：{}", actionId);
                        }

                        @Override
                        public void onFail(Throwable e, EsOperateParam param) {
                            logger.info("【es删除index】=>【ERROR】 参数：{} 原因：{}",param,e);
                        }
                    })
                    .build()
            );
        }catch (Exception e){
            logger.error("调用[deleteSearchIndex] 删除es数据出现异常！",e);
        }

    }

    /**
     * 批量删除es 中的记录
     */
    public void batchDeleteIndex(Set<Action> actions){

        if (CollectionUtils.isEmpty(actions)){
            return;
        }
        try {
            Map values =  actions.stream().collect(Collectors.toMap(action->String.valueOf(action.getId()), action -> action));
            esOperateSdk.batchDelete(EsOperateParam.deleteBuilder()
                    .indexName(INDEX_NAME)
                    .typeName(INDEX_TYPE).values(values).callback(new EsOperateCallback() {
                        @Override
                        public void onSuccess(ActionResponse result) {
                            logger.info("【es批量删除index】=>【SUCCESS】接口actionIds：{}", actions.stream().map(e->e.getId()).collect(Collectors.toList()));

                        }

                        @Override
                        public void onFail(Throwable e, EsOperateParam param) {
                            logger.info("【es批量删除index】=>【ERROR】 参数：{} 原因：{}",param,e);

                        }
                    }).build());

        }catch (Exception e){
            logger.error("调用[batchDeleteIndex] 查询es数据出现异常！",e);
        }

    }


    /**
     * 批量新增es 中的记录
     */
    public void batchCreateIndex(List<Project> projects){

        if (CollectionUtils.isEmpty(projects)){
            return;
        }
        try {
            // 进行project=》actions 分组
            Map<Long, Set<Action>> projectActions = Maps.newHashMap();
            projects.stream()
                    .filter(e -> CollectionUtils.isNotEmpty(e.getModuleList()))
                    .forEach(project -> {
                        Set<Action> actions = project.getModuleList().stream()
                                .filter(module -> CollectionUtils.isNotEmpty(module.getPageList()))
                                .flatMap(module -> module.getPageList().stream())
                                .filter(page -> CollectionUtils.isNotEmpty(page.getActionList()))
                                .flatMap(page -> page.getActionList().stream())
                                .collect(Collectors.toSet());
                        projectActions.put(Long.valueOf(project.getId()), actions);

                    });

            Map<String,Object> values = new HashedMap();
            projectActions.entrySet().stream().forEach(entry -> {
                Long projectId = entry.getKey();
                Map actionMap = entry.getValue().stream().map(action -> {
                    EsAction esProject = new EsAction();
                    esProject.setProjectId(projectId);
                    esProject.setId(Long.valueOf(action.getId()));
                    esProject.setName(action.getName());
                    esProject.setDescription(action.getDescription());
                    esProject.setRequestUrl(action.getRequestUrl());
                    return esProject;
                }).collect(Collectors.toMap(esaction->esaction.getId().toString(), Function.identity()));
                values.putAll(actionMap);
            });


            esOperateSdk.batchSave(EsOperateParam.deleteBuilder()
                    .indexName(INDEX_NAME)
                    .typeName(INDEX_TYPE).values(values).callback(new EsOperateCallback() {
                        @Override
                        public void onSuccess(ActionResponse result) {
                            logger.info("【es批量创建index】=>【SUCCESS】,result:{}", JSON.toJSONString(result));

                        }

                        @Override
                        public void onFail(Throwable e, EsOperateParam param) {
                            logger.info("【es批量创建index】=>【ERROR】 参数：{} 原因：{}",param,e);

                        }
                    }).build());

        }catch (Exception e){
            logger.error("调用[batchCreateIndex] 批量创建es index数据出现异常！",e);
        }

    }

}
