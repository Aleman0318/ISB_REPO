package com.sistemascontables.ISuiteBalance.Config;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.sistemascontables.ISuiteBalance.Services.UsuarioDetailsService;
import com.sistemascontables.ISuiteBalance.Services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // ✅ usa el bean de Application

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // ===== Público (invitado) =====
                        .requestMatchers(
                                "/",
                                "/acerca",          // ✅ Invitado puede ver
                                "/login",
                                "/register",
                                "/api/me",          // ✅ lo usa tu JS para hidratar
                                "/fonts/**",
                                "/css/**",
                                "/js/**",
                                "/JS/**",           // ✅ por si sirves /JS en mayúsculas
                                "/img/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ===== Por rol (usa hasRole/hasAnyRole porque tus authorities son ROLE_<Rol>) =====
                        // Administrador
                        .requestMatchers("/gestion-usuario/**").hasRole("Administrador")

                        // Administrador o Contador
                        .requestMatchers(
                                "/registro-libro-diario/**",
                                "/libro-mayor/**",
                                "/partidas/**",
                                "/subir-doc/**",
                                "/gestion-partida/**"
                        ).hasAnyRole("Administrador","Contador")

                        // Administrador o Auditor
                        .requestMatchers(
                                "/bitacora/**",
                                "/reportes/**",
                                "/generar-reporte/**"
                        ).hasAnyRole("Administrador","Auditor")

                        // Dashboard: requiere login
                        .requestMatchers("/dashboard/**").authenticated()

                        // Cualquier otra ruta, autenticada
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .failureUrl("/login?error")
                        .successHandler((request, response, authentication) -> {
                            // Guarda loginTime
                            request.getSession().setAttribute("loginTime", System.currentTimeMillis());

                            // Guarda userId en sesión (opcional)
                            try {
                                Object p = authentication.getPrincipal();
                                Long userId = null;

                                if (p instanceof com.sistemascontables.ISuiteBalance.Models.Usuario u) {
                                    userId = u.getId_usuario();
                                } else if (p instanceof UserDetails ud) {
                                    usuarioService.findByCorreo(ud.getUsername())
                                            .ifPresent(u -> request.getSession().setAttribute("userId", u.getId_usuario()));
                                }

                                if (userId != null) {
                                    request.getSession().setAttribute("userId", userId);
                                }
                            } catch (Exception ignored) {}

                            // Redirige manualmente
                            response.sendRedirect("/dashboard");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        // Atrapamos POST /logout explícitamente
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                // ✅ 403 personalizado para rol inadecuado
                .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));

        // CSRF queda habilitado por defecto (tu formulario ya manda el token)
        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(usuarioDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }
}
