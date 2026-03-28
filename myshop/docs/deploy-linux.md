# Linux 部署（Vite dist + 域名 + 80 端口）

## 1. 打包
在本地项目根目录执行：
- npm install
- npm run build

生成 dist/ 目录。

## 2. 上传到服务器
将 dist/ 上传到服务器，例如：
- /var/www/my-shop/dist

## 3. 域名解析
在域名控制台添加 A 记录：
- 主机记录：@  →  服务器公网 IP
- （可选）主机记录：www  →  服务器公网 IP

等待解析生效（通常几分钟到 1 小时）。

## 4. 放行 80 端口
- 云厂商安全组：允许 TCP 80
- 服务器防火墙：允许 TCP 80

## 5. 安装 Nginx
- sudo apt update
- sudo apt install -y nginx

## 6. Nginx 配置（含 SPA 回退）
创建文件：/etc/nginx/sites-available/my-shop

配置内容：
server {
  listen 80;
  server_name 你的域名或公网IP;

  root /var/www/my-shop/dist;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }

  # 若后端同机 8080，可开启反向代理
  # location /tinymall/ {
  #   proxy_pass http://127.0.0.1:8080/;
  # }
}

启用并重载：
- sudo ln -s /etc/nginx/sites-available/my-shop /etc/nginx/sites-enabled/my-shop
- sudo nginx -t
- sudo systemctl reload nginx

## 7. 访问
浏览器访问：http://你的域名

## 8. 常见问题
- 404：检查 root 路径是否为 dist
- 路由刷新 404：确保 try_files 配置了 /index.html
- API 失败：确认后端地址可访问，或启用反向代理
