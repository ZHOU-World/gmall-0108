package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Date:2021/7/1
 * Author：ZHOU_World
 * Description:规格参数相关
 */
@Data
public class SearchAttrValueVo {
    @Field(type= FieldType.Long)
    private Long attrId;
    @Field(type=FieldType.Keyword)
    private String attrName;
    @Field(type=FieldType.Keyword)
    private String attrValue;
}
