package likelion.madi.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;


@Configuration
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().replaceWithSchema(LocalTime.class, new StringSchema().example("14:30:00"));
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Authorization", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("JWT 액세스 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다."))
                        .addSecuritySchemes("X-Refresh-Token", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Refresh-Token")
                                .description("리프레시 토큰을 입력하세요. 형식: Bearer <token>")))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .info(new Info()
                        .title("HUFS Lion SSO Session API")
                        .description("소셜 로그인 및 JWT 기반 인증 API")
                        .version("1.0.0"));
    }
}
