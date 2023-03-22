package com.leo.service.study.elasticsearch.domain;

import com.leo.service.study.elasticsearch.domain.Headmaster;
import com.leo.service.study.elasticsearch.domain.Teacher;
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
 * 学生
 * */
@Data
@AllArgsConstructor
@NoArgsConstructor
//indexName名字如果是字母那么必须是小写字母
@Document(indexName = "student")
@Setting(shards = 3, replicas = 1)
public class Student {

    @Id
    @Field(index = true, store = true, type = FieldType.Keyword)
    private String sId;

    @Field(index = true, store = true, type = FieldType.Keyword)
    private String sName;

    @Field(index = true, store = true, type = FieldType.Text, analyzer = "ik_smart")
    //Text可以分词 ik_smart=粗粒度分词 ik_max_word 为细粒度分词
    private String sAddress;

    @Field(index = false, store = true, type = FieldType.Integer)
    private Integer sAge;

    @Field(index = false, store = true, type = FieldType.Date, format = DateFormat.basic_date_time)
    private Date sCreateTime;

    @Field(index = false, store = true, type = FieldType.Object)
    private Headmaster sHeadmaster;//班主任

    @Field(index = true, store = false, type = FieldType.Keyword)
    private String[] sCourseList; //数组类型 由数组中第一个非空值决定(这里数组和集合一个意思了)

    @Field(index = true, store = false, type = FieldType.Keyword)
    private List<String> sColorList; //集合类型 由数组中第一个非空值决定


    @Field(index = true, store = false, type = FieldType.Nested)//嵌套类型list里泛型是object形式的或自定义对象
    private List<Teacher> sTeacherList; //教所有科目的老师

}

