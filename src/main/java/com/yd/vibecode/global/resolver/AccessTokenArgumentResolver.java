package com.yd.vibecode.global.resolver;

import static com.yd.vibecode.global.exception.code.status.AuthErrorStatus.INVALID_ACCESS_TOKEN;
import static com.yd.vibecode.global.exception.code.status.GlobalErrorStatus._UNAUTHORIZED;

import com.yd.vibecode.global.annotation.AccessToken;
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
public class AccessTokenArgumentResolver implements HandlerMethodArgumentResolver {

    private final TokenProvider tokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(AccessToken.class) != null
                && String.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        if (request == null) {
            throw new RestApiException(_UNAUTHORIZED);
        }

        String token = tokenProvider.getToken(request)
                .orElseThrow(() -> new RestApiException(_UNAUTHORIZED));

        if (!tokenProvider.isAccessToken(token)) {
            throw new RestApiException(INVALID_ACCESS_TOKEN);
        }

        return token;
    }
}


