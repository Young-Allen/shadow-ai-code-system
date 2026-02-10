package com.shadow.aicodingsystem;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;

@EnableCaching
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.shadow.aicodingsystem.mapper")
@ComponentScan(
        basePackages = "com.shadow.aicodingsystem",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.shadow\\.aicodingsystem\\.genresult\\..*"
        )
)
public class AiCodingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodingSystemApplication.class, args);
    }

}
