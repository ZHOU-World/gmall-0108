package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Date:2021/6/25
 * Author：ZHOU_World
 * Description:对应json数据中的baseAttrs属性
 */
//@Data 手动重写方法，不需要@Data自动生成
public class SpuAttrValueVo extends SpuAttrValueEntity {
    //接收数据，本质是调用属性的set,get方法
    private List<String> valueSelected;
    //重写set方法（扩展的字段valueSelected）
    public void setValueSelected(List<String> valueSelected) {
        //进行判断集合valueSelected是否为空
        if(CollectionUtils.isEmpty(valueSelected)){
            return;//为空终止
        }
        //将valueSelected的值赋值给父类中的AttrValue
                        //将list集合转化为字符串类型
        this.setAttrValue(StringUtils.join(valueSelected,","));//将集合转为字符串，用逗号分隔开
    }
}
