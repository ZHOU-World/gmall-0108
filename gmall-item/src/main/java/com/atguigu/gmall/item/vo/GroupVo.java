package com.atguigu.gmall.item.vo;
import lombok.Data;
import java.util.List;

/**
 * Date:2021/7/13
 * Author：ZHOU_World
 * Description:分组集合
 */
@Data
public class GroupVo {
    private String name;//分组的名称
    private List<AttrValueVo> attrs;//分组中的值
}
