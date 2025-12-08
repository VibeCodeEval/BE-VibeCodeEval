package com.yd.vibecode.global.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.yd.vibecode.domain.auth.domain.service.RefreshTokenService;
import com.yd.vibecode.domain.auth.domain.service.TokenWhitelistService;
import com.yd.vibecode.global.config.properties.CorsProperties;
import com.yd.vibecode.global.exception.code.BaseCode;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final TokenProvider tokenProvider;
	private final ExcludeAuthPathProperties excludeAuthPathProperties;
	private final RefreshTokenService refreshTokenService;
	private final TokenWhitelistService tokenWhitelistService;
	private final CorsProperties corsProperties;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable);

		http.authorizeHttpRequests(request -> {
			excludeAuthPathProperties.getPaths().iterator()
					.forEachRemaining(authPath ->
							request.requestMatchers(HttpMethod.valueOf(authPath.getMethod()), authPath.getPathPattern()).permitAll()
					);
		});

		http.authorizeHttpRequests(request -> request
				.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
				.requestMatchers(
						"/swagger-ui/**",
						"/v3/api-docs/**",
						"/swagger-ui.html",
						"/webjars/**"
				).permitAll()
				.requestMatchers(
						"/actuator/**",
						"/favicon.ico"
				).permitAll()
				.requestMatchers(
						"/ws/**"  // WebSocket 엔드포인트 (자체 JWT 검증)
				).permitAll()
				.requestMatchers(HttpMethod.POST, "/users/token").authenticated() // 토큰 재발급
				// Authenticated
				.anyRequest().authenticated()
		);

		// Session 해제
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Jwt 커스텀 필터 등록
		http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		// Token Exception Handling
		http.exceptionHandling(except -> except
				.authenticationEntryPoint((request, response, authException) -> writeUnauthorizedResponse(response))
		);

		return http.build();
	}

	@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 HTTP 메서드
        config.setAllowedMethods(Arrays.asList(corsProperties.getAllowedMethods().split(",")));
        // 허용할 헤더
        config.setAllowedHeaders(Arrays.asList(corsProperties.getAllowedHeaders().split(",")));
        config.setAllowedOrigins(Arrays.asList(corsProperties.getAllowedOrigins().split(",")));
        // 인증정보(cookie, Authorization 헤더) 허용 여부
        config.setAllowCredentials(true);
        // pre-flight 캐시 시간 (초)
        config.setMaxAge(corsProperties.getMaxAge());
        // 노출할 응답 헤더
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Total-Count"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 대해 위 정책을 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, excludeAuthPathProperties, refreshTokenService, tokenWhitelistService);
    }
    
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        BaseCode errorCode = GlobalErrorStatus._UNAUTHORIZED.getCode();
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        String jsonResponse = String.format("{\"code\":\"%s\",\"message\":\"%s\"}",
                errorCode.getCode(), errorCode.getMessage());
        response.getWriter().write(jsonResponse);
    }
}