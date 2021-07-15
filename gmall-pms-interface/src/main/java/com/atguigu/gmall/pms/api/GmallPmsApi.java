package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Date:2021/7/1
 * Author：ZHOU_World
 * Description:
 */
public interface GmallPmsApi {

    //根据分类id、spuId、skuId查询出所有的规格参数组及组下的规格参数和值
    @GetMapping("pms/attrgroup/with/attr/value/{cid}")
    public ResponseVo<List<GroupVo>> queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId);

    //根据skuId查询sku图片列表
    @GetMapping("pms/skuimages/sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId")Long skuId);

    //接口一 分页查询spu(feign传递值)
    @PostMapping("pms/spu/page")
                    //返回值为当前页的分页数据
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    //同步数据：根据spuId查询spu详情信息
    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    //接口二 查询spu(统一规格)下的所有sku（不同）信息 api.gmall.com/pms/sku/spu/7
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId")Long spuId);

    //根据skuId查询sku
    @GetMapping("pms/sku/{id}")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    //接口四 根据品牌Id查询品牌
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    //接口五 根据分类id查询分类
    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    //根据父id查询分类(一级分类)
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoryByPid(@PathVariable("parentId") Long parentId);

    //根据一级分类查询二级、三级分类
    @GetMapping("pms/category/subs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLvl2WithSubsByPid(@PathVariable("pid")Long pid);

    //根据三级分类的id(cid)获取一二三级分类
    @GetMapping("pms/category/sub/{cid3}")
    public ResponseVo<List<CategoryEntity>> queryLvl123WithSubByCid3(@PathVariable("cid3")Long cid);

    //接口六--根据caregoryId和spuId查询基本类型的搜索类型（search_tupe=1）的规格参数和值
    @GetMapping("pms/skuattrvalue/search/{cid}")
    public  ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("skuId") Long skuId
    );

    //根据spuId查询spuId下所有销售属性的可取值
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrValuesBySpuId(@PathVariable("spuId")Long spuId);

    //查询当前商品的销售属性
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueBySkuId(@PathVariable("skuId")Long skuId);

    //根据spuId查询spu下的销售属性和skuId的映射关系
    @GetMapping("pms/skuattrvalue/mapping/{spuId}")
    public ResponseVo<String> queryMappingBySpuId(@PathVariable("spuId")Long spuId);

    //接口七--根据caregoryId和spuId查询基本类型的搜索类型（search_tupe=1）的规格参数和值
    @GetMapping("pms/spuattrvalue/search/{cid}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValuesBySpuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId
    );

    //根据spuId查询商品详情信息
    @GetMapping("pms/spudesc/{spuId}")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);
}
