package com.atguigu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    //根据检索条件进行分页查询
    @Override
    public PageResultVo querySpuByCidAndPage(PageParamVo paramVo, Long categoryId) {
        //sql语句select * from pms_spu where category_id=255 and (id=7 or name like "%7%"),区分本类和全站
        //封装查询条件
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (categoryId != 0) {//categoryId不为0，查本类
            //根据商品分类ID查询,category_id=255
            wrapper.eq("category_id", categoryId);
        }
        String key = paramVo.getKey();//获取关键字
        if (StringUtils.isNotBlank(key)) {//blank判断空格，去空格,--关键字进行查询,
            //wrapper后直接写条件，默认是and连接，并且没有括号
                        //消费性函数式接口，有参数无返回结果集
                        //(id=7 or name like "%7%")
            wrapper.and(t -> t.eq("id", key).or().like("name", key));//t代表的就是wrapper
        }
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                wrapper
        );
        return new PageResultVo(page);
    }
}
