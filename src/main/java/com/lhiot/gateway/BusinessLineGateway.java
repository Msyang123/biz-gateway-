package com.lhiot.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Objects;

/**
 * 业务线统一网关
 *
 * @author Leon (234239150@qq.com) created in 17:42 18.10.7
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BusinessLineGateway {

    private static final String ALL = "*";
    private static final String MAX_AGE = "18000L";
    private static final String METHODS = "GET, PUT, POST, DELETE, OPTIONS";

    @Bean
    public WebFilter webFilter() {
        return (ctx, chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            if (CorsUtils.isCorsRequest(request)) {
                ServerHttpResponse response = ctx.getResponse();
                HttpHeaders headers = response.getHeaders();
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALL);
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, METHODS);
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALL);
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE.toString());
                headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            return chain.filter(ctx);
        };
    }

    /**
     * 自定义限流标志的key，多个维度可以从这里入手
     * exchange对象中获取服务ID、请求信息，用户信息等
     */
    @Bean("remoteAddressKeyResolver")
    public KeyResolver remoteAddressKeyResolver() {
        return exchange -> {
            String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            return Mono.just(userAgent
                    + (Objects.nonNull(remoteAddress) ? remoteAddress.toString() : "")
                    + (Objects.nonNull(uri) ? uri.toString() : "")
            );
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(BusinessLineGateway.class, args);
    }
}
