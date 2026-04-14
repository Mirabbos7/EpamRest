package org.example.config;

import org.example.client.WorkloadClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientsConfig {

    // TODO:
    //  Please check if @LoadBalanced is needed here
    @Bean
    public WorkloadClient workloadClient() {
        RestClient restClient = RestClient.builder()
                // TODO:
                //  It does not look like a call routed by a service name, more like a direct URL.
                //  Which is not an expected approach when using service discovery
                .baseUrl("http://localhost:9090/api/workload")
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(WorkloadClient.class);
    }
}
