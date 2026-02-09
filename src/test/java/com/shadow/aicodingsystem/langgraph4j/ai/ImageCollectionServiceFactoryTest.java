package com.shadow.aicodingsystem.langgraph4j.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageCollectionServiceFactoryTest {

    @Resource
    private ImageCollectionService imageCollectionService;

    @Test
    void createImageCollectionService() {
        String result = imageCollectionService.collectImages("创建一个电商购物网站，需要展示商品和品牌形象");
        Assertions.assertNotNull(result);
        System.out.println("电商网站收集到的图片： " + result);
    }
}