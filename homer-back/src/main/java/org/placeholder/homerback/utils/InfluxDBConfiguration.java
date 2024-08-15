package org.placeholder.homerback.utils;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfiguration {
    @Value("${influx.url}")
    private String url;
    @Value("${influx.organization}")
    private String organization;
    @Value("${influx.bucket}")
    private String bucket;
    @Value("${influx.token}")
    private String token;

    public InfluxDBConfiguration() {}

    @Bean
    public InfluxDBClient influxDbClient() {
        return InfluxDBClientFactory.create(this.url, token.toCharArray(),
                this.organization, this.bucket);
    }

}
