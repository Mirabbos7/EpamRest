package org.example.config;

import org.example.client.WorkloadClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientsConfig {

    @Bean
    public WorkloadClient workloadClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:9090/api/workload")
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(WorkloadClient.class);
    }
}
