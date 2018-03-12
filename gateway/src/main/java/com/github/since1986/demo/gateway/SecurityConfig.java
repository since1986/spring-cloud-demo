package com.github.since1986.demo.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER - 998)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AppProperties appProperties;

    @Autowired
    public SecurityConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    private static RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchyImpl = new RoleHierarchyImpl();
        roleHierarchyImpl.setHierarchy("ROLE_DEVELOPER > ROLE_SUPERVISOR and ROLE_SUPERVISOR > ROLE_ADMIN and ROLE_ADMIN > ROLE_USER and ROLE_USER > ROLE_ANONYMOUS"); //两个角色继承之间用空格或and连接
        return roleHierarchyImpl;
    }

    private static SecurityExpressionHandler<FilterInvocation> webSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
        defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchy());
        return defaultWebSecurityExpressionHandler;
    }

    @Override
    public void configure(WebSecurity web) {
        web
                .ignoring()
                .antMatchers("/favicon.ico", "/webjars/**", "/static/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean //设置CORS，注意这个设置与MVC中的CORS是相互独立的
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(appProperties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(appProperties.getCors().getAllowedMethods());
        configuration.setAllowedHeaders(appProperties.getCors().getAllowedHeaders());
        configuration.setAllowCredentials(appProperties.getCors().isAllowCredentials());
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration(appProperties.getCors().getMapping(), configuration);
        return urlBasedCorsConfigurationSource;
    }

    //无需认证与授权的资源 /public/**
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER - 999)
    @Configuration
    public static class PublicWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final AppProperties appProperties;

        @Autowired
        public PublicWebSecurityConfigurationAdapter(AppProperties appProperties) {
            this.appProperties = appProperties;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher(appProperties.getSecurity().getPublicWeb().getAntMatcher())
                    .csrf().disable()
                    .cors()
                    .and()
                    .anonymous()
                    .authorities("ROLE_ANONYMOUS")
                    .and()
                    .authorizeRequests()
                    .expressionHandler(webSecurityExpressionHandler())
                    .anyRequest()
                    .hasAnyAuthority("ROLE_ANONYMOUS");
        }
    }

    //需要认证与授权的资源 /private/**
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER - 1002)
    @Configuration
    public static class PrivateWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final AppProperties appProperties;
        private final DataSource dataSource;
        private final PasswordEncoder passwordEncoder;
        private final ObjectMapper objectMapper;

        @Autowired
        public PrivateWebSecurityConfigurationAdapter(DataSource dataSource, PasswordEncoder passwordEncoder, AppProperties appProperties, ObjectMapper objectMapper) {
            this.dataSource = dataSource;
            this.passwordEncoder = passwordEncoder;
            this.appProperties = appProperties;
            this.objectMapper = objectMapper;
        }

        @Bean
        public UserDetailsService jdbcUserDetailsService() {
            JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
            jdbcDao.setEnableGroups(true);
            jdbcDao.setDataSource(dataSource);
            return jdbcDao;
        }

        private RequestHeaderAuthenticationFilter jwtRequestHeaderAuthenticationFilter() {
            RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter = new RequestHeaderAuthenticationFilter();
            requestHeaderAuthenticationFilter.setPrincipalRequestHeader("Authorization");
            return requestHeaderAuthenticationFilter;
        }

        private SignatureVerifier signatureVerifier() {
            return new MacSigner("123456");
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
            authenticationManagerBuilder
                    .userDetailsService(jdbcUserDetailsService()).passwordEncoder(passwordEncoder);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher(appProperties.getSecurity().getPrivateWeb().getAntMatcher())
                    .csrf().disable()
                    .cors()
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                    .and()
                    .exceptionHandling()
                    .authenticationEntryPoint((request, response, authException) -> { //认证入口点
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getLocalizedMessage());
                    })
                    .and()
                    .formLogin().loginProcessingUrl(appProperties.getSecurity().getPrivateWeb().getLoginProcessingUrl()).permitAll()
                    .successHandler((request, response, authentication) -> { //认证成功
                        objectMapper.writeValue(response.getWriter(), authentication.getPrincipal());
                    })
                    .failureHandler((request, response, exception) -> { //认证失败
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getLocalizedMessage());
                    })
                    .and()
                    .logout()
                    .logoutUrl(appProperties.getSecurity().getPrivateWeb().getLogoutUrl())
                    .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()).permitAll()

                    .and()
                    .authorizeRequests()
                    .expressionHandler(webSecurityExpressionHandler())
                    .antMatchers(HttpMethod.OPTIONS).permitAll()
                    .anyRequest().access("hasRole('ROLE_USER')")
                    .and()
                    .exceptionHandling()
                    .accessDeniedHandler((request, response, accessDeniedException) -> { //AccessDeniedHandler只对已经通过认证的用户（也就是已经登陆成功）的用户生效
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    });

            http
                    .addFilterBefore(jwtRequestHeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

            http
                    .authenticationProvider(new AuthenticationProvider() {

                        class Payload {

                            private String username;
                            private Date expirationTime = new Date();
                            private List<String> authorities = new ArrayList<>();

                            public String getUsername() {
                                return username;
                            }

                            public void setUsername(String username) {
                                this.username = username;
                            }

                            public Date getExpirationTime() {
                                return expirationTime;
                            }

                            public void setExpirationTime(Date expirationTime) {
                                this.expirationTime = expirationTime;
                            }

                            public List<String> getAuthorities() {
                                return authorities;
                            }

                            public void setAuthorities(List<String> authorities) {
                                this.authorities = authorities;
                            }
                        }

                        class JsonWebToken extends AbstractAuthenticationToken {

                            private User principal;

                            /**
                             * Creates a token with the supplied array of authorities.
                             *
                             * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
                             *                    represented by this authentication object.
                             */
                            public JsonWebToken(Collection<? extends GrantedAuthority> authorities) {
                                super(authorities);
                            }

                            public JsonWebToken(User principal) {
                                super(principal.getAuthorities());
                                this.principal = principal;
                            }

                            @Override
                            public Object getCredentials() {
                                return this.principal.getPassword();
                            }

                            @Override
                            public Object getPrincipal() {
                                return this.principal;
                            }
                        }

                        @Override
                        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                            if (authentication.getClass().isAssignableFrom(PreAuthenticatedAuthenticationToken.class) && authentication.getPrincipal() != null) {
                                String jwtTokenHeader = (String) authentication.getPrincipal();
                                Jwt jwt = JwtHelper.decodeAndVerify(jwtTokenHeader, signatureVerifier());
                                Payload payload = new Payload();
                                try {
                                    payload = objectMapper.readValue(jwt.getClaims(), Payload.class);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                List<GrantedAuthority> authorities = payload.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                                return new JsonWebToken(new User(payload.getUsername(), "[PROTECTED]", authorities));
                            }
                            return authentication;
                        }

                        @Override
                        public boolean supports(Class<?> authentication) {
                            return authentication.isAssignableFrom(PreAuthenticatedAuthenticationToken.class) || authentication.isAssignableFrom(JsonWebToken.class);
                        }
                    });
        }
    }

    //对于/system/**的URL启用不同的安全配置
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER - 1003)
    @Configuration
    public static class SystemWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        private final AppProperties appProperties;
        private final PasswordEncoder passwordEncoder;

        @Autowired
        public SystemWebSecurityConfigurerAdapter(PasswordEncoder passwordEncoder, AppProperties appProperties) {
            this.passwordEncoder = passwordEncoder;
            this.appProperties = appProperties;
        }

        @Bean
        public UserDetailsService inMemoryUserDetailsService() {
            InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();
            inMemoryUserDetailsManager
                    .createUser(
                            User
                                    .withUsername(appProperties.getSecurity().getSystemWeb().getSystemUsername())
                                    .password(appProperties.getSecurity().getSystemWeb().getSystemPassword())
                                    .roles("SYSTEM") //将内置账号的角色与生产系统隔离（ROLE_SYSTEM无法访问任何生产系统的URL，同时生产系统中任何账号的角色都不可能是ROLE_SYSTEM因此生产系统的账号也无法访问内置的系统管理相关的URL）以保证安全性
                                    .build()
                    );

            inMemoryUserDetailsManager
                    .createUser(
                            User.withUsername(appProperties.getSecurity().getSystemWeb().getDeveloperUsername())
                                    .password(appProperties.getSecurity().getSystemWeb().getDeveloperPassword())
                                    .roles("DEVELOPER", "SYSTEM") //内置的开发者账号
                                    .build()
                    );
            return inMemoryUserDetailsManager;
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
            authenticationManagerBuilder
                    .userDetailsService(inMemoryUserDetailsService())
                    .passwordEncoder(passwordEncoder);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher(appProperties.getSecurity().getSystemWeb().getAntMatcher())
                    .authorizeRequests()
                    .expressionHandler(webSecurityExpressionHandler())
                    .anyRequest()
                    .hasRole("SYSTEM")
                    .and()
                    .httpBasic();
        }
    }
}