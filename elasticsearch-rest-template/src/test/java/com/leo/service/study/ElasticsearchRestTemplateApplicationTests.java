package com.leo.service.study;

import com.leo.service.study.elasticsearch.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@SpringBootTest(classes = ElasticsearchRestTemplateApplication.class)
@Slf4j
class ElasticsearchRestTemplateApplicationTests {


    @Autowired
    public ElasticsearchRestTemplate elasticsearchRestTemplate;


    @Test
    public void findById() {
        Product product = elasticsearchRestTemplate.get("HzwfMIUBnhBSI8p_vYdc", Product.class);
        log.info("product:{}", product);
    }



}
