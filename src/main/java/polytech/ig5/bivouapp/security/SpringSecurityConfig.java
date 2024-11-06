package polytech.ig5.bivouapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpHeaders;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> {
                // Allow public access to the User microservice
                auth.requestMatchers("/users/**").permitAll();
                
                // Require authentication (any authenticated user) for other services
                auth.requestMatchers("/reservations/**").authenticated();
                auth.requestMatchers("/bivouacs/**").authenticated();
                auth.requestMatchers("/addresses/**").authenticated();
                
                // Any other requests must be authenticated
                auth.anyRequest().authenticated();
            })
            .formLogin(Customizer.withDefaults())
            .build();
    }

    @Bean
    public UserDetailsService users() {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user"))
                .roles("USER")
                .build();

        UserDetails host = User.builder()
                .username("host")
                .password(passwordEncoder().encode("host"))
                .roles("HOST")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, host, admin);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // @Bean
    // public GlobalFilter customGlobalFilter() {
    //     return (exchange, chain) -> {
    //         String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    //         if (authHeader != null) {
    //             exchange.getRequest().mutate().header(HttpHeaders.AUTHORIZATION, authHeader);
    //         }
    //         return chain.filter(exchange);
    //     };
    // }
}
