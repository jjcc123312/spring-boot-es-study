package com.leo.service.study.elasticsearch.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

/**
 * @author Leo
 * @version 1.0 2023/3/31
 */
@Data
@Document(indexName = "test_index_obj")
@Setting(shards = 3, replicas = 1)
public class TestIndexObject {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(index = true, store = true, type = FieldType.Keyword)
    private String name;

    //英文姓名
    @Field(index = true, store = true, type = FieldType.Keyword)
    private String englishName;

    private String alias;

    private String age;
}
