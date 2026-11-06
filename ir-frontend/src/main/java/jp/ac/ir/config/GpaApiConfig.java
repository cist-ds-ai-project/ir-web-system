package jp.ac.ir.config;

import jp.ac.ir.repository.gpa.GpaApiClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;

@Configuration
public class GpaApiConfig {

    @Bean
    public GpaApiClient gpaApiClient() {

        WebClient webClient = WebClient.builder()
                .baseUrl(fast.api)
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory
                        .builderFor(WebClientAdapter.create(webClient))
                        .build();

        return factory.createClient(GpaApiClient.class);
    }
}
