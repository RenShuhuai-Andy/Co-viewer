package com.rsh.coviewer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.rsh.coviewer.bean.MovieBean;
import com.rsh.coviewer.bean.MyFriendsBean;
import com.rsh.coviewer.bean.AllInformation;
import com.rsh.coviewer.bean.OneSubject;
import com.rsh.coviewer.movie.celebrity.Celebrity;
import com.rsh.coviewer.movie.celebrity.USbox;
import com.rsh.coviewer.movie.maoyan.Hot;
import com.rsh.coviewer.movie.maoyan.movie.MovieInformation;
import com.rsh.coviewer.pojo.MovieWish;
import com.rsh.coviewer.pojo.MyFriends;
import com.rsh.coviewer.pojo.UserInformation;
import com.rsh.coviewer.service.*;
import com.rsh.coviewer.tool.HttpUtils;
import com.rsh.coviewer.tool.POSTtoJSON;
import com.rsh.coviewer.tool.SensitivewordFilter;
import com.rsh.coviewer.tool.Tool;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;
import javax.sound.midi.Soundbank;
import java.util.*;

/**
 * @DESCRIPTION : 电影的链接控制
 * @AUTHOR : rsh
 * @TIME : 2018/7/4
 */
@Controller
public class MovieController {
    private static final String ENCODE = "UTF-8";
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String URL = "http://op.juhe.cn/onebox/movie/video";//影视影讯检索数据接口
    private static final String KEY = "e712295ae7ca460ec31624dd3dfe2094";
    private static final String DOUBAN_URL = "https://api.douban.com";

    @Resource
    private UserInformationService userInformationService;
    @Resource
    private PublishCriticService publishCriticService;
    @Resource
    private MyFriendsService myFriendsService;
    @Resource
    private CommentCriticService commentCriticService;
    @Resource
    private CollectionCriticService collectionCriticService;
    @Resource
    private GoodCriticService goodCriticService;
    @Resource
    private MovieWishService movieWishService;

    //模糊查询电影信息
    @RequestMapping(value = "/search/movie/result")
    public String searchMovieResult(Model model, HttpServletRequest request, @ModelAttribute("name") String q) {
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        if (Tool.getInstance().isNullOrEmpty(q)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/search";//豆瓣电影条目搜索api
        Map<String, String> map = new HashMap<>();
        map.put("q", q);
        AllInformation information = getMovieInformation(url, map, ENCODE, GET);
        model.addAttribute("action", 3);
        model.addAttribute("movie", information);
        model.addAttribute("movie_name", q + "搜索结果");
        getUserCounts(model, userInformation.getId());
        getFriend(model, userInformation.getId());
        return "/movie/SearchMovieResult";
    }

    //查看电影信息
    @RequestMapping(value = "/search/movie/information")
    public String searchMovie(Model model, HttpServletRequest request, @RequestParam String id) {
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";//重定向到/login
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/subject/" + id;//豆瓣电影条目信息
        System.out.println(url);
        Map map = new HashMap();
        String name = "";
        try {
            String result = HttpUtils.submitPostData(url, map, ENCODE, GET);
            System.out.println(result);
            OneSubject subject = JSON.parseObject(result, OneSubject.class);
            String down = "ftp://www.wsk1103.cc:8088/down/" + id + "/" + subject.getTitle() + ".mkv";
            model.addAttribute("down", down);
            model.addAttribute("subject", subject);
            name = subject.getTitle();
        } catch (JSONException e) {
            e.printStackTrace();
            model.addAttribute("result", "error");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("result", "error");
        }
        MovieBean movieBean;
        Map<String, String> params = new HashMap<>();
        params.put("key", KEY);
        params.put("q", name);
        params.put("dtype", "json");
        try {
//          String sr = HttpUtils.submitPostData(URL, params, ENCODE, POST);
            String sr = POSTtoJSON.getInstance().post(URL, params, POST);
            movieBean = JSON.parseObject(sr, MovieBean.class);
        } catch (JSONException e) {
            e.printStackTrace();
            movieBean = new MovieBean();
        } catch (Exception e) {
            e.printStackTrace();
            movieBean = new MovieBean();
        }
        model.addAttribute("movie", movieBean);
        model.addAttribute("result", "success");
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        getFriend(model, userInformation.getId());
        return "information/movieInformation";
    }

    //查看影人条目信息
    @RequestMapping(value = "/celebrity/{id}")
    public String celebrity(@PathVariable String id, Model model, HttpServletRequest request) {
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/celebrity/" + id;
        System.out.println(url);
        Map<String, String> params = new HashMap<>();
        String result = HttpUtils.submitPostData(url, params, ENCODE, GET);
        System.out.println(result);
        model.addAttribute("result", "success");
        Celebrity celebrity;
        try {
            celebrity = JSON.parseObject(result, Celebrity.class);
        } catch (JSONException e) {
            e.printStackTrace();
            celebrity = new Celebrity();
            model.addAttribute("result", "error");
        } catch (Exception e) {
            e.printStackTrace();
            celebrity = new Celebrity();
            model.addAttribute("result", "error");
        }
        model.addAttribute("celebrity", celebrity);
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        getFriend(model, userInformation.getId());
        return "/movie/celebrity";
    }

    //添加到想看
    @RequestMapping(value = "/add/movie/wish")
    public String addMovieWish(Model model,HttpServletRequest request) {
//        System.out.println("add/movie/wish:ok");
        String movieid = request.getParameter("id");
        Integer new_movie_id = Integer.valueOf(movieid);
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
//        System.out.println("userinfo:"+userInformation.toString());
        Map<String, String> map = new HashMap<>();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            map.put("result", "0");
            //return map;
            return null;
        }
        MovieWish movieWish = new MovieWish();
        movieWish.setUid((Integer) request.getSession().getAttribute("uid"));
        movieWish.setAllow((short) 1);
        movieWish.setModified(new Date());
        movieWish.setTime(new Date());
        movieWish.setMovieid(new_movie_id);

        String url = "http://m.maoyan.com/movie/" + movieid + ".json";
//        String result = HttpUtils.maoyan(url);
        String result = "{\"control\":{\"expires\":3600},\"status\":0,\"data\":{\"MovieDetailModel\":{\"cat\":\"剧情,喜剧\",\"dealsum\":0,\"dir\":\"文牧野 \",\"dra\":\"\n" +
                "一位不速之客的意外到访，打破了神油店老板程勇（徐峥 饰）的平凡人生，他从一个交不起房租的男性保健品商贩，一跃成为印度仿制药“格列宁”的独家代理商。收获巨额利润的他，生活剧烈变化，被病患们冠以“药神”的称号。但是，一场关于救赎的拉锯战也在波涛暗涌中慢慢展开......\n" +
                "\n" +
                "\",\"dur\":117,\"id\":1200486,\"imax\":true,\"img\":\"http://p0.meituan.net/165.220/movie/238e2dc36beae55a71cabfc14069fe78236351.jpg\",\"isShowing\":true,\"late\":false,\"mk\":0,\"nm\":\"我不是药神\",\"photos\":[\"http://p1.meituan.net/w.h/movie/36d1a14aca0666c9ca6ed1bd3031c7f1566914.jpg\",\"http://p0.meituan.net/w.h/movie/5d579bf80909cba285093db441766061324196.jpg\",\"http://p0.meituan.net/w.h/movie/7d6e1ba1234ea8b4134e43e8e549f17f308710.jpg\",\"http://p1.meituan.net/w.h/movie/ef24ce01dc72ef91366a698307cb45dd275718.jpg\",\"http://p0.meituan.net/w.h/movie/6ebfc1b2c0e9afb6dc3f8c561420e220378981.jpg\",\"http://p1.meituan.net/w.h/movie/cfb412ebb73704f1dc67c820155b70bc395466.jpg\",\"http://p1.meituan.net/w.h/movie/1198d01a448a10a9aaf16b51986f2db0468937.jpg\",\"http://p0.meituan.net/w.h/movie/2ef10db2b2967532def7cdc4e32c3b7f657105.jpg\",\"http://p0.meituan.net/w.h/movie/bde7f6f0001bcba6357b5828f20c09cd321357.jpg\",\"http://p1.meituan.net/w.h/movie/25b2dc81375700c7035c1e6fa9efd727286777.jpg\",\"http://p1.meituan.net/w.h/movie/0b5289a62f65696d240185fc3dec67d3331950.jpg\",\"http://p1.meituan.net/w.h/movie/c3ce3646a21ef5acaabae52a9f863040253328.jpg\",\"http://p0.meituan.net/w.h/movie/1c8f43cbaa6e71cce6b7471e588b21f1540645.jpg\",\"http://p1.meituan.net/w.h/movie/b4ef616d50039c284c2fdf539b834dab696225.jpg\",\"http://p0.meituan.net/w.h/movie/5cec8d43352f164f117fc6c126fc015a716757.jpg\",\"http://p1.meituan.net/w.h/movie/ca84ac86af126081fef629b545446d54749772.jpg\",\"http://p1.meituan.net/w.h/movie/7f8900ef3d618de9aa64af37aee099e5678963.jpg\",\"http://p1.meituan.net/w.h/movie/4b065728c8321a7c299932248317ab69746727.jpg\",\"http://p0.meituan.net/w.h/movie/55befe46945858d4cb31203ac6118f7b767588.jpg\",\"http://p1.meituan.net/w.h/movie/28b3055ea2efb82f7832cb615274e5d8562412.jpg\"],\"pn\":214,\"preSale\":0,\"rt\":\"2018-07-05上映\",\"sc\":9.7,\"scm\":\"印度代神药，良心及时现\",\"showSnum\":true,\"snum\":1073429,\"src\":\"中国大陆\",\"star\":\"徐峥 周一围 王传君 谭卓 章宇 杨新鸣 王砚辉 贾晨飞 王佳佳 李乃文 龚蓓苾 宁浩 苇青 邓飞 \",\"vd\":\"http://maoyan.meituan.net/movie/videos/854x4804c109134879943f4b24387adc040504b.mp4\",\"ver\":\"2D/IMAX 2D/中国巨幕\",\"vnum\":8,\"wish\":164738,\"wishst\":0},\"CommentResponseModel\":{\"hcmts\":[{\"avatarurl\":\"https://img.meituan.net/avatar/b07fe330b3b11713ff096390d60d3a9b7039.jpg\",\"nick\":\"乐Forwards\",\"approve\":14756,\"oppose\":0,\"reply\":1177,\"score\":5,\"userId\":65021517,\"nickName\":\"乐Forwards\",\"vipInfo\":\"\",\"time\":\"2018-07-05 15:22\",\"id\":1026981572,\"content\":\"我认为这是一部跟摔跤吧爸爸同层次的电影，忍到最后还是会流泪。生命很坚强也很脆弱，命与药的关系就是这么赤裸裸。（不良厂商做药设天价，没有良心，不合理，但是这在世界都是完全合法的，决定用这个药来牟取暴利还是济世度人是药商自己的权利，毕竟药是他们研发的。）之前刚看完在这里说不良厂商，还是刚看完有些不清醒了，其实不论他们的主观意愿是什么，都是他们的成果给了病人选择的希望。国家政府会把药纳入医保、发展制度、免除关税来降低病人的压力，我们个人也应该为诸如此类的研发事业做出贡献，比如为医学难题做出重大突破，等等各行各业，才能将未来将好东西做到平价( ¯ᒡ̱¯ )و\"},{\"avatarurl\":\"\",\"nick\":\"龟口逃生\",\"approve\":22170,\"oppose\":0,\"reply\":432,\"score\":5,\"userId\":242680147,\"nickName\":\"龟口逃生\",\"vipInfo\":\"\",\"time\":\"2018-07-05 01:55\",\"id\":1026929804,\"content\":\"没的说，今年最好影片，没有之一\"},{\"avatarurl\":\"https://img.meituan.net/avatar/590edf580b1b58c3e9dc5a8e22061a8e219481.jpg\",\"nick\":\"梦里梦惊梦丶\",\"approve\":12126,\"oppose\":0,\"reply\":298,\"score\":5,\"userId\":117559106,\"nickName\":\"梦里梦惊梦丶\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:19\",\"id\":1026930110,\"content\":\"反正我哭了。我妈妈身体也不好。也是药养着。但是现在都有医保啥的。条件也好了。但是身临其境的想下。眼泪就止不住了。很好的电影\"},{\"avatarurl\":\"https://img.meituan.net/avatar/6809ed6becc04e30e9ddc43cde501a8e70570.jpg\",\"nick\":\"jay20m\",\"approve\":5082,\"oppose\":0,\"reply\":348,\"score\":4.5,\"userId\":33330867,\"nickName\":\"jay20m\",\"vipInfo\":\"\",\"time\":\"2018-07-05 20:16\",\"id\":1026932031,\"content\":\"给九分是因为这片真实的说出了在中国就是吃天价药，发个烧二话不说先打针吊牌几百块就没了，更别说得了重病治疗了，穷人家根本看不起病，\\n法律不在乎人情，只会纵容坏人，好人都得不好报！！\"},{\"avatarurl\":\"\",\"nick\":\"Qgu743039906\",\"approve\":4422,\"oppose\":0,\"reply\":117,\"score\":5,\"userId\":142629978,\"nickName\":\"Qgu743039906\",\"vipInfo\":\"\",\"time\":\"2018-07-05 15:12\",\"id\":1026986429,\"content\":\"以前不管看什么电影，从来没有写过影评，但是这部电影看完以后真的是忍不住，就好像胸中憋了一团火要炸裂的感觉。从电影院出来以后眼泪一直挂着。良心说，里面的演员颜值不像其他国产电影，甚至于那个小黄毛看起来还有点丑。就连关谷神奇也变丑了。但是演技真的很完美，从写实的角度把故事叙述的刀刀插入人心。我和我的家人没有得过什么大病，但是换位思考，谁还能保证到死也不会进医院？换成如果我是故事里面的人物，我估计我应该会承受不住压力自己了断不去耽误家里人。毕竟我们的国家就是这样，穷人真的不能病也不敢病，就像现在微信朋友圈里隔三差五的就是水滴筹或则其他类似的信息。每次看到都是一阵心痛和后怕。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/2d7ec8a3f61513feae27a80fced1b2c5258971.jpg\",\"nick\":\"王狗狗77\",\"approve\":4105,\"oppose\":0,\"reply\":94,\"score\":5,\"userId\":41784496,\"nickName\":\"王狗狗77\",\"vipInfo\":\"\",\"time\":\"2018-07-09 23:14\",\"id\":1026930189,\"content\":\"据说是一部真实事情而改编的电影，一直觉得徐铮是一位优秀的好演员。\\n\\n此部电影反应了社会、现实、人性的一些问题，庆幸我们遇上了一个好年代，国家逐渐得以民心，生命漫长而又短暂，好好活着就是最大的奢侈，好好生活，保持善良。平凡并不可怕，拥有高贵的品格才是人最需要的东西，身处绝境也能绽放出最温暖的笑容。\\n\\n这部电影的题材与以往很多国产电影不同，我相信电影最后每一个落泪的人内心都有一份纯净。希望以后能多有这样类型的电影，传递正能量，去正视社会，也敲醒人性的真诚和理解。\\n \\n这个世界没有药神，也没有超级英雄，而是因为千千万万平凡如你我的小人物。\\n\\n良心剧... 推荐 ～ \uD83D\uDE42\uD83D\uDE42\uD83D\uDE42\"},{\"avatarurl\":\"https://img.meituan.net/avatar/f718dd93235b7aa2bc3afae2267ef7dc74370.jpg\",\"nick\":\"生来倔强F\",\"approve\":3416,\"oppose\":0,\"reply\":39,\"score\":5,\"userId\":252113942,\"nickName\":\"生来倔强F\",\"vipInfo\":\"\",\"time\":\"2018-07-05 01:59\",\"id\":1026931225,\"content\":\"不亏和老公凌晨赶场过来看，话不多说，真的值得一看。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/4e2974b8a70fd2e82f238244f5cf705e86873.jpg\",\"nick\":\"IZM619073352\",\"approve\":2287,\"oppose\":0,\"reply\":31,\"score\":5,\"userId\":581494255,\"nickName\":\"IZM619073352\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:01\",\"id\":1026931248,\"content\":\"赶了个首映！ \\n应该是中国近5年的良心剧了吧[强][强][强] 前面搞笑，后面真实…… 推荐推荐推荐，不好看你找我[嘿哈]\"},{\"avatarurl\":\"\",\"nick\":\"BUm277947052\",\"approve\":1575,\"oppose\":0,\"reply\":10,\"score\":5,\"userId\":1011262448,\"nickName\":\"BUm277947052\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:06\",\"id\":1026931291,\"content\":\"真的很赞 笑点泪点把控的很好\\n故事真实 今天去看爆满 前面笑一片 后面哭一片\"},{\"avatarurl\":\"https://img.meituan.net/avatar/de1fd17ab7490ee050ff46a3e9938c44107518.jpg\",\"nick\":\"刮哥\",\"approve\":441,\"oppose\":0,\"reply\":7,\"score\":4.5,\"userId\":52565494,\"nickName\":\"刮哥\",\"vipInfo\":\"自媒体\",\"time\":\"2018-07-07 12:00\",\"id\":1027433186,\"content\":\"虐心，但走心，这是生活的真相。希望越来越好，向有社会责任感的制作团队致敬！\"}],\"cmts\":[{\"avatarurl\":\"https://img.meituan.net/avatar/116e4bc6392c60962bb029e8b45715d977591.jpg\",\"nick\":\"快乐每一天2385\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":454240658,\"nickName\":\"快乐每一天2385\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028356459,\"content\":\"最好影片太感人了\"},{\"avatarurl\":\"\",\"nick\":\"IvI151288352\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":3.5,\"userId\":1518471490,\"nickName\":\"IvI151288352\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028433469,\"content\":\"挺好挺好挺好挺好\"},{\"avatarurl\":\"\",\"nick\":\"钟情2075\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":1664262106,\"nickName\":\"钟情2075\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1027030133,\"content\":\"特别好。演出了很多人的心声\"},{\"avatarurl\":\"\",\"nick\":\"恒、Man\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":1668321147,\"nickName\":\"恒、Man\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028447089,\"content\":\"非常好的一部影片，让人感触很深，意犹未尽的感觉\"},{\"avatarurl\":\"\",\"nick\":\"Cmenfy\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":42184514,\"nickName\":\"Cmenfy\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028441273,\"content\":\"好看！真走心！\"}],\"total\":294069,\"hasNext\":true}}}";
        MovieInformation information = JSON.parseObject(result, MovieInformation.class);
        model.addAttribute("movie", information);
        movieWish.setNm(information.getData().getMovieDetailModel().getNm());
        movieWish.setSc(information.getData().getMovieDetailModel().getSc());

        System.out.println("movieid:" + movieWish.getMovieid());
        System.out.println("movienm:" + movieWish.getNm());
        System.out.println("moviesc:" + movieWish.getSc());
        int id_result = movieWishService.insert(movieWish);
        if (id_result != 1) {
            map.put("result", "0");
            //return map;
            return null;
        }
        map.put("result", "1");
        //return map;
        //我关注的电影列表

        Map<String, Integer> userMap = new HashMap<>();
        userMap.put("uid",(Integer) request.getSession().getAttribute("uid"));
        userMap.put("start",0);
        List<MovieWish> FocusList=new ArrayList<>();
        FocusList=movieWishService.selectByUid(userMap);

        model.addAttribute("FocusList", FocusList);
        model.addAttribute("userInformation", userInformation);
        return "FocusList";
    }

    //查看想看的人的列表
    @RequestMapping(value = "/check/movie/wish")
    public String checkMovieWish(Model model,HttpServletRequest request) {
        System.out.println("check/movie/wish:ok");
        String movieid = request.getParameter("id");
        Integer new_movie_id = Integer.valueOf(movieid);
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
        System.out.println("username:"+userInformation.getName());
        List<MovieWish> wishlist = new ArrayList<>();
        List<Integer> uidList = new ArrayList<>();
        List<UserInformation> userInformationList = new ArrayList<>();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            //return wishlist;
            return null;
        }
        Map<String, Integer> map = new HashMap<>();
        map.put("movieid", new_movie_id);
        map.put("start", 0);
        wishlist = movieWishService.selectByMovieid(map);
        for (MovieWish m : wishlist) {
            uidList.add(m.getUid());//获取想看这部电影的所有用户id
        }
        userInformationList = userInformationService.getAllForeach(uidList);//根据用户id获取所有想看的用户的信息
        String testString = userInformation.getName();
        System.out.println(testString);
        //return userInformationList;

        String url = "http://m.maoyan.com/movie/" + movieid + ".json";
//        String result = HttpUtils.maoyan(url);
        String result = "{\"control\":{\"expires\":3600},\"status\":0,\"data\":{\"MovieDetailModel\":{\"cat\":\"剧情,喜剧\",\"dealsum\":0,\"dir\":\"文牧野 \",\"dra\":\"\n" +
                "一位不速之客的意外到访，打破了神油店老板程勇（徐峥 饰）的平凡人生，他从一个交不起房租的男性保健品商贩，一跃成为印度仿制药“格列宁”的独家代理商。收获巨额利润的他，生活剧烈变化，被病患们冠以“药神”的称号。但是，一场关于救赎的拉锯战也在波涛暗涌中慢慢展开......\n" +
                "\n" +
                "\",\"dur\":117,\"id\":1200486,\"imax\":true,\"img\":\"http://p0.meituan.net/165.220/movie/238e2dc36beae55a71cabfc14069fe78236351.jpg\",\"isShowing\":true,\"late\":false,\"mk\":0,\"nm\":\"我不是药神\",\"photos\":[\"http://p1.meituan.net/w.h/movie/36d1a14aca0666c9ca6ed1bd3031c7f1566914.jpg\",\"http://p0.meituan.net/w.h/movie/5d579bf80909cba285093db441766061324196.jpg\",\"http://p0.meituan.net/w.h/movie/7d6e1ba1234ea8b4134e43e8e549f17f308710.jpg\",\"http://p1.meituan.net/w.h/movie/ef24ce01dc72ef91366a698307cb45dd275718.jpg\",\"http://p0.meituan.net/w.h/movie/6ebfc1b2c0e9afb6dc3f8c561420e220378981.jpg\",\"http://p1.meituan.net/w.h/movie/cfb412ebb73704f1dc67c820155b70bc395466.jpg\",\"http://p1.meituan.net/w.h/movie/1198d01a448a10a9aaf16b51986f2db0468937.jpg\",\"http://p0.meituan.net/w.h/movie/2ef10db2b2967532def7cdc4e32c3b7f657105.jpg\",\"http://p0.meituan.net/w.h/movie/bde7f6f0001bcba6357b5828f20c09cd321357.jpg\",\"http://p1.meituan.net/w.h/movie/25b2dc81375700c7035c1e6fa9efd727286777.jpg\",\"http://p1.meituan.net/w.h/movie/0b5289a62f65696d240185fc3dec67d3331950.jpg\",\"http://p1.meituan.net/w.h/movie/c3ce3646a21ef5acaabae52a9f863040253328.jpg\",\"http://p0.meituan.net/w.h/movie/1c8f43cbaa6e71cce6b7471e588b21f1540645.jpg\",\"http://p1.meituan.net/w.h/movie/b4ef616d50039c284c2fdf539b834dab696225.jpg\",\"http://p0.meituan.net/w.h/movie/5cec8d43352f164f117fc6c126fc015a716757.jpg\",\"http://p1.meituan.net/w.h/movie/ca84ac86af126081fef629b545446d54749772.jpg\",\"http://p1.meituan.net/w.h/movie/7f8900ef3d618de9aa64af37aee099e5678963.jpg\",\"http://p1.meituan.net/w.h/movie/4b065728c8321a7c299932248317ab69746727.jpg\",\"http://p0.meituan.net/w.h/movie/55befe46945858d4cb31203ac6118f7b767588.jpg\",\"http://p1.meituan.net/w.h/movie/28b3055ea2efb82f7832cb615274e5d8562412.jpg\"],\"pn\":214,\"preSale\":0,\"rt\":\"2018-07-05上映\",\"sc\":9.7,\"scm\":\"印度代神药，良心及时现\",\"showSnum\":true,\"snum\":1073429,\"src\":\"中国大陆\",\"star\":\"徐峥 周一围 王传君 谭卓 章宇 杨新鸣 王砚辉 贾晨飞 王佳佳 李乃文 龚蓓苾 宁浩 苇青 邓飞 \",\"vd\":\"http://maoyan.meituan.net/movie/videos/854x4804c109134879943f4b24387adc040504b.mp4\",\"ver\":\"2D/IMAX 2D/中国巨幕\",\"vnum\":8,\"wish\":164738,\"wishst\":0},\"CommentResponseModel\":{\"hcmts\":[{\"avatarurl\":\"https://img.meituan.net/avatar/b07fe330b3b11713ff096390d60d3a9b7039.jpg\",\"nick\":\"乐Forwards\",\"approve\":14756,\"oppose\":0,\"reply\":1177,\"score\":5,\"userId\":65021517,\"nickName\":\"乐Forwards\",\"vipInfo\":\"\",\"time\":\"2018-07-05 15:22\",\"id\":1026981572,\"content\":\"我认为这是一部跟摔跤吧爸爸同层次的电影，忍到最后还是会流泪。生命很坚强也很脆弱，命与药的关系就是这么赤裸裸。（不良厂商做药设天价，没有良心，不合理，但是这在世界都是完全合法的，决定用这个药来牟取暴利还是济世度人是药商自己的权利，毕竟药是他们研发的。）之前刚看完在这里说不良厂商，还是刚看完有些不清醒了，其实不论他们的主观意愿是什么，都是他们的成果给了病人选择的希望。国家政府会把药纳入医保、发展制度、免除关税来降低病人的压力，我们个人也应该为诸如此类的研发事业做出贡献，比如为医学难题做出重大突破，等等各行各业，才能将未来将好东西做到平价( ¯ᒡ̱¯ )و\"},{\"avatarurl\":\"\",\"nick\":\"龟口逃生\",\"approve\":22170,\"oppose\":0,\"reply\":432,\"score\":5,\"userId\":242680147,\"nickName\":\"龟口逃生\",\"vipInfo\":\"\",\"time\":\"2018-07-05 01:55\",\"id\":1026929804,\"content\":\"没的说，今年最好影片，没有之一\"},{\"avatarurl\":\"https://img.meituan.net/avatar/590edf580b1b58c3e9dc5a8e22061a8e219481.jpg\",\"nick\":\"梦里梦惊梦丶\",\"approve\":12126,\"oppose\":0,\"reply\":298,\"score\":5,\"userId\":117559106,\"nickName\":\"梦里梦惊梦丶\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:19\",\"id\":1026930110,\"content\":\"反正我哭了。我妈妈身体也不好。也是药养着。但是现在都有医保啥的。条件也好了。但是身临其境的想下。眼泪就止不住了。很好的电影\"},{\"avatarurl\":\"https://img.meituan.net/avatar/6809ed6becc04e30e9ddc43cde501a8e70570.jpg\",\"nick\":\"jay20m\",\"approve\":5082,\"oppose\":0,\"reply\":348,\"score\":4.5,\"userId\":33330867,\"nickName\":\"jay20m\",\"vipInfo\":\"\",\"time\":\"2018-07-05 20:16\",\"id\":1026932031,\"content\":\"给九分是因为这片真实的说出了在中国就是吃天价药，发个烧二话不说先打针吊牌几百块就没了，更别说得了重病治疗了，穷人家根本看不起病，\\n法律不在乎人情，只会纵容坏人，好人都得不好报！！\"},{\"avatarurl\":\"\",\"nick\":\"Qgu743039906\",\"approve\":4422,\"oppose\":0,\"reply\":117,\"score\":5,\"userId\":142629978,\"nickName\":\"Qgu743039906\",\"vipInfo\":\"\",\"time\":\"2018-07-05 15:12\",\"id\":1026986429,\"content\":\"以前不管看什么电影，从来没有写过影评，但是这部电影看完以后真的是忍不住，就好像胸中憋了一团火要炸裂的感觉。从电影院出来以后眼泪一直挂着。良心说，里面的演员颜值不像其他国产电影，甚至于那个小黄毛看起来还有点丑。就连关谷神奇也变丑了。但是演技真的很完美，从写实的角度把故事叙述的刀刀插入人心。我和我的家人没有得过什么大病，但是换位思考，谁还能保证到死也不会进医院？换成如果我是故事里面的人物，我估计我应该会承受不住压力自己了断不去耽误家里人。毕竟我们的国家就是这样，穷人真的不能病也不敢病，就像现在微信朋友圈里隔三差五的就是水滴筹或则其他类似的信息。每次看到都是一阵心痛和后怕。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/2d7ec8a3f61513feae27a80fced1b2c5258971.jpg\",\"nick\":\"王狗狗77\",\"approve\":4105,\"oppose\":0,\"reply\":94,\"score\":5,\"userId\":41784496,\"nickName\":\"王狗狗77\",\"vipInfo\":\"\",\"time\":\"2018-07-09 23:14\",\"id\":1026930189,\"content\":\"据说是一部真实事情而改编的电影，一直觉得徐铮是一位优秀的好演员。\\n\\n此部电影反应了社会、现实、人性的一些问题，庆幸我们遇上了一个好年代，国家逐渐得以民心，生命漫长而又短暂，好好活着就是最大的奢侈，好好生活，保持善良。平凡并不可怕，拥有高贵的品格才是人最需要的东西，身处绝境也能绽放出最温暖的笑容。\\n\\n这部电影的题材与以往很多国产电影不同，我相信电影最后每一个落泪的人内心都有一份纯净。希望以后能多有这样类型的电影，传递正能量，去正视社会，也敲醒人性的真诚和理解。\\n \\n这个世界没有药神，也没有超级英雄，而是因为千千万万平凡如你我的小人物。\\n\\n良心剧... 推荐 ～ \uD83D\uDE42\uD83D\uDE42\uD83D\uDE42\"},{\"avatarurl\":\"https://img.meituan.net/avatar/f718dd93235b7aa2bc3afae2267ef7dc74370.jpg\",\"nick\":\"生来倔强F\",\"approve\":3416,\"oppose\":0,\"reply\":39,\"score\":5,\"userId\":252113942,\"nickName\":\"生来倔强F\",\"vipInfo\":\"\",\"time\":\"2018-07-05 01:59\",\"id\":1026931225,\"content\":\"不亏和老公凌晨赶场过来看，话不多说，真的值得一看。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/4e2974b8a70fd2e82f238244f5cf705e86873.jpg\",\"nick\":\"IZM619073352\",\"approve\":2287,\"oppose\":0,\"reply\":31,\"score\":5,\"userId\":581494255,\"nickName\":\"IZM619073352\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:01\",\"id\":1026931248,\"content\":\"赶了个首映！ \\n应该是中国近5年的良心剧了吧[强][强][强] 前面搞笑，后面真实…… 推荐推荐推荐，不好看你找我[嘿哈]\"},{\"avatarurl\":\"\",\"nick\":\"BUm277947052\",\"approve\":1575,\"oppose\":0,\"reply\":10,\"score\":5,\"userId\":1011262448,\"nickName\":\"BUm277947052\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:06\",\"id\":1026931291,\"content\":\"真的很赞 笑点泪点把控的很好\\n故事真实 今天去看爆满 前面笑一片 后面哭一片\"},{\"avatarurl\":\"https://img.meituan.net/avatar/de1fd17ab7490ee050ff46a3e9938c44107518.jpg\",\"nick\":\"刮哥\",\"approve\":441,\"oppose\":0,\"reply\":7,\"score\":4.5,\"userId\":52565494,\"nickName\":\"刮哥\",\"vipInfo\":\"自媒体\",\"time\":\"2018-07-07 12:00\",\"id\":1027433186,\"content\":\"虐心，但走心，这是生活的真相。希望越来越好，向有社会责任感的制作团队致敬！\"}],\"cmts\":[{\"avatarurl\":\"https://img.meituan.net/avatar/116e4bc6392c60962bb029e8b45715d977591.jpg\",\"nick\":\"快乐每一天2385\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":454240658,\"nickName\":\"快乐每一天2385\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028356459,\"content\":\"最好影片太感人了\"},{\"avatarurl\":\"\",\"nick\":\"IvI151288352\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":3.5,\"userId\":1518471490,\"nickName\":\"IvI151288352\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028433469,\"content\":\"挺好挺好挺好挺好\"},{\"avatarurl\":\"\",\"nick\":\"钟情2075\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":1664262106,\"nickName\":\"钟情2075\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1027030133,\"content\":\"特别好。演出了很多人的心声\"},{\"avatarurl\":\"\",\"nick\":\"恒、Man\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":1668321147,\"nickName\":\"恒、Man\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028447089,\"content\":\"非常好的一部影片，让人感触很深，意犹未尽的感觉\"},{\"avatarurl\":\"\",\"nick\":\"Cmenfy\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":42184514,\"nickName\":\"Cmenfy\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028441273,\"content\":\"好看！真走心！\"}],\"total\":294069,\"hasNext\":true}}}";
        MovieInformation information = JSON.parseObject(result, MovieInformation.class);
        model.addAttribute("movie", information);

        model.addAttribute("userInformationList", userInformationList);
        model.addAttribute("userInformation", userInformation);
        return "userInformationList";
    }

    //即将上映的电影
    @RequestMapping(value = "/coming/soon")
    public String comingSoon(Model model, HttpServletRequest request) {
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/coming_soon";
        HashMap<String, String> params = new HashMap<>();
        AllInformation comingSoon = getMovieInformation(url, params, ENCODE, GET);
        model.addAttribute("movie", comingSoon);
        model.addAttribute("movie_name", "即将上映");
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        getFriend(model, userInformation.getId());
        return "/movie/SearchMovieResult";
    }

    //top250
    @RequestMapping(value = "/top")
    public String top(Model model, HttpServletRequest request) {
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/top250";
        HashMap<String, String> params = new HashMap<>();
        AllInformation allInformation = getMovieInformation(url, params, ENCODE, GET);
        model.addAttribute("movie", allInformation);
        model.addAttribute("movie_name", "高分电影");
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        getFriend(model, userInformation.getId());
        return "/movie/SearchMovieResult";
    }

    //正在上映
    @RequestMapping(value = "/coming")
    public String coming(Model model, HttpServletRequest request) {
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = "http://m.maoyan.com/movie/list.json?type=hot&offset=0&limit=20";
//        String result = HttpUtils.maoyan(url);
        String result = "{\"control\":{\"expires\":1800},\"status\":0,\"data\":{\"hasNext\":true,\"movies\":[{\"showInfo\":\"今天205家影院放映5432场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":214,\"preSale\":0,\"vd\":\"\",\"dir\":\"文牧野\",\"star\":\"徐峥,周一围,王传君\",\"cat\":\"剧情,喜剧\",\"wish\":164738,\"3d\":false,\"src\":\"\",\"nm\":\"我不是药神\",\"img\":\"http://p0.meituan.net/165.220/movie/238e2dc36beae55a71cabfc14069fe78236351.jpg\",\"sc\":9.7,\"ver\":\"2D/IMAX 2D/中国巨幕\",\"showDate\":\"\",\"dur\":117,\"scm\":\"印度代神药，良心及时现\",\"imax\":true,\"snum\":1073429,\"rt\":\"2018-07-05上映\",\"time\":\"\",\"id\":1200486},{\"showInfo\":\"今天185家影院放映743场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":399,\"preSale\":0,\"vd\":\"\",\"dir\":\"韩延\",\"star\":\"李易峰,迈克尔·道格拉斯,周冬雨\",\"cat\":\"动作,悬疑,冒险\",\"wish\":194088,\"3d\":true,\"src\":\"\",\"nm\":\"动物世界\",\"img\":\"http://p0.meituan.net/165.220/movie/c6382117be87ac04fcc81fa02df815944946929.jpg\",\"sc\":8.4,\"ver\":\"2D/3D/IMAX 3D/中国巨幕/全景声\",\"showDate\":\"\",\"dur\":132,\"scm\":\"是考验人性还是自我救赎？\",\"imax\":true,\"snum\":359810,\"rt\":\"2018-06-29上映\",\"time\":\"\",\"id\":1198178},{\"showInfo\":\"2018-07-13 本周五上映\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":208,\"preSale\":1,\"vd\":\"\",\"dir\":\"姜文\",\"star\":\"姜文,彭于晏,廖凡\",\"cat\":\"爱情,动作,冒险\",\"wish\":192273,\"3d\":false,\"src\":\"\",\"nm\":\"邪不压正\",\"img\":\"http://p1.meituan.net/165.220/movie/8ea36c210973ef97616be7ca1f0d5fc0216565.jpg\",\"sc\":0.0,\"ver\":\"2D/IMAX 2D/中国巨幕/全景声\",\"showDate\":\"\",\"dur\":137,\"scm\":\"姜文导演最新力作\",\"imax\":true,\"snum\":4729,\"rt\":\"本周五上映\",\"time\":\"\",\"id\":248566},{\"showInfo\":\"2018-07-13 本周五上映\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":307,\"preSale\":1,\"vd\":\"\",\"dir\":\"张鹏\",\"star\":\"吴磊,梁家辉,刘嘉玲\",\"cat\":\"爱情,动作,奇幻\",\"wish\":159166,\"3d\":true,\"src\":\"\",\"nm\":\"阿修罗\",\"img\":\"http://p1.meituan.net/165.220/movie/cdebee8c9cd0f220576f47d5a816479c678915.jpg\",\"sc\":0.0,\"ver\":\"2D/3D/中国巨幕\",\"showDate\":\"\",\"dur\":141,\"scm\":\"六道轮回中，万事皆为空\",\"imax\":false,\"snum\":11698,\"rt\":\"本周五上映\",\"time\":\"\",\"id\":346096},{\"showInfo\":\"今天190家影院放映689场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":97,\"preSale\":0,\"vd\":\"\",\"dir\":\"何澄\",\"star\":\"刘纯燕,董浩,鞠萍\",\"cat\":\"动画,冒险,奇幻\",\"wish\":47729,\"3d\":false,\"src\":\"\",\"nm\":\"新大头儿子和小头爸爸3：俄罗斯奇遇记\",\"img\":\"http://p1.meituan.net/165.220/movie/517fd5611a22ea9b498fb2dac3dcd1461033977.jpg\",\"sc\":8.5,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":80,\"scm\":\"大头儿子小头爸爸如何摆脱白夜城\",\"imax\":false,\"snum\":36969,\"rt\":\"2018-07-06上映\",\"time\":\"\",\"id\":1220713},{\"showInfo\":\"今天174家影院放映591场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":121,\"preSale\":0,\"vd\":\"\",\"dir\":\"布拉德·伯德\",\"star\":\"格雷格·T·尼尔森,霍利·亨特,莎拉·沃威尔\",\"cat\":\"动画,动作,冒险\",\"wish\":67501,\"3d\":true,\"src\":\"\",\"nm\":\"超人总动员2\",\"img\":\"http://p1.meituan.net/165.220/movie/6383f5cb72f994370f9e817eaa495aaf428644.jpg\",\"sc\":9.0,\"ver\":\"2D/3D/IMAX 3D/中国巨幕/全景声\",\"showDate\":\"\",\"dur\":126,\"scm\":\"\",\"imax\":true,\"snum\":117595,\"rt\":\"2018-06-22上映\",\"time\":\"\",\"id\":338385},{\"showInfo\":\"今天151家影院放映526场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":62,\"preSale\":0,\"vd\":\"\",\"dir\":\"胡安·安东尼奥·巴亚纳\",\"star\":\"克里斯·帕拉特,布莱丝·达拉斯·霍华德,泰德·拉文\",\"cat\":\"动作,冒险,科幻\",\"wish\":621367,\"3d\":true,\"src\":\"\",\"nm\":\"侏罗纪世界2\",\"img\":\"http://p1.meituan.net/165.220/movie/3d17aa5ee07f5d66239d8393bcb8fe5196556.jpg\",\"sc\":8.5,\"ver\":\"2D/3D/IMAX 3D/中国巨幕/全景声\",\"showDate\":\"\",\"dur\":128,\"scm\":\"寻找恐龙的欧文与克莱尔发现……\",\"imax\":true,\"snum\":661994,\"rt\":\"2018-06-15上映\",\"time\":\"\",\"id\":341628},{\"showInfo\":\"2018-07-20 下周五上映\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":92,\"preSale\":1,\"vd\":\"\",\"dir\":\"罗森·马歇尔·瑟伯\",\"star\":\"道恩·强森,昆凌,文峰\",\"cat\":\"动作,冒险\",\"wish\":62773,\"3d\":true,\"src\":\"\",\"nm\":\"摩天营救\",\"img\":\"http://p0.meituan.net/165.220/movie/bd9cbf2bd06efbf6a5aab9b36e59e57b395548.jpg\",\"sc\":0.0,\"ver\":\"2D/IMAX 3D/全景声\",\"showDate\":\"\",\"dur\":102,\"scm\":\"摩天大楼遭破坏，威尔能营救成功吗\",\"imax\":true,\"snum\":1160,\"rt\":\"下周五上映\",\"time\":\"\",\"id\":1203528},{\"showInfo\":\"今天59家影院放映147场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":54,\"preSale\":0,\"vd\":\"\",\"dir\":\"崔永元,麦丽丝,德格娜,张大磊,杜粮\",\"star\":\"于洋,王晓棠,王心刚\",\"cat\":\"纪录片\",\"wish\":11908,\"3d\":false,\"src\":\"\",\"nm\":\"您一定不要错过\",\"img\":\"http://p1.meituan.net/165.220/movie/451a3da1f09ea4bc4312b373443b68b33250010.jpg\",\"sc\":9.2,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":99,\"scm\":\"跟崔永元一起看老胶片下的人物故事\",\"imax\":false,\"snum\":15374,\"rt\":\"2018-07-06上映\",\"time\":\"\",\"id\":1225340},{\"showInfo\":\"今天47家影院放映112场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":57,\"preSale\":0,\"vd\":\"\",\"dir\":\"王家卫\",\"star\":\"张国荣,张曼玉,刘德华\",\"cat\":\"剧情,爱情,犯罪\",\"wish\":26482,\"3d\":false,\"src\":\"\",\"nm\":\"阿飞正传\",\"img\":\"http://p0.meituan.net/165.220/movie/885fc379c614a2b4175587b95ac98eb95045650.jpg\",\"sc\":8.8,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":94,\"scm\":\"\",\"imax\":false,\"snum\":16903,\"rt\":\"2018-06-25上映\",\"time\":\"\",\"id\":11237},{\"showInfo\":\"今天66家影院放映112场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":107,\"preSale\":0,\"vd\":\"\",\"dir\":\"史蒂芬·C·米勒\",\"star\":\"西尔维斯特·史泰龙,黄晓明,戴夫·巴蒂斯塔\",\"cat\":\"动作,惊悚,犯罪\",\"wish\":147973,\"3d\":false,\"src\":\"\",\"nm\":\"金蝉脱壳2\",\"img\":\"http://p1.meituan.net/165.220/movie/95f246163e1b11702685fd8b01421182466294.jpg\",\"sc\":5.4,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":93,\"scm\":\"看海上监狱大营救，如何越狱成功！\",\"imax\":false,\"snum\":101532,\"rt\":\"2018-06-29上映\",\"time\":\"\",\"id\":1197181},{\"showInfo\":\"2018-07-14 本周六上映\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":32,\"preSale\":1,\"vd\":\"\",\"dir\":\"叶伟青,王以立\",\"star\":\"\",\"cat\":\"喜剧,动画,冒险\",\"wish\":7063,\"3d\":true,\"src\":\"\",\"nm\":\"小悟空\",\"img\":\"http://p0.meituan.net/165.220/movie/02a4444d80810e84b947701febb1a6a816007019.jpg\",\"sc\":0.0,\"ver\":\"2D/3D\",\"showDate\":\"\",\"dur\":85,\"scm\":\"大森会成功打败牛魔王吗？\",\"imax\":false,\"snum\":461,\"rt\":\"本周六上映\",\"time\":\"\",\"id\":592271},{\"showInfo\":\"2018-07-20 下周五上映\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":27,\"preSale\":1,\"vd\":\"\",\"dir\":\"拉加·高斯内尔\",\"star\":\"威尔·阿奈特,娜塔莎·雷昂,卢达克里斯\",\"cat\":\"剧情,喜剧\",\"wish\":8525,\"3d\":false,\"src\":\"\",\"nm\":\"汪星卧底\",\"img\":\"http://p1.meituan.net/165.220/movie/f2b43dd5aef309c0b314ca67760980ef240821.jpg\",\"sc\":0.0,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":93,\"scm\":\"\",\"imax\":false,\"snum\":187,\"rt\":\"下周五上映\",\"time\":\"\",\"id\":883179},{\"showInfo\":\"今天20家影院放映34场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":217,\"preSale\":0,\"vd\":\"\",\"dir\":\"小沈阳\",\"star\":\"小沈阳,潘斌龙,宋芸桦\",\"cat\":\"喜剧,动作\",\"wish\":42015,\"3d\":false,\"src\":\"\",\"nm\":\"猛虫过江\",\"img\":\"http://p0.meituan.net/165.220/movie/5fd19e095c4ac795cc291179356152c52921178.jpg\",\"sc\":8.0,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":99,\"scm\":\"在追杀中发生的啼笑皆非的故事\",\"imax\":false,\"snum\":150206,\"rt\":\"2018-06-15上映\",\"time\":\"\",\"id\":1139994},{\"showInfo\":\"今天18家影院放映28场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":58,\"preSale\":0,\"vd\":\"\",\"dir\":\"矢口史靖\",\"star\":\"小日向文世,深津绘里,泉泽祐希\",\"cat\":\"剧情,喜剧,家庭\",\"wish\":16868,\"3d\":false,\"src\":\"\",\"nm\":\"生存家族\",\"img\":\"http://p1.meituan.net/165.220/movie/ef958c2221323a10e7ac43a3887936fa14593664.jpg\",\"sc\":8.5,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":117,\"scm\":\"\",\"imax\":false,\"snum\":10192,\"rt\":\"2018-06-22上映\",\"time\":\"\",\"id\":1132494},{\"showInfo\":\"今天18家影院放映22场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":2,\"preSale\":0,\"vd\":\"\",\"dir\":\"王兴东,田水泉\",\"star\":\"佟瑞欣,王诗槐,刘小锋\",\"cat\":\"剧情\",\"wish\":25,\"3d\":false,\"src\":\"\",\"nm\":\"邹碧华\",\"img\":\"http://p0.meituan.net/165.220/movie/0635a69a56a78143fcfc7d2fe83db190317328.jpg\",\"sc\":0.0,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":88,\"scm\":\"\",\"imax\":false,\"snum\":158,\"rt\":\"2017-11-20上映\",\"time\":\"\",\"id\":344661},{\"showInfo\":\"2018-07-13 本周五上映\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":12,\"preSale\":1,\"vd\":\"\",\"dir\":\"红旗\",\"star\":\"郭少芸,白柳汐,周昊勇呈\",\"cat\":\"奇幻,音乐\",\"wish\":5102,\"3d\":false,\"src\":\"\",\"nm\":\"天佑之爱\",\"img\":\"http://p1.meituan.net/165.220/movie/cd51440ae8d3c8450b7038566c656d67189910.jpg\",\"sc\":0.0,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":95,\"scm\":\"通过音乐找到亲生父母的团圆故事\",\"imax\":false,\"snum\":462,\"rt\":\"本周五上映\",\"time\":\"\",\"id\":1221133},{\"showInfo\":\"今天14家影院放映16场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":20,\"preSale\":0,\"vd\":\"\",\"dir\":\"张跃斌\",\"star\":\"韩素媛,蓝波,庞贝童\",\"cat\":\"恐怖,惊悚\",\"wish\":6068,\"3d\":false,\"src\":\"\",\"nm\":\"细思极恐\",\"img\":\"http://p1.meituan.net/165.220/movie/df427f69d892ee0550ae7ca35621ca1c1618793.jpg\",\"sc\":3.8,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":92,\"scm\":\"地下停车库的恐怖故事……\",\"imax\":false,\"snum\":2121,\"rt\":\"2018-07-06上映\",\"time\":\"\",\"id\":1221379},{\"showInfo\":\"今天8家影院放映13场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":189,\"preSale\":0,\"vd\":\"\",\"dir\":\"李昕芸\",\"star\":\"王千源,袁姗姗,刘桦\",\"cat\":\"喜剧,动作,犯罪\",\"wish\":47330,\"3d\":false,\"src\":\"\",\"nm\":\"龙虾刑警\",\"img\":\"http://p1.meituan.net/165.220/movie/4e8f02b04bf755da8b888952392b9729968669.jpg\",\"sc\":7.4,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":93,\"scm\":\"寻找线索发现更大阴谋……\",\"imax\":false,\"snum\":37357,\"rt\":\"2018-06-22上映\",\"time\":\"\",\"id\":1203310},{\"showInfo\":\"今天9家影院放映12场\",\"late\":false,\"cnms\":0,\"sn\":0,\"pn\":45,\"preSale\":0,\"vd\":\"\",\"dir\":\"丹尼拉·科兹洛夫斯基\",\"star\":\"丹尼拉·科兹洛夫斯基,弗拉基米尔·伊利因,奥莉亚·祖耶娃\",\"cat\":\"剧情,运动\",\"wish\":20524,\"3d\":false,\"src\":\"\",\"nm\":\"最后一球\",\"img\":\"http://p0.meituan.net/165.220/movie/3994a186cac00de4337b30afb6f2765e2580621.jpg\",\"sc\":9.1,\"ver\":\"2D\",\"showDate\":\"\",\"dur\":138,\"scm\":\"面对困境，尤里带领国家队出征！\",\"imax\":false,\"snum\":6270,\"rt\":\"2018-06-29上映\",\"time\":\"\",\"id\":1219270}]}}";
        Hot hot = JSON.parseObject(result, Hot.class);
        model.addAttribute("movie", hot);
//        model.addAttribute("movie_name", "正在上映");
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        getFriend(model, userInformation.getId());
        return "/movie/hot_movie";
    }

    //热映电影的详情
    @RequestMapping(value = "/hot/movie/information/{id}")
    public String hotMovieInformation(@PathVariable String id, Model model, HttpServletRequest request) {
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:../login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = "http://m.maoyan.com/movie/" + id + ".json";
//        String result = HttpUtils.maoyan(url);
        String result = "{\"control\":{\"expires\":3600},\"status\":0,\"data\":{\"MovieDetailModel\":{\"cat\":\"剧情,喜剧\",\"dealsum\":0,\"dir\":\"文牧野 \",\"dra\":\"\n" +
                "一位不速之客的意外到访，打破了神油店老板程勇（徐峥 饰）的平凡人生，他从一个交不起房租的男性保健品商贩，一跃成为印度仿制药“格列宁”的独家代理商。收获巨额利润的他，生活剧烈变化，被病患们冠以“药神”的称号。但是，一场关于救赎的拉锯战也在波涛暗涌中慢慢展开......\n" +
                "\n" +
                "\",\"dur\":117,\"id\":1200486,\"imax\":true,\"img\":\"http://p0.meituan.net/165.220/movie/238e2dc36beae55a71cabfc14069fe78236351.jpg\",\"isShowing\":true,\"late\":false,\"mk\":0,\"nm\":\"我不是药神\",\"photos\":[\"http://p1.meituan.net/w.h/movie/36d1a14aca0666c9ca6ed1bd3031c7f1566914.jpg\",\"http://p0.meituan.net/w.h/movie/5d579bf80909cba285093db441766061324196.jpg\",\"http://p0.meituan.net/w.h/movie/7d6e1ba1234ea8b4134e43e8e549f17f308710.jpg\",\"http://p1.meituan.net/w.h/movie/ef24ce01dc72ef91366a698307cb45dd275718.jpg\",\"http://p0.meituan.net/w.h/movie/6ebfc1b2c0e9afb6dc3f8c561420e220378981.jpg\",\"http://p1.meituan.net/w.h/movie/cfb412ebb73704f1dc67c820155b70bc395466.jpg\",\"http://p1.meituan.net/w.h/movie/1198d01a448a10a9aaf16b51986f2db0468937.jpg\",\"http://p0.meituan.net/w.h/movie/2ef10db2b2967532def7cdc4e32c3b7f657105.jpg\",\"http://p0.meituan.net/w.h/movie/bde7f6f0001bcba6357b5828f20c09cd321357.jpg\",\"http://p1.meituan.net/w.h/movie/25b2dc81375700c7035c1e6fa9efd727286777.jpg\",\"http://p1.meituan.net/w.h/movie/0b5289a62f65696d240185fc3dec67d3331950.jpg\",\"http://p1.meituan.net/w.h/movie/c3ce3646a21ef5acaabae52a9f863040253328.jpg\",\"http://p0.meituan.net/w.h/movie/1c8f43cbaa6e71cce6b7471e588b21f1540645.jpg\",\"http://p1.meituan.net/w.h/movie/b4ef616d50039c284c2fdf539b834dab696225.jpg\",\"http://p0.meituan.net/w.h/movie/5cec8d43352f164f117fc6c126fc015a716757.jpg\",\"http://p1.meituan.net/w.h/movie/ca84ac86af126081fef629b545446d54749772.jpg\",\"http://p1.meituan.net/w.h/movie/7f8900ef3d618de9aa64af37aee099e5678963.jpg\",\"http://p1.meituan.net/w.h/movie/4b065728c8321a7c299932248317ab69746727.jpg\",\"http://p0.meituan.net/w.h/movie/55befe46945858d4cb31203ac6118f7b767588.jpg\",\"http://p1.meituan.net/w.h/movie/28b3055ea2efb82f7832cb615274e5d8562412.jpg\"],\"pn\":214,\"preSale\":0,\"rt\":\"2018-07-05上映\",\"sc\":9.7,\"scm\":\"印度代神药，良心及时现\",\"showSnum\":true,\"snum\":1073429,\"src\":\"中国大陆\",\"star\":\"徐峥 周一围 王传君 谭卓 章宇 杨新鸣 王砚辉 贾晨飞 王佳佳 李乃文 龚蓓苾 宁浩 苇青 邓飞 \",\"vd\":\"http://maoyan.meituan.net/movie/videos/854x4804c109134879943f4b24387adc040504b.mp4\",\"ver\":\"2D/IMAX 2D/中国巨幕\",\"vnum\":8,\"wish\":164738,\"wishst\":0},\"CommentResponseModel\":{\"hcmts\":[{\"avatarurl\":\"https://img.meituan.net/avatar/b07fe330b3b11713ff096390d60d3a9b7039.jpg\",\"nick\":\"乐Forwards\",\"approve\":14756,\"oppose\":0,\"reply\":1177,\"score\":5,\"userId\":65021517,\"nickName\":\"乐Forwards\",\"vipInfo\":\"\",\"time\":\"2018-07-05 15:22\",\"id\":1026981572,\"content\":\"我认为这是一部跟摔跤吧爸爸同层次的电影，忍到最后还是会流泪。生命很坚强也很脆弱，命与药的关系就是这么赤裸裸。（不良厂商做药设天价，没有良心，不合理，但是这在世界都是完全合法的，决定用这个药来牟取暴利还是济世度人是药商自己的权利，毕竟药是他们研发的。）之前刚看完在这里说不良厂商，还是刚看完有些不清醒了，其实不论他们的主观意愿是什么，都是他们的成果给了病人选择的希望。国家政府会把药纳入医保、发展制度、免除关税来降低病人的压力，我们个人也应该为诸如此类的研发事业做出贡献，比如为医学难题做出重大突破，等等各行各业，才能将未来将好东西做到平价( ¯ᒡ̱¯ )و\"},{\"avatarurl\":\"\",\"nick\":\"龟口逃生\",\"approve\":22170,\"oppose\":0,\"reply\":432,\"score\":5,\"userId\":242680147,\"nickName\":\"龟口逃生\",\"vipInfo\":\"\",\"time\":\"2018-07-05 01:55\",\"id\":1026929804,\"content\":\"没的说，今年最好影片，没有之一\"},{\"avatarurl\":\"https://img.meituan.net/avatar/590edf580b1b58c3e9dc5a8e22061a8e219481.jpg\",\"nick\":\"梦里梦惊梦丶\",\"approve\":12126,\"oppose\":0,\"reply\":298,\"score\":5,\"userId\":117559106,\"nickName\":\"梦里梦惊梦丶\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:19\",\"id\":1026930110,\"content\":\"反正我哭了。我妈妈身体也不好。也是药养着。但是现在都有医保啥的。条件也好了。但是身临其境的想下。眼泪就止不住了。很好的电影\"},{\"avatarurl\":\"https://img.meituan.net/avatar/6809ed6becc04e30e9ddc43cde501a8e70570.jpg\",\"nick\":\"jay20m\",\"approve\":5082,\"oppose\":0,\"reply\":348,\"score\":4.5,\"userId\":33330867,\"nickName\":\"jay20m\",\"vipInfo\":\"\",\"time\":\"2018-07-05 20:16\",\"id\":1026932031,\"content\":\"给九分是因为这片真实的说出了在中国就是吃天价药，发个烧二话不说先打针吊牌几百块就没了，更别说得了重病治疗了，穷人家根本看不起病，\\n法律不在乎人情，只会纵容坏人，好人都得不好报！！\"},{\"avatarurl\":\"\",\"nick\":\"Qgu743039906\",\"approve\":4422,\"oppose\":0,\"reply\":117,\"score\":5,\"userId\":142629978,\"nickName\":\"Qgu743039906\",\"vipInfo\":\"\",\"time\":\"2018-07-05 15:12\",\"id\":1026986429,\"content\":\"以前不管看什么电影，从来没有写过影评，但是这部电影看完以后真的是忍不住，就好像胸中憋了一团火要炸裂的感觉。从电影院出来以后眼泪一直挂着。良心说，里面的演员颜值不像其他国产电影，甚至于那个小黄毛看起来还有点丑。就连关谷神奇也变丑了。但是演技真的很完美，从写实的角度把故事叙述的刀刀插入人心。我和我的家人没有得过什么大病，但是换位思考，谁还能保证到死也不会进医院？换成如果我是故事里面的人物，我估计我应该会承受不住压力自己了断不去耽误家里人。毕竟我们的国家就是这样，穷人真的不能病也不敢病，就像现在微信朋友圈里隔三差五的就是水滴筹或则其他类似的信息。每次看到都是一阵心痛和后怕。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/2d7ec8a3f61513feae27a80fced1b2c5258971.jpg\",\"nick\":\"王狗狗77\",\"approve\":4105,\"oppose\":0,\"reply\":94,\"score\":5,\"userId\":41784496,\"nickName\":\"王狗狗77\",\"vipInfo\":\"\",\"time\":\"2018-07-09 23:14\",\"id\":1026930189,\"content\":\"据说是一部真实事情而改编的电影，一直觉得徐铮是一位优秀的好演员。\\n\\n此部电影反应了社会、现实、人性的一些问题，庆幸我们遇上了一个好年代，国家逐渐得以民心，生命漫长而又短暂，好好活着就是最大的奢侈，好好生活，保持善良。平凡并不可怕，拥有高贵的品格才是人最需要的东西，身处绝境也能绽放出最温暖的笑容。\\n\\n这部电影的题材与以往很多国产电影不同，我相信电影最后每一个落泪的人内心都有一份纯净。希望以后能多有这样类型的电影，传递正能量，去正视社会，也敲醒人性的真诚和理解。\\n \\n这个世界没有药神，也没有超级英雄，而是因为千千万万平凡如你我的小人物。\\n\\n良心剧... 推荐 ～ \uD83D\uDE42\uD83D\uDE42\uD83D\uDE42\"},{\"avatarurl\":\"https://img.meituan.net/avatar/f718dd93235b7aa2bc3afae2267ef7dc74370.jpg\",\"nick\":\"生来倔强F\",\"approve\":3416,\"oppose\":0,\"reply\":39,\"score\":5,\"userId\":252113942,\"nickName\":\"生来倔强F\",\"vipInfo\":\"\",\"time\":\"2018-07-05 01:59\",\"id\":1026931225,\"content\":\"不亏和老公凌晨赶场过来看，话不多说，真的值得一看。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/4e2974b8a70fd2e82f238244f5cf705e86873.jpg\",\"nick\":\"IZM619073352\",\"approve\":2287,\"oppose\":0,\"reply\":31,\"score\":5,\"userId\":581494255,\"nickName\":\"IZM619073352\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:01\",\"id\":1026931248,\"content\":\"赶了个首映！ \\n应该是中国近5年的良心剧了吧[强][强][强] 前面搞笑，后面真实…… 推荐推荐推荐，不好看你找我[嘿哈]\"},{\"avatarurl\":\"\",\"nick\":\"BUm277947052\",\"approve\":1575,\"oppose\":0,\"reply\":10,\"score\":5,\"userId\":1011262448,\"nickName\":\"BUm277947052\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:06\",\"id\":1026931291,\"content\":\"真的很赞 笑点泪点把控的很好\\n故事真实 今天去看爆满 前面笑一片 后面哭一片\"},{\"avatarurl\":\"https://img.meituan.net/avatar/de1fd17ab7490ee050ff46a3e9938c44107518.jpg\",\"nick\":\"刮哥\",\"approve\":441,\"oppose\":0,\"reply\":7,\"score\":4.5,\"userId\":52565494,\"nickName\":\"刮哥\",\"vipInfo\":\"自媒体\",\"time\":\"2018-07-07 12:00\",\"id\":1027433186,\"content\":\"虐心，但走心，这是生活的真相。希望越来越好，向有社会责任感的制作团队致敬！\"}],\"cmts\":[{\"avatarurl\":\"https://img.meituan.net/avatar/116e4bc6392c60962bb029e8b45715d977591.jpg\",\"nick\":\"快乐每一天2385\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":454240658,\"nickName\":\"快乐每一天2385\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028356459,\"content\":\"最好影片太感人了\"},{\"avatarurl\":\"\",\"nick\":\"IvI151288352\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":3.5,\"userId\":1518471490,\"nickName\":\"IvI151288352\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028433469,\"content\":\"挺好挺好挺好挺好\"},{\"avatarurl\":\"\",\"nick\":\"钟情2075\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":1664262106,\"nickName\":\"钟情2075\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1027030133,\"content\":\"特别好。演出了很多人的心声\"},{\"avatarurl\":\"\",\"nick\":\"恒、Man\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":1668321147,\"nickName\":\"恒、Man\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028447089,\"content\":\"非常好的一部影片，让人感触很深，意犹未尽的感觉\"},{\"avatarurl\":\"\",\"nick\":\"Cmenfy\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":42184514,\"nickName\":\"Cmenfy\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028441273,\"content\":\"好看！真走心！\"}],\"total\":294069,\"hasNext\":true}}}";
        MovieInformation information = JSON.parseObject(result, MovieInformation.class);
        model.addAttribute("movie", information);
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        getFriend(model, userInformation.getId());
        return "/movie/hot_movie_information";
    }

    //北美票房榜
    @RequestMapping(value = "/us/box")
    public String us_box(Model model, HttpServletRequest request) {
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/us_box";
        HashMap<String, String> params = new HashMap<>();
        USbox allInformation = getUsBox(url, params, ENCODE, GET);
        model.addAttribute("movie", allInformation);
        model.addAttribute("movie_name", "北美票房");
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        getFriend(model, userInformation.getId());
        return "/movie/us_box";
    }

    //获得点赞数量，收藏数量，评论数量
    private void getUserCounts(Model model, int uid) {
        model.addAttribute("comments", commentCriticService.getUserCounts(uid));
        model.addAttribute("critics", publishCriticService.getUserCounts(uid));
        model.addAttribute("goods", goodCriticService.getUserCounts(uid));
        model.addAttribute("collections", collectionCriticService.getUserCounts(uid));
    }

    //获得电影信息
    private AllInformation getMovieInformation(String url, Map params, String encode, String action) {
        AllInformation information;
        try {
            //JSON.parseObject将从服务器端接收到的json字符串转化为相应的AllInformation对象
            //HttpUtils.submitPostData向服务器提交请求并返回结果
            information = JSON.parseObject(HttpUtils.submitPostData(url, params, encode, action), AllInformation.class);
        } catch (JSONException e) {
            e.printStackTrace();
            information = new AllInformation();
        } catch (Exception e) {
            e.printStackTrace();
            information = new AllInformation();
        }
        return information;
    }

    private USbox getUsBox(String url, Map params, String encode, String action) {
        USbox information;
        try {
            information = JSON.parseObject(HttpUtils.submitPostData(url, params, encode, action), USbox.class);
        } catch (JSONException e) {
            e.printStackTrace();
            information = new USbox();
        } catch (Exception e) {
            e.printStackTrace();
            information = new USbox();
        }
        return information;
    }

    private void getFriend(Model model, int uid) {
        List<MyFriends> list = myFriendsService.getFid(uid);
        List<MyFriendsBean> ids = new ArrayList<>();
        for (MyFriends myFriends : list) {
            UserInformation userInformation = userInformationService.selectByPrimaryKey(myFriends.getFid());
            MyFriendsBean myFriendsBean = new MyFriendsBean();
            myFriendsBean.setAvatar(userInformation.getAvatar());
            myFriendsBean.setFid(myFriends.getFid());
            myFriendsBean.setId(myFriends.getId());
            myFriendsBean.setUid(myFriends.getUid());
            myFriendsBean.setName(userInformation.getName());
            ids.add(myFriendsBean);
        }
        model.addAttribute("myFriends", ids);
    }
}
