package com.yd.vibecode.global.resolver;

import static com.yd.vibecode.global.exception.code.status.AuthErrorStatus.INVALID_ACCESS_TOKEN;
import static com.yd.vibecode.global.exception.code.status.GlobalErrorStatus._UNAUTHORIZED;

import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.security.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final TokenProvider tokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (parameter.getParameterAnnotation(CurrentUser.class) == null) {
            return false;
        }
        Class<?> type = parameter.getParameterType();
        return String.class.isAssignableFrom(type)
                || Long.class.equals(type)
                || long.class.equals(type);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        if (request == null) {
            throw new RestApiException(_UNAUTHORIZED);
        }

        String token = tokenProvider.getToken(request)
                .orElseThrow(() -> {
                    return new RestApiException(_UNAUTHORIZED);
                });

        String userId = tokenProvider.getId(token)
                .orElseThrow(() -> {
                    return new RestApiException(_UNAUTHORIZED);
                });

        Class<?> paramType = parameter.getParameterType();
        if (Long.class.equals(paramType) || long.class.equals(paramType)) {
            try {
                return Long.parseLong(userId);
            } catch (NumberFormatException e) {
                throw new RestApiException(INVALID_ACCESS_TOKEN);
            }
        }

        return userId;
    }
}