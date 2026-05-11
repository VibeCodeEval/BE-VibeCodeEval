package com.yd.vibecode.global.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;
import com.yd.vibecode.global.security.TokenProvider;

@ExtendWith(MockitoExtension.class)
class CurrentUserArgumentResolverTest {

    @Mock
    private TokenProvider tokenProvider;

    private CurrentUserArgumentResolver resolver;

    private MockHttpServletRequest mockRequest;
    private ServletWebRequest webRequest;

    @BeforeEach
    void setUp() {
        resolver = new CurrentUserArgumentResolver(tokenProvider);
        mockRequest = new MockHttpServletRequest();
        webRequest = new ServletWebRequest(mockRequest);
    }

    /**
     * 테스트 전용 메서드 시그니처 모음. 리플렉션으로 MethodParameter를 만들어
     * supportsParameter / resolveArgument 흐름을 검증한다.
     */
    @SuppressWarnings("unused")
    static class SampleHandlers {
        public void stringParam(@CurrentUser String userId) {}
        public void longBoxedParam(@CurrentUser Long userId) {}
        public void longPrimitiveParam(@CurrentUser long userId) {}
        public void integerParam(@CurrentUser Integer userId) {}
        public void noAnnotationStringParam(String userId) {}
    }

    private MethodParameter parameterOf(String methodName) throws NoSuchMethodException {
        for (Method method : SampleHandlers.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return new MethodParameter(method, 0);
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    @Nested
    @DisplayName("supportsParameter")
    class SupportsParameter {

        @Test
        @DisplayName("@CurrentUser String 파라미터는 지원한다")
        void supports_string_parameter() throws Exception {
            MethodParameter parameter = parameterOf("stringParam");

            assertThat(resolver.supportsParameter(parameter)).isTrue();
        }

        @Test
        @DisplayName("@CurrentUser Long 파라미터는 지원한다")
        void supports_long_boxed_parameter() throws Exception {
            MethodParameter parameter = parameterOf("longBoxedParam");

            assertThat(resolver.supportsParameter(parameter)).isTrue();
        }

        @Test
        @DisplayName("@CurrentUser long primitive 파라미터는 지원한다")
        void supports_long_primitive_parameter() throws Exception {
            MethodParameter parameter = parameterOf("longPrimitiveParam");

            assertThat(resolver.supportsParameter(parameter)).isTrue();
        }

        @Test
        @DisplayName("@CurrentUser가 붙은 지원하지 않는 타입(Integer)은 false를 반환한다")
        void does_not_support_unsupported_type() throws Exception {
            MethodParameter parameter = parameterOf("integerParam");

            assertThat(resolver.supportsParameter(parameter)).isFalse();
        }

        @Test
        @DisplayName("@CurrentUser 어노테이션이 없으면 false를 반환한다")
        void does_not_support_when_annotation_missing() throws Exception {
            MethodParameter parameter = parameterOf("noAnnotationStringParam");

            assertThat(resolver.supportsParameter(parameter)).isFalse();
        }
    }

    @Nested
    @DisplayName("resolveArgument")
    class ResolveArgument {

        @Test
        @DisplayName("String 파라미터는 토큰에서 추출한 userId 문자열을 그대로 반환한다")
        void returns_string_user_id_for_string_parameter() throws Exception {
            MethodParameter parameter = parameterOf("stringParam");
            String token = "valid-access-token";
            given(tokenProvider.getToken(mockRequest)).willReturn(Optional.of(token));
            given(tokenProvider.isAccessToken(token)).willReturn(true);
            given(tokenProvider.getId(token)).willReturn(Optional.of("42"));

            Object result = resolver.resolveArgument(parameter, null, webRequest, null);

            assertThat(result).isInstanceOf(String.class).isEqualTo("42");
        }

        @Test
        @DisplayName("Long 파라미터는 userId를 Long으로 파싱해 반환한다")
        void returns_long_for_long_boxed_parameter() throws Exception {
            MethodParameter parameter = parameterOf("longBoxedParam");
            String token = "valid-access-token";
            given(tokenProvider.getToken(mockRequest)).willReturn(Optional.of(token));
            given(tokenProvider.isAccessToken(token)).willReturn(true);
            given(tokenProvider.getId(token)).willReturn(Optional.of("123"));

            Object result = resolver.resolveArgument(parameter, null, webRequest, null);

            assertThat(result).isInstanceOf(Long.class).isEqualTo(123L);
        }

        @Test
        @DisplayName("long primitive 파라미터도 Long 값으로 반환한다 (오토박싱)")
        void returns_long_for_long_primitive_parameter() throws Exception {
            MethodParameter parameter = parameterOf("longPrimitiveParam");
            String token = "valid-access-token";
            given(tokenProvider.getToken(mockRequest)).willReturn(Optional.of(token));
            given(tokenProvider.isAccessToken(token)).willReturn(true);
            given(tokenProvider.getId(token)).willReturn(Optional.of("456"));

            Object result = resolver.resolveArgument(parameter, null, webRequest, null);

            assertThat(result).isInstanceOf(Long.class).isEqualTo(456L);
        }

        @Test
        @DisplayName("Long 파라미터인데 userId가 숫자가 아니면 INVALID_ACCESS_TOKEN 예외를 던진다")
        void throws_invalid_access_token_when_user_id_is_not_numeric() throws Exception {
            MethodParameter parameter = parameterOf("longBoxedParam");
            String token = "valid-access-token";
            given(tokenProvider.getToken(mockRequest)).willReturn(Optional.of(token));
            given(tokenProvider.isAccessToken(token)).willReturn(true);
            given(tokenProvider.getId(token)).willReturn(Optional.of("not-a-number"));

            assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, webRequest, null))
                    .isInstanceOf(RestApiException.class)
                    .extracting(e -> ((RestApiException) e).getErrorCode())
                    .isEqualTo(AuthErrorStatus.INVALID_ACCESS_TOKEN.getCode());
        }

        @Test
        @DisplayName("요청에서 토큰을 찾지 못하면 _UNAUTHORIZED 예외를 던진다")
        void throws_unauthorized_when_token_missing() throws Exception {
            MethodParameter parameter = parameterOf("stringParam");
            given(tokenProvider.getToken(mockRequest)).willReturn(Optional.empty());

            assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, webRequest, null))
                    .isInstanceOf(RestApiException.class)
                    .extracting(e -> ((RestApiException) e).getErrorCode())
                    .isEqualTo(GlobalErrorStatus._UNAUTHORIZED.getCode());
        }

        @Test
        @DisplayName("토큰은 있으나 getId에서 userId를 추출하지 못하면 INVALID_ACCESS_TOKEN 예외를 던진다 (MeUseCase와 일관)")
        void throws_invalid_access_token_when_get_id_returns_empty() throws Exception {
            MethodParameter parameter = parameterOf("stringParam");
            String token = "valid-access-token";
            given(tokenProvider.getToken(mockRequest)).willReturn(Optional.of(token));
            given(tokenProvider.isAccessToken(token)).willReturn(true);
            given(tokenProvider.getId(token)).willReturn(Optional.empty());

            assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, webRequest, null))
                    .isInstanceOf(RestApiException.class)
                    .extracting(e -> ((RestApiException) e).getErrorCode())
                    .isEqualTo(AuthErrorStatus.INVALID_ACCESS_TOKEN.getCode());
        }

        @Test
        @DisplayName("refresh token이 access token 자리로 전달되면 INVALID_ACCESS_TOKEN 예외를 던진다")
        void throws_invalid_access_token_when_refresh_token_is_used() throws Exception {
            MethodParameter parameter = parameterOf("longBoxedParam");
            String refreshToken = "refresh-token-value";
            given(tokenProvider.getToken(mockRequest)).willReturn(Optional.of(refreshToken));
            given(tokenProvider.isAccessToken(refreshToken)).willReturn(false);

            assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, webRequest, null))
                    .isInstanceOf(RestApiException.class)
                    .extracting(e -> ((RestApiException) e).getErrorCode())
                    .isEqualTo(AuthErrorStatus.INVALID_ACCESS_TOKEN.getCode());
        }
    }
}
