# 这里是一些外部配置文件
## hosts
```
# 注意测试时不要用配置有代理的浏览器
127.0.0.1	eureka1
127.0.0.1	eureka2
127.0.0.1	eureka3
```

## NGINX
```
#网关之上再用Nginx做负载均衡
#配置文件放到 Nginx配置目录/servers/ 中
upstream localhost {
    server localhost:9001;
    server localhost:9002;
    server localhost:9003;
}

#暴露给前端localhost:8081
server{
    listen 8081;
    server_name localhost;
    location / {
        proxy_pass         http://localhost;
        proxy_set_header   Host             $host;
        proxy_set_header   X-Real-IP        $remote_addr;
        proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
    }
}
```
