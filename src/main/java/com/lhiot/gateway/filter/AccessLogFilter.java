package com.lhiot.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 访问日志过滤器，记录客户端访问信息
 *
 * @author Leon (234239150@qq.com) created in 17:42 18.10.7
 */
@Slf4j
@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).doFinally(signalType -> {
            if (ServerWebExchangeUtils.isAlreadyRouted(exchange)) {
                log.info("api '{}' response: Http Code - {}",
                        exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR), exchange.getResponse().getStatusCode()
                );
            }
        });
    }
}
