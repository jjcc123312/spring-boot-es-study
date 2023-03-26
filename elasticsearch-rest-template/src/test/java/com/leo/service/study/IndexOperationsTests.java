/*
 * Copyright(c) 2022 长沙市希尚网络科技有限公司
 * 注意：本内容仅限于长沙市希尚网络科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.leo.service.study;

import com.alibaba.fastjson.JSONObject;
import com.leo.service.study.elasticsearch.domain.Headmaster;
import com.leo.service.study.elasticsearch.domain.Student;
import com.leo.service.study.elasticsearch.domain.Teacher;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexInformation;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.Settings;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

/**
 * TODO
 *
 * @author Leo
 * @version 1.0 2023/3/20
 */
@SpringBootTest(classes = ElasticsearchRestTemplateApplication.class)
@Slf4j
public class IndexOperationsTests {

    @Autowired
    public ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 创建索引
     * @author Leo
     */
    @Test
    public void indexCreate() {
        commonIndexCreate(Student.class);
        commonIndexCreate(Headmaster.class);
        commonIndexCreate(Teacher.class);
    }

    private <T> void commonIndexCreate(Class<T> tClass) {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(tClass);
        // 创建索引
        boolean b = indexOperations.create();
        boolean b1 = false;
        if (b) {
            // 创建mapping
            Document mapping = indexOperations.createMapping();
            b1 = indexOperations.putMapping(mapping);
        }
        log.info("创建索引及mapping结果:{}", b && b1);
    }

    /**
     * 删除索引
     * @author Leo
     */
    @Test
    public void deleteIndex() {
        IndexCoordinates of = IndexCoordinates.of("studnet_new1");
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(of);
        boolean delete = indexOperations.delete();
        log.info("删除索引结果：{}", delete);
    }

    /**
     * 查看索引信息
     * @author Leo
     */
    @Test
    public void searchIndexInfo() {
        IndexCoordinates of = IndexCoordinates.of("student_new1");
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(of);
        List<IndexInformation> information = indexOperations.getInformation();
        Map<String, Object> mapping = indexOperations.getMapping();
        Settings settings = indexOperations.getSettings();
        log.info("indexInfo：{}", JSONObject.toJSONString(information));
        log.info("mappings：{}", mapping);
        log.info("settings：{}", settings);
    }


}
