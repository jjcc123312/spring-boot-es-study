package com.leo.service.study.elasticsearch.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.util.Date;
import java.util.List;
import org.springframework.data.elasticsearch.annotations.Setting;

/*
 * 班主任
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "headmaster")
@Setting(shards = 3, replicas = 1)
public class Headmaster {

    @Id
    @Field(index = true, store = false, type = FieldType.Keyword)
    private String hId;

    @Field(index = true, store = false, type = FieldType.Keyword)
    private String hName;

    @Field(index = true, store = false, type = FieldType.Keyword)
    private String hAddress;

    @Field(index = false, store = false, type = FieldType.Integer)
    private Integer hSalary;

    @Field(index = false, store = false, type = FieldType.Keyword)
    private List<String> hClass;

    @Field(index = true, store = true, type = FieldType.Date, format = DateFormat.basic_date_time)
    private Date  hCreateTime;

    public Headmaster(String hName, String hAddress, Integer hSalary, List<String> hClass, Date hCreateTime) {
        this.hName = hName;
        this.hAddress = hAddress;
        this.hSalary = hSalary;
        this.hClass = hClass;
        this.hCreateTime = hCreateTime;
    }
}

