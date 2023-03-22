/*
 * Copyright(c) 2022 长沙市希尚网络科技有限公司
 * 注意：本内容仅限于长沙市希尚网络科技有限公司内部传阅，禁止外泄以及用于其他的商业目的
 */

package com.leo.service.study;

import com.leo.service.study.elasticsearch.domain.Student;
import com.leo.service.study.elasticsearch.repository.ProductDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;

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

    @Autowired
    public ProductDao productDao;

    @Test
    public void indexCreate() {
        // Student build = Student.builder()
        //     .age(27)
        //     .sex("man")
        //     .name("leo").headPictureUrl("www.image.com").build();
        // elasticsearchRestTemplate.save(build);
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(Student.class);
        indexOperations.create();

        productDao.findById("");

        Document mapping = indexOperations.createMapping();

    }
}
