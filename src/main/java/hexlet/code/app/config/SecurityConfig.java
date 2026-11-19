package hexlet.code.app.config;

import hexlet.code.app.service.CustomUserDetailsService;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private JwtDecoder decoder;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/h2-console", "/h2-console/**").permitAll()
						.dispatcherTypeMatchers(
								DispatcherType.REQUEST, DispatcherType.FORWARD).permitAll()
						.requestMatchers("/", "/**", "/api/login",
								"/index.html", "/assets/**", "/index.html/**",
								"/#/login", "/#/login/**").permitAll()
						.anyRequest().authenticated())
				.headers(headers -> headers
						.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.oauth2ResourceServer(rs ->
						rs.jwt(jwt -> jwt.decoder(decoder)))
				.httpBasic(Customizer.withDefaults());
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
}
