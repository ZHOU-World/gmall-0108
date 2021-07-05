package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * Date:2021/7/3
 * Author：ZHOU_World
 * Description:接收搜索参数的数据模型
 */
@Data
public class SearchParamVo {
    //查询关键字
    private String keyword;

    //品牌过滤
    private List<Long> brandId;

    //分类过滤
    private List<Long> categoryId;

    //规格参数过滤（“4：8G=12G”，“5：128G,256G”）
    private List<String> props;

    //价格区间过滤
    private Double priceFrom;
    private Double priceTo;

    //仅显示有货
    private Boolean store;

    //排序：0-默认排序 1-价格降序 2-价格升序 3-销量的降序 4-新品降序
    private Integer sort = 0;

    //页码
    private Integer pageNum = 1;
    private final Integer pageSize =20;
}
