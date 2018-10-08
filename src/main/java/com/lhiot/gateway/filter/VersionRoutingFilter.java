package com.lhiot.gateway.filter;

import com.leon.microx.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;


/**
 * 版本路由过滤器，用于根据HTTP Header中的version路由到对应版本微服务
 * @author Leon (234239150@qq.com) created in 17:42 18.10.7
 */
@Slf4j
@Component
public class VersionRoutingFilter implements GlobalFilter, Ordered {

    private static final String HTTP_HEADER_VERSION_ROUTE_KEY = "version";

    private static final int HTTP_HEADER_VERSION_ROUTE_FILTER_ORDER = LoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER - 1;

    @Override
    public int getOrder() {
        return HTTP_HEADER_VERSION_ROUTE_FILTER_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (ServerWebExchangeUtils.isAlreadyRouted(exchange)){
            return chain.filter(exchange);
        }

        String version = exchange.getRequest().getHeaders().getFirst(HTTP_HEADER_VERSION_ROUTE_KEY);
        if (StringUtils.isBlank(version)){
            return chain.filter(exchange);
        }

        if (version.contains(",")) {
            version = StringUtils.tokenizeToStringArray(version, ",")[0];
        }
        URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        if (uri == null) {
            log.warn("VersionRoutingFilter: URI is null. running next filter..." );
            return chain.filter(exchange);
        }
        String uriStr = uri.toString();
        if (uriStr.contains(version)) {
            log.info("VersionRoutingFilter: URL has version - " + uriStr);
            return chain.filter(exchange);
        }

        String newHost = StringUtils.format("{}-{}", uri.getHost(), version);
        URI newUri = URI.create(StringUtils.replace(uriStr, uri.getHost(), newHost));
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newUri);
        log.debug("VersionRoutingFilter: formatted URI - " + newUri);
        return chain.filter(exchange);
    }
}
