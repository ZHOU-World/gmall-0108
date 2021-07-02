package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Date:2021/7/1
 * Author：ZHOU_World
 * Description:
 */
public interface GmallPmsApi {
    //接口一 分页查询spu(feign传递值)
    @PostMapping("pms/spu/page")
                    //返回值为当前页的分页数据
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    //接口二 查询spu(统一规格)下的所有sku（不同）信息 api.gmall.com/pms/sku/spu/7
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId")Long spuId);

    //接口四 根据品牌Id查询品牌
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    //接口五 根据分类id查询分类
    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    //接口六--根据caregoryId和spuId查询基本类型的搜索类型（search_tupe=1）的规格参数和值
    @GetMapping("pms/skuattrvalue/search/{cid}")
    public  ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("skuId") Long skuId
    );

    //接口七--根据caregoryId和spuId查询基本类型的搜索类型（search_tupe=1）的规格参数和值
    @GetMapping("pms/spuattrvalue/search/{cid}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValuesBySpuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId
    );
}
