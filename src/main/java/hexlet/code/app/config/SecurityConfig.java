package hexlet.code.app.config;

import hexlet.code.app.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CustomUserDetailsService userDetailsService;
	private final PasswordEncoder encoder;
	private final JwtDecoder decoder;
	private final String allowedOrigins;

	public SecurityConfig(
			CustomUserDetailsService userDetailsService,
			PasswordEncoder encoder,
			JwtDecoder decoder,
			@Value("${app.cors.allowed-origins:}") String allowedOrigins
	) {
		this.userDetailsService = userDetailsService;
		this.encoder = encoder;
		this.decoder = decoder;
		this.allowedOrigins = allowedOrigins;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/swagger-ui.html",
								"/swagger-ui/**",
								"/v3/api-docs/**",
								"/swagger-resources/**",
								"/webjars/**"
						).permitAll()
						.requestMatchers("/h2-console", "/h2-console/**").permitAll()
						.requestMatchers("/h2-console", "/h2-console/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/users").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/task_statuses/**").permitAll()
						// Если всё-таки удалять можно не всем,
						// то очепятка в 8-м шаге, и надо будет сообщить
//						.requestMatchers(HttpMethod.DELETE, "/api/tasks/**").permitAll()
						.requestMatchers("/", "/index.html",
								"/assets/**", "/favicon.ico",
								"/favicon.ico/**").permitAll()
						.anyRequest().authenticated())
				.headers(headers -> headers
						.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.oauth2ResourceServer(rs ->
						rs.jwt(jwt -> jwt.decoder(decoder)))
				.cors(cors -> cors.configurationSource(corsConfigurationSource()));
//				.httpBasic(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	public AuthenticationProvider daoAuthProvider() {
		var provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(encoder);
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
		return config.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		List<String> origins = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isEmpty())
				.toList();

		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(origins);
		config.setAllowedMethods(
				List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
		);
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setExposedHeaders(List.of("X-Total-Count"));
		config.setAllowCredentials(false);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
