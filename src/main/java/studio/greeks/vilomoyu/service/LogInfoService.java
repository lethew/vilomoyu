package studio.greeks.vilomoyu.service;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import studio.greeks.vilomoyu.model.LogInfo;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * TODO:
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>Zhao.Wu</a>
 * @description studio.greeks.vilomoyu.service vilomoyu
 * @date 2020/9/1 0001 14:45
 */
@Service
public class LogInfoService {
    @Autowired
    private InfluxDB influx;

    public void importZip(MultipartFile file) {

        BatchPoints batch = BatchPoints.builder()
                .tag("async", "true")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        Point point = Point.measurementByPOJO(LogInfo.class)
                .addFieldsFromPOJO(new LogInfo())
                .build();
        batch.point(point);
        influx.write(batch);
    }

    private List<LogInfo> parseLogFile(File logFile) {
        return Collections.emptyList();
    }

    private <T> void save(List<T> logInfos) {
        BatchPoints batch = BatchPoints.builder()
                .tag("async", "true")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        logInfos.stream()
                .map(t -> Point.measurementByPOJO(t.getClass())
                            .addFieldsFromPOJO(t)
                            .build())
                .forEach(batch::point);
        influx.write(batch);
    }
}
