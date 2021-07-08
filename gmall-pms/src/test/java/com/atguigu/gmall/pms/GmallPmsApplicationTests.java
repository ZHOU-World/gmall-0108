package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class GmallPmsApplicationTests {
    @Resource//@Autowired
    private CategoryMapper categoryMapper;

    @Test
    void queryLvl2WithSubsByPid() {
        this.categoryMapper.queryLvl2WithSubsByPid(1L).forEach(System.out::println);
    }
}

