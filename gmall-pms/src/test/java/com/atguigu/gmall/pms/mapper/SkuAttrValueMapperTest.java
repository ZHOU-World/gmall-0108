package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Date:2021/7/14
 * Author：ZHOU_World
 * Description:动态sql测试用例
 */
@SpringBootTest
class SkuAttrValueMapperTest {
    @Resource//@Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Test
    void queryMappingBySpuId(){
        List<Map<String, Object>> stringLongMap = this.skuAttrValueMapper.queryMappingBySpuId(Arrays.asList(11L, 12L, 13L, 14L));
        System.out.println(stringLongMap);
    }
}