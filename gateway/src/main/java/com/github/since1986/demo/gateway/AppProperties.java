package com.github.since1986.demo.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties("app")
public class AppProperties {

    private final DataSource dataSource = new DataSource();
    private final Cors cors = new Cors();
    private final Security security = new Security();
    private String defaultCharacterEncoding;

    public String getDefaultCharacterEncoding() {
        return defaultCharacterEncoding;
    }

    public void setDefaultCharacterEncoding(String defaultCharacterEncoding) {
        this.defaultCharacterEncoding = defaultCharacterEncoding;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Cors getCors() {
        return cors;
    }

    public Security getSecurity() {
        return security;
    }

    public static class DataSource {

        private String driverClassName;
        private String url;
        private String username;
        private String password;

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Cors {

        private String mapping;
        private List<String> allowedOrigins = new ArrayList<>();
        private List<String> allowedMethods = new ArrayList<>();
        private List<String> allowedHeaders = new ArrayList<>();
        private boolean allowCredentials;

        public String getMapping() {
            return mapping;
        }

        public void setMapping(String mapping) {
            this.mapping = mapping;
        }

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }
    }

    public static class Security {

        private final PublicWeb publicWeb = new PublicWeb();
        private final PrivateWeb privateWeb = new PrivateWeb();
        private final SystemWeb systemWeb = new SystemWeb();
        private String loginNeededMessage;
        private String loginSuccessMessage;
        private String loginFailMessage;
        private String logoutSuccessMessage;
        private String accessDeniedMessage;

        public String getLoginNeededMessage() {
            return loginNeededMessage;
        }

        public void setLoginNeededMessage(String loginNeededMessage) {
            this.loginNeededMessage = loginNeededMessage;
        }

        public String getLoginSuccessMessage() {
            return loginSuccessMessage;
        }

        public void setLoginSuccessMessage(String loginSuccessMessage) {
            this.loginSuccessMessage = loginSuccessMessage;
        }

        public String getLoginFailMessage() {
            return loginFailMessage;
        }

        public void setLoginFailMessage(String loginFailMessage) {
            this.loginFailMessage = loginFailMessage;
        }

        public String getLogoutSuccessMessage() {
            return logoutSuccessMessage;
        }

        public void setLogoutSuccessMessage(String logoutSuccessMessage) {
            this.logoutSuccessMessage = logoutSuccessMessage;
        }

        public String getAccessDeniedMessage() {
            return accessDeniedMessage;
        }

        public void setAccessDeniedMessage(String accessDeniedMessage) {
            this.accessDeniedMessage = accessDeniedMessage;
        }

        public PublicWeb getPublicWeb() {
            return publicWeb;
        }

        public PrivateWeb getPrivateWeb() {
            return privateWeb;
        }

        public SystemWeb getSystemWeb() {
            return systemWeb;
        }

        public static class PublicWeb {

            private String antMatcher;

            public String getAntMatcher() {
                return antMatcher;
            }

            public void setAntMatcher(String antMatcher) {
                this.antMatcher = antMatcher;
            }
        }

        public static class PrivateWeb {

            private String antMatcher;
            private String loginProcessingUrl;
            private String logoutUrl;

            public String getAntMatcher() {
                return antMatcher;
            }

            public void setAntMatcher(String antMatcher) {
                this.antMatcher = antMatcher;
            }

            public String getLoginProcessingUrl() {
                return loginProcessingUrl;
            }

            public void setLoginProcessingUrl(String loginProcessingUrl) {
                this.loginProcessingUrl = loginProcessingUrl;
            }

            public String getLogoutUrl() {
                return logoutUrl;
            }

            public void setLogoutUrl(String logoutUrl) {
                this.logoutUrl = logoutUrl;
            }
        }

        public static class SystemWeb {

            private String antMatcher;
            private String systemUsername;
            private String systemPassword;
            private String developerUsername;
            private String developerPassword;

            public String getAntMatcher() {
                return antMatcher;
            }

            public void setAntMatcher(String antMatcher) {
                this.antMatcher = antMatcher;
            }

            public String getSystemUsername() {
                return systemUsername;
            }

            public void setSystemUsername(String systemUsername) {
                this.systemUsername = systemUsername;
            }

            public String getSystemPassword() {
                return systemPassword;
            }

            public void setSystemPassword(String systemPassword) {
                this.systemPassword = systemPassword;
            }

            public String getDeveloperUsername() {
                return developerUsername;
            }

            public void setDeveloperUsername(String developerUsername) {
                this.developerUsername = developerUsername;
            }

            public String getDeveloperPassword() {
                return developerPassword;
            }

            public void setDeveloperPassword(String developerPassword) {
                this.developerPassword = developerPassword;
            }
        }
    }
}
