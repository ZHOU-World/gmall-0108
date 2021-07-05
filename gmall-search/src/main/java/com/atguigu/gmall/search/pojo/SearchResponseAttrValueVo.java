package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * Date:2021/7/4
 * Author：ZHOU_World
 * Description:规格参数
 */
@Data
public class SearchResponseAttrValueVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValues;
}
