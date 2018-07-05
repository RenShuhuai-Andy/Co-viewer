# Co-viewer

2018.6-2018.7软件学院暑期工程实训  
第七组：任抒怀、周振宇、庾聪晖、范志康、文泽宇、齐晓阳  
项目：一起看电影（影伴）  
  
## Co-viewer项目启动方法：  
参考项目movie-boot地址：https://github.com/wsk1103/movie-boot    
**1. win系统安装Java 1.8 ， IDEA软件，MySQL数据库，redis，Nginx，Tomcat**（redis、Nginx暂时不知道怎么用）  
最新版Navicat Premium12 中文破解版 安装激活：https://blog.csdn.net/qq_40529395/article/details/78839357  
在IDEA中加入lombook插件，解决注释红线报错问题：https://blog.csdn.net/seapeak007/article/details/72911529  
去除.html thymeleaf模板变量取值时显示的红线：https://bbs.csdn.net/topics/392302108?page=1   
**2. 将项目导入到IDEA中，构建为MAVEN项目**  
IDEA导入maven项目：https://jingyan.baidu.com/article/b0b63dbf0c0ac04a49307078.html  
IDEA部署maven工程到Tomcat：https://blog.csdn.net/li_steve/article/details/72812084    
**3. 打开MySQL，执行sql文件，将数据导入到MySQL中**  
下载MySQL：https://blog.csdn.net/clouderpig/article/details/79556149  
下载Navicat Premium 12：https://blog.csdn.net/qq_40529395/article/details/78839357  
如何通过navicat创建数据库和导入数据库：https://jingyan.baidu.com/article/219f4bf7cba39ade442d38f4.html  
建议新建的数据库名为coviewer，然后在Navicat中运行项目/sql/中的.spl文件  
运行后在Idea连接数据库（MySql Workbench）：https://blog.csdn.net/joker8023joker/article/details/73065378，建议连接名设定为coviewer@localhost  
**4. 建立IDEA与MySQL的链接**  
修改/src/main/java/com.rsh.coviewer/application.properties中的配置信息：第4、5、9、10行改为自己MySQL数据库的用户名和密码；将第7行3036/后的movie改为自己的数据库名(建议为coviewer)，最后再加上&useSSL=true：spring.datasource.url=jdbc:mysql://localhost:3306/coviewer?useUnicode=true&characterEncoding=utf8&useSSL=true  
完成Tomcat数据库连接池的配置：https://blog.csdn.net/qq_24421591/article/details/51055390  
**5. 修改项目中/src/main/java/com.rsh.coviewer/email/send中的用户账号和密码信息**（不知为何没有效果）  
**6. 运行**\src\main\java\com\rsh\coviewer\MovieApplication.java  
**7. 访问**http://localhost:8080/ 账号：123456；密码qq12345  

---    
    
## 电影类框架：  
#### movie包：手动从bean中分出  
* celebrity:json电影人条目信息  
  * Celebrity:电影人
  * Subjects:电影
  * USbox:北美票房
  * Work:？？？
* maoyan:json猫眼条目信息
  * cinema:单个影院信息  
    *...
  * cinemas:多个影院信息  
    *...
  * movie:电影信息（？？？）  
    *...
#### bean包:回显的实体类  
* Acts:演员
* Casts:演员表
* CriticComment:影评
* Directors:导演
* MovieBean:电影
* OneSubject:豆瓣-单个电影具体信息  
   
 ---
 
 #### pojo包:电影相关的数据库实体--service包:电影相关的服务操作--resources下的mapping包:mybatis相关的xml文件  
 service下定义了9个服务接口，lmpl文件夹下是服务接口的实现类  
 1. CollectionCritic:被收藏的影评--CollectionCriticService
 2. CommentCritic:影评--CommentCriticSercive
 3. GoodCritic:被点赞的影评--GoodCriticService
 4. Message:消息--MessageService
 5. MovieName:电影名--MovieNameService
 6. MyFriends:我的好友--MyFriendsService
 7. PublishCritic:发布的评论--PublishCriticService
 8. UserInformation:用户信息--UserInformationService
 9. UserPassword:用户密码--UserPasswordService
  
存在于pojo、mapping但不存在于service中的5个类（似乎未在网页中实现）：
1. AdminAction:管理员行为
2. AdminInformation:管理员信息
3. MovieForum:电影论坛
4. ForumContent:论坛内容
5. UserHeadPicture:用户头像
 
---

#### controller包：链接控制--resources下的templates包
* CriticController:影评的链接控制，包括发表、查找、收藏、点赞--criticInformation.html
* MovieController:电影的链接控制，包括查看电影、影院、电影人信息--movieInformation.html

## 使用到的API
* 豆瓣图书：https://developers.douban.com/wiki/?title=book_v2
* 豆瓣电影：https://developers.douban.com/wiki/?title=movie_v2
* 猫眼电影："http://m.maoyan.com/movie/" + id + ".json"

## 疑问
* /resources/templates/book/search/result.html中为什么使用search_movie_result的结果?
* message功能BUG：关闭右下角弹窗后再打开，时间戳和信息顺序都会改变，时间戳会变得相同
* BUG：网页头部鼠标从音乐、电影、设置直接移开后，下拉菜单不会自动关闭
* movieWishMapper里面的selectByUid和selectByMovieid方法没有写对应的sql语句？
