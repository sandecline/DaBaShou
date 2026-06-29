package com.dabashou.api.config;

import com.dabashou.common.core.AjaxResult;
import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应包装 — 将Controller返回值自动包装为 {code, msg, data} 格式
 */
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 已经是AjaxResult的不用再包装
        return !AjaxResult.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType, Class selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 已经是AjaxResult的不再包装
        if (body instanceof AjaxResult) {
            return body;
        }
        // Map中含有code字段的，说明已经格式化好了，不再包装
        if (body instanceof Map && ((Map<?, ?>) body).containsKey("code")) {
            return body;
        }
        return AjaxResult.ok(body);
    }
}
