package studio.greeks.vilomoyu.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 配置Influx
 *
 * @author <a href="mailto:wuzhao-1@thunisoft.com>Zhao.Wu</a>
 * @description studio.greeks.vilomoyu.config vilomoyu
 * @date 2020/9/1 0001 14:37
 */
@Configuration
public class InfluxConfig {
    @Value("${spring.influx.url:'http://localhost:8086'}")
    private String url;

    @Value("${spring.influx.user:''}")
    private String username;

    @Value("${spring.influx.password:''}")
    private String password;

    @Value("${spring.influx.database:''}")
    private String database;

    @Bean
    public InfluxDB influx() {
        InfluxDB influx = InfluxDBFactory.connect(url,username, password);
        influx.setDatabase(database)
                .enableBatch(100, 2000, TimeUnit.MILLISECONDS);
        influx.setLogLevel(InfluxDB.LogLevel.BASIC);
        return influx;
    }
}
