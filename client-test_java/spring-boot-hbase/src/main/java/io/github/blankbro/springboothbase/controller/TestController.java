package io.github.blankbro.springboothbase.controller;

import io.github.blankbro.springboothbase.hbase.HBaseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private HBaseUtils hBaseUtils;

    @GetMapping("/test-scan")
    public Object testScan(String tableName, String startRow, String stopRow, String[] families, String[] qualifiers) throws IOException {
        log.info("tableName = {}, startRow = {}, stopRow = {}, families = {}, qualifiers = {}", tableName, startRow, stopRow, families, qualifiers);
        List<Map<String, Object>> maps = hBaseUtils.fuzzyQuery(tableName, startRow, stopRow, families, qualifiers);
        return maps;
    }
}
