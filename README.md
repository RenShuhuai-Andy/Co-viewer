# Co-viewer

2018.6-2018.7软件学院暑期工程实训  
第七组：任抒怀、周振宇、庾聪晖、范志康、文泽宇、齐晓阳  
项目：一起看电影（影伴）  
  
movie-boot项目启动方法：  
movie-boot项目地址：https://github.com/wsk1103/movie-boot    
**1. win系统安装Java 1.8 ， IDEA软件，MySQL数据库，redis，Nginx，Tomcat**（redis、Nginx暂时不知道怎么用）  
最新版Navicat Premium12 中文破解版 安装激活：https://blog.csdn.net/qq_40529395/article/details/78839357  
**2. 将项目导入到IDEA中，构建为MAVEN项目**  
IDEA导入maven项目：https://jingyan.baidu.com/article/b0b63dbf0c0ac04a49307078.html  
IDEA部署maven工程到Tomcat：https://blog.csdn.net/li_steve/article/details/72812084    
**3. 打开MySQL，执行sql文件，将数据导入到MySQL中**  
下载MySQL：https://blog.csdn.net/clouderpig/article/details/79556149  
下载Navicat Premium 12：https://blog.csdn.net/qq_40529395/article/details/78839357  
如何通过navicat创建数据库和导入数据库：https://jingyan.baidu.com/article/219f4bf7cba39ade442d38f4.html  
然后要点击运行  
运行后在Idea连接数据库（MySql Workbench）：https://blog.csdn.net/joker8023joker/article/details/73065378  
**4. 建立IDEA与MySQL的链接**  
修改/src/main/java/com.wsk.movie/application.properties中的配置信息：第4、5、9、10行改为自己MySQL数据库的用户名和密码；在第7行后加上&useSSL=true。  
完成Tomcat数据库连接池的配置：https://blog.csdn.net/qq_24421591/article/details/51055390  
**5. 修改项目中/src/main/java/com.wsk.movie/email/send中的用户账号和密码信息**（不知为何没有效果）  
**6. 运行**\src\main\java\com\wsk\movie\MovieApplication.java  
**7. 访问**http://localhost:8080/
