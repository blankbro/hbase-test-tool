package io.github.blankbro.springboothbase.hbase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.blankbro.springboothbase.hbase.config.HBaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
@Component
public class HBaseUtils {

    private Connection connection;

    private Admin admin;

    public HBaseUtils(@Autowired HBaseProperties hbaseProperties) {
        Configuration conf = HBaseConfiguration.create();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("hBase-pool-%d").build();
        ExecutorService executor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 2,
                0, TimeUnit.NANOSECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory
        );
        try {
            // 将hBase配置类中定义的配置加载到连接池中每个连接里
            Map<String, String> properties = hbaseProperties.getProperties();
            if (null != properties && !properties.isEmpty()) {
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    conf.set(entry.getKey(), entry.getValue());
                }
                Connection connection = ConnectionFactory.createConnection(conf, executor);
                this.admin = connection.getAdmin();
            }
        } catch (IOException e) {
            log.error("HBaseUtils实例初始化失败！错误信息为：{}", e.getMessage(), e);
        }
    }

    public Put buildPut(String row, String[] families, String[] qualifiers, Object[] values) {
        Put put = new Put(Bytes.toBytes(row));
        // 设置不写hlog
        put.setDurability(Durability.SKIP_WAL);
        for (int i = 0; i < qualifiers.length; i++) {
            if (values[i] != null) {
                put.addColumn(Bytes.toBytes(families[i]), Bytes.toBytes(qualifiers[i]), values[i] instanceof byte[] ? (byte[]) values[i] : Bytes.toBytes(values[i].toString()));
            } else {
                put.addColumn(Bytes.toBytes(families[i]), Bytes.toBytes(qualifiers[i]), Bytes.toBytes("-"));
            }
        }
        return put;
    }

    /**
     * 指定版本写入 put构建
     *
     * @param row
     * @param families
     * @param qualifiers
     * @param values
     * @param timestamp
     * @return
     */
    public Put buildPut(String row, String[] families, String[] qualifiers, Object[] values, long timestamp) {
        Put put = new Put(Bytes.toBytes(row), timestamp);
        // 设置不写hlog
        put.setDurability(Durability.SKIP_WAL);
        for (int i = 0; i < qualifiers.length; i++) {
            if (values[i] != null) {
                put.addColumn(Bytes.toBytes(families[i]), Bytes.toBytes(qualifiers[i]), values[i] instanceof byte[] ? (byte[]) values[i] : Bytes.toBytes(values[i].toString()));
            } else {
                put.addColumn(Bytes.toBytes(families[i]), Bytes.toBytes(qualifiers[i]), Bytes.toBytes("-"));
            }
        }
        return put;
    }

    /**
     * 批量设置写入
     *
     * @param tableName 表名
     * @param list      put list
     * @throws IOException
     */
    public void insertRecords(String tableName, List<Put> list) throws IOException {
        TableName name = TableName.valueOf(tableName);
        Table table = connection.getTable(name);
        table.put(list);
    }

    /**
     * 查询指定类
     *
     * @param tableName  表名称
     * @param startRow   开始rowKey
     * @param stopRow    结束rowKey
     * @param qualifiers 列名list
     * @return 结果集
     * @throws Exception
     */
    public List<Map<String, Object>> fuzzyQuery(String tableName, String startRow, String stopRow, String[] families, String[] qualifiers) throws IOException {
        Table table = connection.getTable(TableName.valueOf(tableName));

        Queue<Map<String, Object>> queue = Queues.newConcurrentLinkedQueue();
        // 构建scan
        Scan scan = new Scan();
        scan.withStartRow(startRow.getBytes());
        scan.withStopRow(stopRow.getBytes(), true);
        if (qualifiers.length > 0) {
            for (int i = 0; i < qualifiers.length; i++) {
                scan.addColumn(families[i].getBytes(), qualifiers[i].getBytes());
            }
        }

        ResultScanner scanner = table.getScanner(scan);
        List<Map<String, Object>> result = getResult(Lists.newArrayList(), scanner, Lists.newArrayList());
        if (!result.isEmpty()) {
            queue.addAll(result);
        }

        List<Map<String, Object>> resultList = Lists.newArrayList();
        resultList.addAll(queue);
        return resultList;
    }

    private List<Map<String, Object>> getResult(List<Map<String, Object>> resultList, ResultScanner scanner, List<String> reservedField) {
        try {
            for (Result result : scanner) {
                resultList.add(getRow(result, reservedField));
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return resultList;
    }

    /**
     * 结果集整理（根据保留字段做整理）
     *
     * @param result 结果集
     * @return
     */
    private Map<String, Object> getRow(Result result, List<String> reservedField) {
        if (reservedField.size() == 0) {
            return getRow(result);
        }

        Map<String, Object> map = Maps.newHashMap();
        for (Cell cell : result.rawCells()) {
            String qualifier = new String(CellUtil.cloneQualifier(cell));
            if (reservedField.contains(qualifier)) {
                map.put(qualifier, Bytes.toString(CellUtil.cloneValue(cell)));
            }
        }
        return map;
    }

    /**
     * 结果集整理
     *
     * @param result 结果集
     * @return
     */
    private Map<String, Object> getRow(Result result) {
        Map<String, Object> map = Maps.newHashMap();
        for (Cell cell : result.rawCells()) {
            String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
            byte[] values = CellUtil.cloneValue(cell);
            String val = Bytes.toString(values);
            map.put(qualifier, val);
        }
        return map;
    }

    /**
     * 解析特殊字符
     *
     * @param keys    keys list
     * @param values  value list
     * @param message 消息体
     */
    public void resolve(List<String> keys, List<Object> values, Map<String, Object> message) {
        for (Map.Entry<String, Object> entry : message.entrySet()) {
            keys.add(entry.getKey());
            values.add(entry.getValue());
        }
    }
}
