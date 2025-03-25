package com.project.cheerha.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.project.cheerha.domain.document")
public class ElasticsearchConfig {

    @Value("${elasticsearch.uris}")
    private String elasticsearchUri;

    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    @Value("${elasticsearch.fingerprint:}") // 기본값을 빈 문자열로 설정
    private String fingerprint;

    private final ObjectMapper objectMapper;

    // 🔹 ObjectMapper를 생성자 주입
    public ElasticsearchConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule()); // LocalDateTime 직렬화 지원
    }

    /**
     * Elasticsearch 클라이언트를 생성하는 메서드.
     * 기본 인증을 사용하여 Elasticsearch 서버에 연결하기 위한 RestClient를 설정합니다.
     * Jackson을 사용하여 JSON 직렬화 및 역직렬화를 처리하는 JsonpMapper를 설정합니다.
     *
     * @return ElasticsearchClient - Elasticsearch에 요청을 보내기 위한 클라이언트 인스턴스
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 기본 인증 (Username / Password)
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        // RestClient 빌드 (기본 인증만 사용)
        RestClient restClient = RestClient.builder(new HttpHost(HttpHost.create(elasticsearchUri)))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    if (fingerprint != null && !fingerprint.isEmpty()) {
                        // SSL 인증서를 검증하기 위한 컨텍스트 설정
                        httpClientBuilder = httpClientBuilder.setSSLContext(TransportUtils.sslContextFromCaFingerprint(fingerprint));
                    }
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                })
                .build();

        // ObjectMapper 기반의 JacksonJsonpMapper 생성
        JacksonJsonpMapper jacksonJsonpMapper = new JacksonJsonpMapper(objectMapper);

        ElasticsearchTransport transport = new RestClientTransport(restClient, jacksonJsonpMapper);
        return new ElasticsearchClient(transport);
    }
}
