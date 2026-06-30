package com.dabashou.api.handler;

import com.dabashou.common.core.AjaxResult;
import com.dabashou.common.enums.ErrorCode;
import com.dabashou.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<AjaxResult<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AjaxResult<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", message);
        return fail(ErrorCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<AjaxResult<Void>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("约束违反: {}", e.getMessage());
        return fail(ErrorCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<AjaxResult<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return fail(ErrorCode.BAD_REQUEST, "缺少参数: " + e.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AjaxResult<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体不可读: {}", e.getMessage());
        return fail(ErrorCode.BAD_REQUEST, "请求体格式错误");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<AjaxResult<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: {}", e.getName());
        return fail(ErrorCode.BAD_REQUEST, "参数类型错误: " + e.getName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<AjaxResult<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMethod());
        return fail(405, "请求方法不支持: " + e.getMethod());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AjaxResult<Void>> handleAccessDenied(AccessDeniedException e) {
        log.warn("访问被拒绝: {}", e.getMessage());
        return fail(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AjaxResult<Void>> handleAuthentication(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return fail(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AjaxResult<Void>> handleBadCredentials(BadCredentialsException e) {
        log.warn("凭证错误: {}", e.getMessage());
        return fail(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AjaxResult<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return fail(ErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<AjaxResult<Void>> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMsg());
    }

    private ResponseEntity<AjaxResult<Void>> fail(ErrorCode errorCode, String message) {
        return fail(errorCode.getCode(), message);
    }

    private ResponseEntity<AjaxResult<Void>> fail(int code, String message) {
        return ResponseEntity.status(resolveStatus(code)).body(AjaxResult.fail(code, message));
    }

    private HttpStatus resolveStatus(int code) {
        HttpStatus status = HttpStatus.resolve(code);
        return status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
    }
}
