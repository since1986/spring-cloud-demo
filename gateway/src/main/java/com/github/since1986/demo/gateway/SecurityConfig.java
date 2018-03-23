package com.github.since1986.demo.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Order(-1)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Configuration
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
                .antMatchers("/favicon.ico", "/webjars/**", "/static/**", "/private/register/**");
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
    @Order(-2)
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
    @Order(-3)
    //注意Order 用 SecurityProperties.ACCESS_OVERRIDE_ORDER -998 导致csrf配置不生效（原因：WebSecurityConfigurerAdapter 的 Order 是 100 比SecurityProperties.ACCESS_OVERRIDE_ORDER -998 小很多 https://stackoverflow.com/questions/45529743/ordersecurityproperties-access-override-order-vs-managementserverproperties-a）
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
        public UserDetailsService userDetailsService() {
            JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
            jdbcDao.setEnableGroups(true);
            jdbcDao.setDataSource(dataSource);
            return jdbcDao;
        }

        private MacSigner jwtSigner() {
            return new MacSigner(appProperties.getSecurity().getPrivateWeb().getJwtSignerKey());
        }

        @Override
        protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
            authenticationManagerBuilder
                    .userDetailsService(userDetailsService())
                    .passwordEncoder(passwordEncoder);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .securityContext()
                    .securityContextRepository(new SecurityContextRepository() { //提供给 SecurityContextPersistenceFilter 来调用（SecurityContextPersistenceFilter 两个主要职责：请求来临时，创建 SecurityContext 安全上下文信息，请求结束时清空 SecurityContextHolder https://www.cnkirito.moe/2017/09/30/spring-security-4/）

                        @Override
                        public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
                            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                            HttpServletRequest request = requestResponseHolder.getRequest();
                            String jwtHeader = request.getHeader("Authorization");
                            if (StringUtils.isNoneEmpty(jwtHeader) && StringUtils.startsWith(jwtHeader, "Bearer ")) {
                                Jwt jwt = JwtHelper.decodeAndVerify(StringUtils.removeStart(jwtHeader, "Bearer "), jwtSigner());
                                JwtPayload jwtPayload;
                                try {
                                    jwtPayload = objectMapper.readValue(jwt.getClaims(), JwtPayload.class);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                List<GrantedAuthority> authorities = jwtPayload.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                                securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(jwtPayload.getUsername(), "*PROTECTED*", authorities));
                            }
                            return securityContext;
                        }

                        @Override
                        public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
                            //JWT 是不需要存储 SecurityContext 的
                        }

                        @Override
                        public boolean containsContext(HttpServletRequest request) {
                            //TODO JWT 过期检查
                            String jwtHeader = request.getHeader("Authorization");
                            return StringUtils.isNoneEmpty(jwtHeader) && StringUtils.startsWith(jwtHeader, "Bearer ") && StringUtils.isNoneBlank(jwtHeader.replaceFirst("Bearer ", ""));
                        }
                    });
            http
                    .antMatcher(appProperties.getSecurity().getPrivateWeb().getAntMatcher())
                    .httpBasic().disable()
                    .csrf().requireCsrfProtectionMatcher(new RequestHeaderRequestMatcher("X-NEED-CSRF-PROTECTION", "true"))
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .cors()
                    .and()

                    .exceptionHandling()
                    .authenticationEntryPoint(new Http401AuthenticationEntryPoint("JWT"))
                    .and()
                    .formLogin().loginProcessingUrl(appProperties.getSecurity().getPrivateWeb().getLoginProcessingUrl()).permitAll()
                    .successHandler((request, response, authentication) -> { //认证成功
                        //认证成功后将Jwt放入response的header中
                        Jwt jwt = JwtHelper.encode(
                                objectMapper.writeValueAsString(
                                        JwtPayload
                                                .newBuilder()
                                                .withUsername(authentication.getPrincipal().toString())
                                                .withAuthorities(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                                                .build()
                                ),
                                jwtSigner()
                        );
                        response.addHeader("WWW-Authenticate", String.format("Bearer %s", jwt.getEncoded()));
                    })
                    .failureHandler((request, response, exception) -> { //认证失败
                        response.setHeader("WWW-Authenticate", "JWT");
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
                    })
                    .and()
                    .logout()
                    .logoutUrl(appProperties.getSecurity().getPrivateWeb().getLogoutUrl())
                    .logoutSuccessHandler((request, response, authentication) -> {
                        response.addHeader("WWW-Authenticate", "JWT"); //退出登录后清除掉Jwt header中的Bearer信息
                    }).permitAll()

                    .and()
                    .authorizeRequests()
                    .expressionHandler(webSecurityExpressionHandler())
                    .antMatchers(HttpMethod.OPTIONS).permitAll()
                    .anyRequest().access("hasRole('ROLE_USER')")
                    .and()
                    .exceptionHandling()
                    .accessDeniedHandler((request, response, accessDeniedException) -> { //AccessDeniedHandler只对已经通过认证的用户（也就是已经登陆成功）的用户生效
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
                    });
        }

        public static class JwtPayload {

            //JWT标准字段 https://tools.ietf.org/html/rfc7519#section-4.1 https://en.wikipedia.org/wiki/JSON_Web_Token#Standard_field
            private String iss = "system";    //Issuer
            private String sub = "authorization";    //Subject
            private String aud;    //Audience
            private long exp = DateUtils.addMonths(new Date(), 6).getTime();    //Expiration
            private long nbf = new Date().getTime();    //Not Before
            private long iat = System.currentTimeMillis();    //Issued At
            private String jti = StringUtils.removeAll(UUID.randomUUID().toString(), "-");    //JWT ID

            //自定义字段
            private String username;
            private String userId;
            private List<String> authorities = new ArrayList<>(); //授权

            //给Jackson用的默认构造器
            public JwtPayload() {
            }

            private JwtPayload(Builder builder) {
                setIss(builder.iss);
                setSub(builder.sub);
                setAud(builder.aud);
                setExp(builder.exp);
                setNbf(builder.nbf);
                setIat(builder.iat);
                setJti(builder.jti);
                setUsername(builder.username);
                setUserId(builder.userId);
                setAuthorities(builder.authorities);
            }

            public static Builder newBuilder() {
                return new Builder();
            }

            public String getIss() {
                return iss;
            }

            public void setIss(String iss) {
                this.iss = iss;
            }

            public String getSub() {
                return sub;
            }

            public void setSub(String sub) {
                this.sub = sub;
            }

            public String getAud() {
                return aud;
            }

            public void setAud(String aud) {
                this.aud = aud;
            }

            public long getExp() {
                return exp;
            }

            public void setExp(long exp) {
                this.exp = exp;
            }

            public long getNbf() {
                return nbf;
            }

            public void setNbf(long nbf) {
                this.nbf = nbf;
            }

            public long getIat() {
                return iat;
            }

            public void setIat(long iat) {
                this.iat = iat;
            }

            public String getJti() {
                return jti;
            }

            public void setJti(String jti) {
                this.jti = jti;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public List<String> getAuthorities() {
                return authorities;
            }

            public void setAuthorities(List<String> authorities) {
                this.authorities = authorities;
            }

            public static final class Builder {
                private String iss;
                private String sub;
                private String aud;
                private long exp;
                private long nbf;
                private long iat;
                private String jti;
                private String username;
                private String userId;
                private List<String> authorities;

                private Builder() {
                }

                public Builder withIss(String iss) {
                    this.iss = iss;
                    return this;
                }

                public Builder withSub(String sub) {
                    this.sub = sub;
                    return this;
                }

                public Builder withAud(String aud) {
                    this.aud = aud;
                    return this;
                }

                public Builder withExp(long exp) {
                    this.exp = exp;
                    return this;
                }

                public Builder withNbf(long nbf) {
                    this.nbf = nbf;
                    return this;
                }

                public Builder withIat(long iat) {
                    this.iat = iat;
                    return this;
                }

                public Builder withJti(String jti) {
                    this.jti = jti;
                    return this;
                }

                public Builder withUsername(String username) {
                    this.username = username;
                    return this;
                }

                public Builder withUserId(String userId) {
                    this.userId = userId;
                    return this;
                }

                public Builder withAuthorities(List<String> authorities) {
                    this.authorities = authorities;
                    return this;
                }

                public JwtPayload build() {
                    return new JwtPayload(this);
                }
            }
        }
    }

    //对于/system/**的URL启用不同的安全配置
    @Order(-4)
    @Configuration
    public static class SystemWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        private final AppProperties appProperties;
        private final PasswordEncoder passwordEncoder;
        private final ObjectMapper objectMapper;

        @Autowired
        public SystemWebSecurityConfigurerAdapter(PasswordEncoder passwordEncoder, AppProperties appProperties, ObjectMapper objectMapper) {
            this.passwordEncoder = passwordEncoder;
            this.appProperties = appProperties;
            this.objectMapper = objectMapper;
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
                    .csrf().disable()
                    .cors()

                    .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(new Http401AuthenticationEntryPoint("JWT"))
                    .and()
                    .formLogin().loginProcessingUrl(appProperties.getSecurity().getSystemWeb().getLoginProcessingUrl()).permitAll()
                    .successHandler((request, response, authentication) -> { //认证成功
                        objectMapper.writeValue(response.getWriter(), authentication.getPrincipal());
                    })
                    .failureHandler((request, response, exception) -> { //认证失败
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
                    })
                    .and()
                    .logout()
                    .logoutUrl(appProperties.getSecurity().getSystemWeb().getLogoutUrl())
                    .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()).permitAll()
                    .and()
                    .authorizeRequests()
                    .expressionHandler(webSecurityExpressionHandler())
                    .antMatchers(HttpMethod.OPTIONS).permitAll()
                    .anyRequest().access("hasRole('ROLE_SYSTEM')")
                    .and()
                    .exceptionHandling()
                    .accessDeniedHandler((request, response, accessDeniedException) -> { //AccessDeniedHandler只对已经通过认证的用户（也就是已经登陆成功）的用户生效
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
                    });
        }
    }
}
