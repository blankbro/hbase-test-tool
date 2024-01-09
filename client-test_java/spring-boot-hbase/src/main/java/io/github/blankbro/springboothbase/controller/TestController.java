package io.github.blankbro.springboothbase.controller;

import io.github.blankbro.springboothbase.hbase.HBaseUtils;
import io.github.blankbro.springboothbase.util.TimeUtil;
import io.github.blankbro.springboothbase.util.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private HBaseUtils hBaseUtils;

    /**
     * @param tableName
     * @param startRow
     * @param stopRow
     * @param families
     * @param qualifiers
     * @return
     * @throws IOException
     */
    @GetMapping("/test-scan")
    public Object testScan(String tableName, String startRow, String stopRow, String[] families, String[] qualifiers) throws IOException {
        TraceIdUtil.setupTraceId();
        log.info("tableName = {}, startRow = {}, stopRow = {}, families = {}, qualifiers = {}", tableName, startRow, stopRow, families, qualifiers);
        long start = System.nanoTime();
        List<Map<String, Object>> maps = hBaseUtils.fuzzyQuery(tableName, startRow, stopRow, families, qualifiers);
        long end = System.nanoTime();
        String handleTime = TimeUtil.formatDuration(Duration.ofNanos(end - start));
        log.info("耗时：{}", handleTime);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scanResultCount", maps.size());
        result.put("handleTime", handleTime);
        return result;
    }
}
