package likelion.madi.common.security;

import java.util.Arrays;

import likelion.madi.common.oauth2.CustomOAuth2UserService;
import likelion.madi.common.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import likelion.madi.common.oauth2.OAuth2AuthenticationFailureHandler;
import likelion.madi.common.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final FilterExceptionHandler filterExceptionHandler;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter;
    private final HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList(
                            "http://localhost:3000",
                            "http://43.200.244.214:8080",
                            "http://43.200.244.214:8080/login/oauth2/code/kakao",
                            "http://43.200.244.214:8080/login/oauth2/code/google",
                            "http://localhost:5173",
                            "http://localhost:5174"
                    ));
                    config.setAllowedMethods(Arrays.asList(
                            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
                    ));
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(Arrays.asList(
                            "Authorization",
                            "X-Refresh-Token",
                            "Content-Type",
                            "X-Requested-With",
                            "Accept",
                            "Origin"
                    ));
                    config.setMaxAge(3600L);
                    config.addExposedHeader("Authorization");
                    config.addExposedHeader("X-Refresh-Token");
                    return config;
                }))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/swagger-resources/**",
                                "/api/swagger-ui/**",
                                "/api/swagger-ui.html",
                                "/api/v3/api-docs/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/prometheus",
                                "/api/webjars/**",
                                "/webjars/**",
                                "/uploads/**",
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**",
                                "/error"
                        ).permitAll()   // Swagger, Spring Actuator, OAuth2 Debug 허가
                        .requestMatchers(
                                "/api/v1/member/**",
                                "/api/v1/vendor/**",
                                "/api/v1/environment/**"
                        ).permitAll()   // Member 관련 허가
                        .anyRequest().authenticated()
                )
                .oauth2Login(   // OAuth2
                        oauth2Login -> oauth2Login
                                .authorizationEndpoint(auth ->
                                        auth.baseUri("/oauth2/authorization")
                                                .authorizationRequestRepository(authorizationRequestRepository))
                                .successHandler(oAuth2AuthenticationSuccessHandler)
                                .failureHandler(oAuth2AuthenticationFailureHandler)
                                .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                        .userService(customOAuth2UserService)))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(filterExceptionHandler)   // 인증 실패 예외 핸들링
                        .accessDeniedHandler(filterExceptionHandler)        // 인가 실패 예외 핸들링
                );

        http.addFilterBefore(jwtAuthenticationProcessingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
