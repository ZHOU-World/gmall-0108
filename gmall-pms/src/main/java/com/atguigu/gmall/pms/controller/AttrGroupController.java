package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import javax.xml.ws.Response;

/**
 * 属性分组
 *
 * @author ZHOU_World
 * @email 3361949746@qq.com
 * @date 2021-06-22 21:27:20
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    //根据分类id、spuId、skuId查询出所有的规格参数组及组下的规格参数和值
    //现根据三级分类id查询分组id,再根据分组Id查询每个分组下的规格参数，再结合规格参数查询值（skuId，spuId）
    @GetMapping("with/attr/value/{cid}")
    public ResponseVo<List<GroupVo>> queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId){
        List<GroupVo> groupVos = this.attrGroupService
                                .queryGroupsWithAttrValuesByCidAndSpuIdAndSkuId(cid,spuId,skuId);
        return ResponseVo.ok(groupVos);
    }

    //根据三级分类id查询分组
    @GetMapping("withattrs/{catId}")
    @ApiOperation("查询分类下的组及规格参数")
    public ResponseVo<List<AttrGroupEntity>> queryGroupsWithAttrsByCid(@PathVariable("catId")Long cid){
        List<AttrGroupEntity> groupEntities = this.attrGroupService.queryGroupsWithAttrsByCid(cid);
        return ResponseVo.ok(groupEntities);
    }

    @GetMapping("category/{cid}")
    @ApiOperation("查询分类的分组")
    public ResponseVo<List<AttrGroupEntity>> queryGroupsById(@PathVariable("cid")Long cid){
//        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
//        QueryWrapper<AttrGroupEntity> category_id = wrapper.eq("category_id", cid);
//        List<AttrGroupEntity> list = this.attrGroupService.list(category_id);
//        return ResponseVo.ok(list) ;
        List<AttrGroupEntity> groupEntities = this.attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        return ResponseVo.ok(groupEntities);
    }
    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
