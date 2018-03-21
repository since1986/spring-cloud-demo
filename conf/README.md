# 这里是一些外部配置文件
## hosts
```
# 注意测试时不要用配置有代理的浏览器
127.0.0.1	eureka1
127.0.0.1	eureka2
127.0.0.1	eureka3

127.0.0.1	gateway1
127.0.0.1	gateway2
127.0.0.1	gateway3

127.0.0.1	profile1
127.0.0.1	profile2
127.0.0.1	profile3

127.0.0.1	demo.since1986.com
```

## NGINX
```
#网关之上再用NGINX做负载均衡
#此配置文件放到NGINX配置目录下的servers目录中
#另外需要修改hosts文件: 127.0.0.1	demo.since1986.com
upstream demo.since1986.com {
    server gateway1:9001;
    server gateway2:9002;
    server gateway3:9003;
}

server{
    listen 8081;
    server_name demo.since1986.com;
    location / {
        proxy_pass         http://demo.since1986.com;
        proxy_set_header   Host             $host;
        proxy_set_header   X-Real-IP        $remote_addr;
        proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
    }
}
```
