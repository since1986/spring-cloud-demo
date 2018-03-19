package com.github.since1986.demo.gateway.model;

import java.io.Serializable;
import java.util.List;

public class RemoteCallEvent implements Serializable {

    private long id;
    private Status status;
    private String remoteServiceSpringBeanName; //远程Service的Spring Bean名
    private String remoteServiceInterfaceName; //远程Service的接口名
    private String remoteServiceMethodName; //远程Service的方法名
    private List<Class> remoteServiceMethodParamTypes; //远程Service的参数类型
    private List<?> remoteServiceMethodParamValues; //远程Service的参数值
    private long timestamp;

    public RemoteCallEvent(){} //Mybatis需要默认构造器，否则会报No constructor found

    private RemoteCallEvent(Builder builder) {
        setId(builder.id);
        setStatus(builder.status);
        setRemoteServiceSpringBeanName(builder.remoteServiceSpringBeanName);
        setRemoteServiceInterfaceName(builder.remoteServiceInterfaceName);
        setRemoteServiceMethodName(builder.remoteServiceMethodName);
        setRemoteServiceMethodParamTypes(builder.remoteServiceMethodParamTypes);
        setRemoteServiceMethodParamValues(builder.remoteServiceMethodParamValues);
        setTimestamp(builder.timestamp);
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getRemoteServiceSpringBeanName() {
        return remoteServiceSpringBeanName;
    }

    public void setRemoteServiceSpringBeanName(String remoteServiceSpringBeanName) {
        this.remoteServiceSpringBeanName = remoteServiceSpringBeanName;
    }

    public String getRemoteServiceInterfaceName() {
        return remoteServiceInterfaceName;
    }

    public void setRemoteServiceInterfaceName(String remoteServiceInterfaceName) {
        this.remoteServiceInterfaceName = remoteServiceInterfaceName;
    }

    public String getRemoteServiceMethodName() {
        return remoteServiceMethodName;
    }

    public void setRemoteServiceMethodName(String remoteServiceMethodName) {
        this.remoteServiceMethodName = remoteServiceMethodName;
    }

    public List<Class> getRemoteServiceMethodParamTypes() {
        return remoteServiceMethodParamTypes;
    }

    public void setRemoteServiceMethodParamTypes(List<Class> remoteServiceMethodParamTypes) {
        this.remoteServiceMethodParamTypes = remoteServiceMethodParamTypes;
    }

    public List<?> getRemoteServiceMethodParamValues() {
        return remoteServiceMethodParamValues;
    }

    public void setRemoteServiceMethodParamValues(List<?> remoteServiceMethodParamValues) {
        this.remoteServiceMethodParamValues = remoteServiceMethodParamValues;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static enum Status {
        CREATED,
        RECEIVED,
        PUBLISHED,
        CONSUMED
    }

    public static final class Builder {
        private long id;
        private Status status;
        private String remoteServiceSpringBeanName;
        private String remoteServiceInterfaceName;
        private String remoteServiceMethodName;
        private List<Class> remoteServiceMethodParamTypes;
        private List<?> remoteServiceMethodParamValues;
        private long timestamp;

        private Builder() {
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withStatus(Status status) {
            this.status = status;
            return this;
        }

        public Builder withRemoteServiceSpringBeanName(String remoteServiceSpringBeanName) {
            this.remoteServiceSpringBeanName = remoteServiceSpringBeanName;
            return this;
        }

        public Builder withRemoteServiceInterfaceName(String remoteServiceInterfaceName) {
            this.remoteServiceInterfaceName = remoteServiceInterfaceName;
            return this;
        }

        public Builder withRemoteServiceMethodName(String remoteServiceMethodName) {
            this.remoteServiceMethodName = remoteServiceMethodName;
            return this;
        }

        public Builder withRemoteServiceMethodParamTypes(List<Class> remoteServiceMethodParamTypes) {
            this.remoteServiceMethodParamTypes = remoteServiceMethodParamTypes;
            return this;
        }

        public Builder withRemoteServiceMethodParamValues(List<?> remoteServiceMethodParamValues) {
            this.remoteServiceMethodParamValues = remoteServiceMethodParamValues;
            return this;
        }

        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public RemoteCallEvent build() {
            return new RemoteCallEvent(this);
        }
    }
}
