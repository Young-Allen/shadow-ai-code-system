package com.shadow.aicodingsystem.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.shadow.aicodingsystem.langgraph4j.model.ImageResource;
import com.shadow.aicodingsystem.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import dev.langchain4j.agent.tool.P;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class UndrawIllustrationTool {

    private static final String UNDRAW_HOME_URL = "https://undraw.co";
    private static final String UNDRAW_API_URL = UNDRAW_HOME_URL + "/_next/data/%s/search/%s.json?term=%s";
    private static final Pattern BUILD_ID_PATTERN = Pattern.compile("\"buildId\"\\s*:\\s*\"([^\"]+)\"");
    private static final long BUILD_ID_TTL_MILLIS = 60 * 60 * 1000L;

    private volatile String cachedBuildId;
    private volatile long cachedBuildIdAtMillis;

    @Tool("搜索插画图片，用于网站美化和装饰")
    public List<ImageResource> searchIllustrations(@P("搜索关键词") String query) {
        List<ImageResource> imageList = new ArrayList<>();
        if (StrUtil.isBlank(query)) {
            return imageList;
        }
        int searchCount = 12;

        String buildId = getBuildId();
        if (StrUtil.isBlank(buildId)) {
            return imageList;
        }

        String encodedQueryForParam = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String encodedQueryForPath = encodedQueryForParam.replace("+", "%20");
        String apiUrl = String.format(UNDRAW_API_URL, buildId, encodedQueryForPath, encodedQueryForParam);

        // 使用 try-with-resources 自动释放 HTTP 资源
        try (HttpResponse response = HttpRequest.get(apiUrl)
                .header("User-Agent", "Mozilla/5.0")
                .timeout(10000)
                .execute()) {
            if (!response.isOk()) {
                return imageList;
            }
            JSONObject result = JSONUtil.parseObj(response.body());
            JSONObject pageProps = result.getJSONObject("pageProps");
            if (pageProps == null) {
                JSONObject props = result.getJSONObject("props");
                if (props != null) {
                    pageProps = props.getJSONObject("pageProps");
                }
            }
            if (pageProps == null) {
                return imageList;
            }
            JSONArray initialResults = pageProps.getJSONArray("initialResults");
            if (initialResults == null) {
                initialResults = pageProps.getJSONArray("results");
            }
            if (initialResults == null || initialResults.isEmpty()) {
                return imageList;
            }
            int actualCount = Math.min(searchCount, initialResults.size());
            for (int i = 0; i < actualCount; i++) {
                JSONObject illustration = initialResults.getJSONObject(i);
                String title = illustration.getStr("title", "插画");
                String media = illustration.getStr("media", "");
                if (StrUtil.isNotBlank(media)) {
                    imageList.add(ImageResource.builder()
                            .category(ImageCategoryEnum.ILLUSTRATION)
                            .description(title)
                            .url(media)
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("搜索插画失败：{}", e.getMessage(), e);
        }
        return imageList;
    }

    private String getBuildId() {
        long now = System.currentTimeMillis();
        String current = cachedBuildId;
        if (StrUtil.isNotBlank(current) && (now - cachedBuildIdAtMillis) < BUILD_ID_TTL_MILLIS) {
            return current;
        }

        String resolved = resolveBuildId();
        if (StrUtil.isNotBlank(resolved)) {
            cachedBuildId = resolved;
            cachedBuildIdAtMillis = now;
            return resolved;
        }

        return current;
    }

    private String resolveBuildId() {
        try (HttpResponse response = HttpRequest.get(UNDRAW_HOME_URL)
                .header("User-Agent", "Mozilla/5.0")
                .timeout(10000)
                .execute()) {
            if (!response.isOk()) {
                log.warn("Failed to fetch Undraw homepage for buildId, status={}", response.getStatus());
                return null;
            }
            String body = response.body();
            Matcher matcher = BUILD_ID_PATTERN.matcher(body);
            if (matcher.find()) {
                return matcher.group(1);
            }
            log.warn("Could not parse Undraw buildId from homepage response");
        } catch (Exception e) {
            log.warn("Failed to resolve Undraw buildId: {}", e.getMessage());
        }
        return null;
    }
}
