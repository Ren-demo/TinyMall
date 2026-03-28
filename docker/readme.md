# 本项目提供的docker部署方案

* 运行docker compose文件部署容器

  ~~~bash
  docker compose up -d
  ~~~

* 在mysql容器中初始化数据库mall

  ~~~bash
  docker exec -i mysql mysql -uroot -p123456 -e "CREATE DATABASE mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
  
  docker exec -i mysql mysql -uroot -p123456 mall < mall.sql
  ~~~

* 登录http://your-IP:9001

  输入账号admin、密码12345678，在minio控制台中创建桶tinymall-bucket，并将桶的读写权限（Access Policy）改为public

* 向rabbitmq容器中添加并激活延迟插件

  ~~~bash
  docker cp ./rabbitmq_delayed_message_exchange-3.8.17.8f537ac.ez rabbitmq:/plugins/
  
  docker exec -it rabbitmq rabbitmq-plugins enable rabbitmq_delayed_message_exchange
  ~~~

