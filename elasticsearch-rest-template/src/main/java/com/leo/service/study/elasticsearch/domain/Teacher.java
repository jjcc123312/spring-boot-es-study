package com.leo.service.study.elasticsearch.domain;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.suggest.Completion;

/*
 * 科目老师
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "teacher")
@Setting(shards = 3, replicas = 1)
public class Teacher {

    //主键id
    @Id
    @Field(index = true, store = true, type = FieldType.Keyword)//index:设置通过这个字段是否可以进行搜索
    private String tId;

    //姓名
    @Field(index = true, store = true, type = FieldType.Keyword)
    private String tName;

    //英文姓名
    @Field(index = true, store = true, type = FieldType.Keyword)
    private String tEnglishName;

    //班级
    @Field(index = true, store = true, type = FieldType.Keyword)
    private String tClassName;

    //地址
    @MultiField(mainField = @Field(index = true, store = true, type = FieldType.Text, analyzer = "ik_smart"), otherFields = {
        @InnerField(type = FieldType.Keyword, suffix = "keyword")
    })
    private String tAddress;

    //至理名言
    @Field(index = true, store = true, type = FieldType.Keyword)
    private String tFamous;

    //年龄
    @Field(index = true, store = true, type = FieldType.Integer)
    private Integer tAge;

    //日期
    @Field(index = true, store = true, type = FieldType.Date, format = DateFormat.basic_date_time)
    private Date tCreateTime;


    //定义关键词索引 要完成补全搜索，必须要用到特殊的数据类型completion，
    //要汉字拼音都能补全，必须要使用自定义的ik+pinyin分词器
    //                                                       maxInputLength:设置单个输入的长度，默认为50 UTF-16 代码点
    @CompletionField(analyzer = "ik_smart", searchAnalyzer = "ik_smart", maxInputLength = 100)
    private Completion completion;

    public Teacher(String tId, String tName, String tEnglishName, String tClassName, String tAddress, Integer tAge, Date tCreateTime) {
        this.tId = tId;
        this.tName = tName;
        this.tEnglishName = tEnglishName;
        this.tClassName = tClassName;
        this.tAddress = tAddress;
        this.tAge = tAge;
        this.tCreateTime = tCreateTime;
    }
}

