package com.lhiot.gateway.filter;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 访问限制过滤器
 *
 * @author Leon (234239150@qq.com) created in 15:56 18.10.8
 */
@Slf4j
@Component
public class UriAccessLimitGatewayFilterFactory extends AbstractGatewayFilterFactory<UriAccessLimitGatewayFilterFactory.UriAccessLimitConfig> {

    public UriAccessLimitGatewayFilterFactory() {
        super(UriAccessLimitConfig.class);
    }

    private AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("patterns");
    }

    @Override
    public GatewayFilter apply(UriAccessLimitConfig config) {
        return (exchange, chain) -> {
            URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            if (Objects.nonNull(uri)) {
                String uriStr = uri.getPath();
                for (String pattern : config.getPatterns()) {
                    if (pattern.equalsIgnoreCase(uriStr) || matcher.match(pattern, uriStr)) {
                        ServerWebExchangeUtils.setAlreadyRouted(exchange);
                        ServerWebExchangeUtils.setResponseStatus(exchange, HttpStatus.UNAUTHORIZED);
                        return Mono.empty(); // complete
                    }
                }
            }
            return chain.filter(exchange);
        };
    }

    @Data
    @ToString
    public final static class UriAccessLimitConfig {
        private List<String> patterns;
    }
}