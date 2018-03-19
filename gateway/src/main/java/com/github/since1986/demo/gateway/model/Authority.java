package com.github.since1986.demo.gateway.model;

import java.io.Serializable;

public class Authority implements Serializable {

    private long id;
    private String username;
    private String authority;
    private long userId;

    public Authority(){}

    private Authority(Builder builder) {
        setId(builder.id);
        setUsername(builder.username);
        setAuthority(builder.authority);
        setUserId(builder.userId);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public static final class Builder {
        private long id;
        private String username;
        private String authority;
        private long userId;

        private Builder() {
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withAuthority(String authority) {
            this.authority = authority;
            return this;
        }

        public Builder withUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Authority build() {
            return new Authority(this);
        }
    }
}
