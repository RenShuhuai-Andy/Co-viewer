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
    //药神
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
    //药神
    @RequestMapping(value = "/hot/movie/information/{id}")
    public String hotMovieInformation(@PathVariable String id, Model model, HttpServletRequest request) {
        UserInformation userInformation = (UserInformation) request.getSession().getAttribute("userInformation");
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:../login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = "http://m.maoyan.com/movie/" + id + ".json";
        String result = null;
        int aid=Integer.parseInt(id);
        if(aid==1200486) {
            result = "{\"control\":{\"expires\":3600},\"status\":0,\"data\":{\"MovieDetailModel\":{\"cat\":\"剧情,喜剧\",\"dealsum\":0,\"dir\":\"文牧野 \",\"dra\":\"\n" +
                    "一位不速之客的意外到访，打破了神油店老板程勇（徐峥 饰）的平凡人生，他从一个交不起房租的男性保健品商贩，一跃成为印度仿制药“格列宁”的独家代理商。收获巨额利润的他，生活剧烈变化，被病患们冠以“药神”的称号。但是，一场关于救赎的拉锯战也在波涛暗涌中慢慢展开......\n" +
                    "\n" +
                    "\",\"dur\":117,\"id\":1200486,\"imax\":true,\"img\":\"http://p0.meituan.net/165.220/movie/238e2dc36beae55a71cabfc14069fe78236351.jpg\",\"isShowing\":true,\"late\":false,\"mk\":0,\"nm\":\"我不是药神\",\"photos\":[\"http://p1.meituan.net/w.h/movie/36d1a14aca0666c9ca6ed1bd3031c7f1566914.jpg\",\"http://p0.meituan.net/w.h/movie/5d579bf80909cba285093db441766061324196.jpg\",\"http://p0.meituan.net/w.h/movie/7d6e1ba1234ea8b4134e43e8e549f17f308710.jpg\",\"http://p1.meituan.net/w.h/movie/ef24ce01dc72ef91366a698307cb45dd275718.jpg\",\"http://p0.meituan.net/w.h/movie/6ebfc1b2c0e9afb6dc3f8c561420e220378981.jpg\",\"http://p1.meituan.net/w.h/movie/cfb412ebb73704f1dc67c820155b70bc395466.jpg\",\"http://p1.meituan.net/w.h/movie/1198d01a448a10a9aaf16b51986f2db0468937.jpg\",\"http://p0.meituan.net/w.h/movie/2ef10db2b2967532def7cdc4e32c3b7f657105.jpg\",\"http://p0.meituan.net/w.h/movie/bde7f6f0001bcba6357b5828f20c09cd321357.jpg\",\"http://p1.meituan.net/w.h/movie/25b2dc81375700c7035c1e6fa9efd727286777.jpg\",\"http://p1.meituan.net/w.h/movie/0b5289a62f65696d240185fc3dec67d3331950.jpg\",\"http://p1.meituan.net/w.h/movie/c3ce3646a21ef5acaabae52a9f863040253328.jpg\",\"http://p0.meituan.net/w.h/movie/1c8f43cbaa6e71cce6b7471e588b21f1540645.jpg\",\"http://p1.meituan.net/w.h/movie/b4ef616d50039c284c2fdf539b834dab696225.jpg\",\"http://p0.meituan.net/w.h/movie/5cec8d43352f164f117fc6c126fc015a716757.jpg\",\"http://p1.meituan.net/w.h/movie/ca84ac86af126081fef629b545446d54749772.jpg\",\"http://p1.meituan.net/w.h/movie/7f8900ef3d618de9aa64af37aee099e5678963.jpg\",\"http://p1.meituan.net/w.h/movie/4b065728c8321a7c299932248317ab69746727.jpg\",\"http://p0.meituan.net/w.h/movie/55befe46945858d4cb31203ac6118f7b767588.jpg\",\"http://p1.meituan.net/w.h/movie/28b3055ea2efb82f7832cb615274e5d8562412.jpg\"],\"pn\":214,\"preSale\":0,\"rt\":\"2018-07-05上映\",\"sc\":9.7,\"scm\":\"印度代神药，良心及时现\",\"showSnum\":true,\"snum\":1073429,\"src\":\"中国大陆\",\"star\":\"徐峥 周一围 王传君 谭卓 章宇 杨新鸣 王砚辉 贾晨飞 王佳佳 李乃文 龚蓓苾 宁浩 苇青 邓飞 \",\"vd\":\"http://maoyan.meituan.net/movie/videos/854x4804c109134879943f4b24387adc040504b.mp4\",\"ver\":\"2D/IMAX 2D/中国巨幕\",\"vnum\":8,\"wish\":164738,\"wishst\":0},\"CommentResponseModel\":{\"hcmts\":[{\"avatarurl\":\"https://img.meituan.net/avatar/b07fe330b3b11713ff096390d60d3a9b7039.jpg\",\"nick\":\"乐Forwards\",\"approve\":14756,\"oppose\":0,\"reply\":1177,\"score\":5,\"userId\":65021517,\"nickName\":\"乐Forwards\",\"vipInfo\":\"\",\"time\":\"2018-07-05 15:22\",\"id\":1026981572,\"content\":\"我认为这是一部跟摔跤吧爸爸同层次的电影，忍到最后还是会流泪。生命很坚强也很脆弱，命与药的关系就是这么赤裸裸。（不良厂商做药设天价，没有良心，不合理，但是这在世界都是完全合法的，决定用这个药来牟取暴利还是济世度人是药商自己的权利，毕竟药是他们研发的。）之前刚看完在这里说不良厂商，还是刚看完有些不清醒了，其实不论他们的主观意愿是什么，都是他们的成果给了病人选择的希望。国家政府会把药纳入医保、发展制度、免除关税来降低病人的压力，我们个人也应该为诸如此类的研发事业做出贡献，比如为医学难题做出重大突破，等等各行各业，才能将未来将好东西做到平价( ¯ᒡ̱¯ )و\"},{\"avatarurl\":\"\",\"nick\":\"龟口逃生\",\"approve\":22170,\"oppose\":0,\"reply\":432,\"score\":5,\"userId\":242680147,\"nickName\":\"龟口逃生\",\"vipInfo\":\"\",\"time\":\"2018-07-05 01:55\",\"id\":1026929804,\"content\":\"没的说，今年最好影片，没有之一\"},{\"avatarurl\":\"https://img.meituan.net/avatar/590edf580b1b58c3e9dc5a8e22061a8e219481.jpg\",\"nick\":\"梦里梦惊梦丶\",\"approve\":12126,\"oppose\":0,\"reply\":298,\"score\":5,\"userId\":117559106,\"nickName\":\"梦里梦惊梦丶\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:19\",\"id\":1026930110,\"content\":\"反正我哭了。我妈妈身体也不好。也是药养着。但是现在都有医保啥的。条件也好了。但是身临其境的想下。眼泪就止不住了。很好的电影\"},{\"avatarurl\":\"https://img.meituan.net/avatar/6809ed6becc04e30e9ddc43cde501a8e70570.jpg\",\"nick\":\"jay20m\",\"approve\":5082,\"oppose\":0,\"reply\":348,\"score\":4.5,\"userId\":33330867,\"nickName\":\"jay20m\",\"vipInfo\":\"\",\"time\":\"2018-07-05 20:16\",\"id\":1026932031,\"content\":\"给九分是因为这片真实的说出了在中国就是吃天价药，发个烧二话不说先打针吊牌几百块就没了，更别说得了重病治疗了，穷人家根本看不起病，\\n法律不在乎人情，只会纵容坏人，好人都得不好报！！\"},{\"avatarurl\":\"\",\"nick\":\"Qgu743039906\",\"approve\":4422,\"oppose\":0,\"reply\":117,\"score\":5,\"userId\":142629978,\"nickName\":\"Qgu743039906\",\"vipInfo\":\"\",\"time\":\"2018-07-05 15:12\",\"id\":1026986429,\"content\":\"以前不管看什么电影，从来没有写过影评，但是这部电影看完以后真的是忍不住，就好像胸中憋了一团火要炸裂的感觉。从电影院出来以后眼泪一直挂着。良心说，里面的演员颜值不像其他国产电影，甚至于那个小黄毛看起来还有点丑。就连关谷神奇也变丑了。但是演技真的很完美，从写实的角度把故事叙述的刀刀插入人心。我和我的家人没有得过什么大病，但是换位思考，谁还能保证到死也不会进医院？换成如果我是故事里面的人物，我估计我应该会承受不住压力自己了断不去耽误家里人。毕竟我们的国家就是这样，穷人真的不能病也不敢病，就像现在微信朋友圈里隔三差五的就是水滴筹或则其他类似的信息。每次看到都是一阵心痛和后怕。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/2d7ec8a3f61513feae27a80fced1b2c5258971.jpg\",\"nick\":\"王狗狗77\",\"approve\":4105,\"oppose\":0,\"reply\":94,\"score\":5,\"userId\":41784496,\"nickName\":\"王狗狗77\",\"vipInfo\":\"\",\"time\":\"2018-07-09 23:14\",\"id\":1026930189,\"content\":\"据说是一部真实事情而改编的电影，一直觉得徐铮是一位优秀的好演员。\\n\\n此部电影反应了社会、现实、人性的一些问题，庆幸我们遇上了一个好年代，国家逐渐得以民心，生命漫长而又短暂，好好活着就是最大的奢侈，好好生活，保持善良。平凡并不可怕，拥有高贵的品格才是人最需要的东西，身处绝境也能绽放出最温暖的笑容。\\n\\n这部电影的题材与以往很多国产电影不同，我相信电影最后每一个落泪的人内心都有一份纯净。希望以后能多有这样类型的电影，传递正能量，去正视社会，也敲醒人性的真诚和理解。\\n \\n这个世界没有药神，也没有超级英雄，而是因为千千万万平凡如你我的小人物。\\n\\n良心剧... 推荐 ～ \uD83D\uDE42\uD83D\uDE42\uD83D\uDE42\"},{\"avatarurl\":\"https://img.meituan.net/avatar/f718dd93235b7aa2bc3afae2267ef7dc74370.jpg\",\"nick\":\"生来倔强F\",\"approve\":3416,\"oppose\":0,\"reply\":39,\"score\":5,\"userId\":252113942,\"nickName\":\"生来倔强F\",\"vipInfo\":\"\",\"time\":\"2018-07-05 01:59\",\"id\":1026931225,\"content\":\"不亏和老公凌晨赶场过来看，话不多说，真的值得一看。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/4e2974b8a70fd2e82f238244f5cf705e86873.jpg\",\"nick\":\"IZM619073352\",\"approve\":2287,\"oppose\":0,\"reply\":31,\"score\":5,\"userId\":581494255,\"nickName\":\"IZM619073352\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:01\",\"id\":1026931248,\"content\":\"赶了个首映！ \\n应该是中国近5年的良心剧了吧[强][强][强] 前面搞笑，后面真实…… 推荐推荐推荐，不好看你找我[嘿哈]\"},{\"avatarurl\":\"\",\"nick\":\"BUm277947052\",\"approve\":1575,\"oppose\":0,\"reply\":10,\"score\":5,\"userId\":1011262448,\"nickName\":\"BUm277947052\",\"vipInfo\":\"\",\"time\":\"2018-07-05 02:06\",\"id\":1026931291,\"content\":\"真的很赞 笑点泪点把控的很好\\n故事真实 今天去看爆满 前面笑一片 后面哭一片\"},{\"avatarurl\":\"https://img.meituan.net/avatar/de1fd17ab7490ee050ff46a3e9938c44107518.jpg\",\"nick\":\"刮哥\",\"approve\":441,\"oppose\":0,\"reply\":7,\"score\":4.5,\"userId\":52565494,\"nickName\":\"刮哥\",\"vipInfo\":\"自媒体\",\"time\":\"2018-07-07 12:00\",\"id\":1027433186,\"content\":\"虐心，但走心，这是生活的真相。希望越来越好，向有社会责任感的制作团队致敬！\"}],\"cmts\":[{\"avatarurl\":\"https://img.meituan.net/avatar/116e4bc6392c60962bb029e8b45715d977591.jpg\",\"nick\":\"快乐每一天2385\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":454240658,\"nickName\":\"快乐每一天2385\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028356459,\"content\":\"最好影片太感人了\"},{\"avatarurl\":\"\",\"nick\":\"IvI151288352\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":3.5,\"userId\":1518471490,\"nickName\":\"IvI151288352\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028433469,\"content\":\"挺好挺好挺好挺好\"},{\"avatarurl\":\"\",\"nick\":\"钟情2075\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":1664262106,\"nickName\":\"钟情2075\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1027030133,\"content\":\"特别好。演出了很多人的心声\"},{\"avatarurl\":\"\",\"nick\":\"恒、Man\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":1668321147,\"nickName\":\"恒、Man\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028447089,\"content\":\"非常好的一部影片，让人感触很深，意犹未尽的感觉\"},{\"avatarurl\":\"\",\"nick\":\"Cmenfy\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":42184514,\"nickName\":\"Cmenfy\",\"vipInfo\":\"\",\"time\":\"2018-07-10 23:29\",\"id\":1028441273,\"content\":\"好看！真走心！\"}],\"total\":294069,\"hasNext\":true}}}";
        }
        else if(aid==1198178){
            result ="{\"control\":{\"expires\":3600},\"status\":0,\"data\":{\"MovieDetailModel\":{\"cat\":\"动作,悬疑,冒险\",\"dealsum\":0,\"dir\":\"韩延 \",\"dra\":\"\n" +
                    "男主角郑开司（李易峰 饰）因被朋友欺骗而背负上数百万的债务，面对重病的母亲和痴心等待的青梅竹马，他决心登上“命运号”游轮，改变自己一事无成的人生。只要能在渡轮上的游戏中获胜，他就将有机会将债务一笔勾销，并给家人带来更好的生活。这场游戏看似简单，参与者只需以标着“石头，剪刀，布”的扑克为道具，赢取对手身上的星星标志。但游轮上的亡命徒们毫无底线的欺诈争夺，却让人性的自私与残酷暴露无遗，局中局、计中计，让游戏场最终沦为“动物世界”斗兽场。面对绝境的郑开司，能否坚守自我底线保持善良本性？能否凭借自己的智慧和坚韧摆脱困境？这是一场自我救赎的残酷游戏，多重考验也将接踵而至。\n" +
                    "\n" +
                    "\",\"dur\":132,\"id\":1198178,\"imax\":true,\"img\":\"http://p0.meituan.net/165.220/movie/c6382117be87ac04fcc81fa02df815944946929.jpg\",\"isShowing\":true,\"late\":false,\"mk\":0,\"nm\":\"动物世界\",\"photos\":[\"http://p0.meituan.net/w.h/movie/169adb761d489f7db8de7f98cf214b6b3079666.jpg\",\"http://p1.meituan.net/w.h/movie/cdf965086de77528b9a9827275fdd9742016860.jpg\",\"http://p0.meituan.net/w.h/movie/024dde27f7746c83ede889aa3c5ae33c2803601.jpg\",\"http://p1.meituan.net/w.h/movie/9ecf952c1b8c096dd38a34c7b6656ffc2395719.jpg\",\"http://p0.meituan.net/w.h/movie/9a729203170673725f520b30eb8f44643486208.jpg\",\"http://p0.meituan.net/w.h/movie/a52ef298e4d8b614e4f4b851f66dc1762533729.jpg\",\"http://p1.meituan.net/w.h/movie/bb632cb6229298e1f23086909313d44f391876.jpg\",\"http://p1.meituan.net/w.h/movie/283dfc4557b8d1273ff80cc7fc57df40433981.jpg\",\"http://p1.meituan.net/w.h/movie/f11c187d924e14566255450d0c60b7cb419395.jpg\",\"http://p1.meituan.net/w.h/movie/6f048b604fee0e640bb5d9cb247afd39565518.jpg\",\"http://p0.meituan.net/w.h/movie/4e24a1e608e0187a18c8e8a1842116ae400947.jpg\",\"http://p0.meituan.net/w.h/movie/930a13b8737c681e17a127a2cb1e5be01223368.jpg\",\"http://p0.meituan.net/w.h/movie/76d0260788413e2bdfd89c191078ffeb1846170.jpg\",\"http://p1.meituan.net/w.h/movie/cd669892f9cfbaf5dc83b8cd5d5d43e91050160.jpg\",\"http://p1.meituan.net/w.h/movie/40af325d6eb84b3e62ae1df29bbd2dc81776626.jpg\",\"http://p0.meituan.net/w.h/movie/e69157e6952098d016f41e599f4118121218560.jpg\",\"http://p1.meituan.net/w.h/movie/e09becec76c5b158c72f876c010a55c22166872.jpg\",\"http://p0.meituan.net/w.h/movie/2b3c6b56c1a3a36b6b733883b4ffeb631264513.jpg\",\"http://p0.meituan.net/w.h/movie/c9b5697ccdbf1f30cf7d1924307f597f1536341.jpg\",\"http://p0.meituan.net/w.h/movie/7b09236befbb93952730da1ef2342d791573737.jpg\"],\"pn\":407,\"preSale\":0,\"rt\":\"2018-06-29上映\",\"sc\":8.4,\"scm\":\"是考验人性还是自我救赎？\",\"showSnum\":true,\"snum\":361487,\"src\":\"中国大陆,美国\",\"star\":\"李易峰 迈克尔·道格拉斯 周冬雨 曹炳琨 王戈 迟嘉 张隽溢 李宜娟 苏可 雷汉 姚安濂 喜利图 阿尔贝托·兰切洛蒂 \",\"vd\":\"http://maoyan.meituan.net/movie/videos/854x480d8102fa1ee734a4ca402f8ac01e4a7aa.mp4\",\"ver\":\"2D/3D/IMAX 3D/中国巨幕/全景声\",\"vnum\":25,\"wish\":194088,\"wishst\":0},\"CommentResponseModel\":{\"hcmts\":[{\"avatarurl\":\"https://img.meituan.net/avatar/169558c38e6e54ca1a1462b3b2b973d2385588.jpg\",\"nick\":\"小猴子7789\",\"approve\":5132,\"oppose\":0,\"reply\":574,\"score\":5,\"userId\":191026995,\"nickName\":\"楼下小姑娘\",\"vipInfo\":\"\",\"time\":\"2018-06-29 11:12\",\"id\":1026085184,\"content\":\"好多年前就看过日漫赌博默示录真的非常非常喜欢。也看了真人版 当初电影版正所谓群星云集，但是放映后却不敬人意，开始并不知道这电影就是原作改编 名字也起的好奇怪 对于国产改编更是抱有质疑 但还是迫不及待开场就去看了 .....现在大荧幕上放着片尾曲，我给了满分，恩 没有失望更多的是惊喜 虽然有些地方不是看的很明白 但我还是给了满分 期待第二部\"},{\"avatarurl\":\"\",\"nick\":\"vPa987871604\",\"approve\":5370,\"oppose\":0,\"reply\":269,\"score\":5,\"userId\":74268746,\"nickName\":\"vPa987871604\",\"vipInfo\":\"\",\"time\":\"2018-06-29 11:27\",\"id\":1026085609,\"content\":\"国产神作，汗毛都竖起来了\"},{\"avatarurl\":\"https://img.meituan.net/avatar/ba28ac2bcf4741cc8129269ad7ce76a0111675.jpg\",\"nick\":\"爱比死冷酷\",\"approve\":946,\"oppose\":0,\"reply\":49,\"score\":4,\"userId\":65310423,\"nickName\":\"爱比死冷酷\",\"vipInfo\":\"影迷会会长\",\"time\":\"2018-07-01 21:34\",\"id\":1025249817,\"content\":\"＃动物世界＃ 我要说这是我见过最好的国产漫改（日漫）电影。\\n国内电影取名《动物世界》也比较贴合故事主题。原作没有女主角，电影版加上女主角显然是为了故事显得更柔和，虽然周冬雨其实可有可无，但是为了故事整体的受众，多一个角色也是可以理解的。一开始我很担心怎样来呈现故事原作中抽象的人物形象和喧嚣的心理描写，但是导演运用大量的蒙太奇剪辑手法和奇思的动画场景穿插达到了惊喜的效果，风格之迥异可以说在现在的国产电影中很少见。\\n电影对原作还原度相当高，除了加入周冬雨玛丽苏的情节和符合国情的医疗话题，其他部分和动画无二，特别李易峰还真的有点伊藤开司的眼神。只是絮絮叨叨有点想起城市之光\uD83D\uDE02\\n8.5分，期待续作\"},{\"avatarurl\":\"https://img.meituan.net/avatar/a58232a69eeec6720cba4d2044ca278f60544.jpg\",\"nick\":\"黎系星熙\",\"approve\":3981,\"oppose\":0,\"reply\":103,\"score\":4.5,\"userId\":1226962683,\"nickName\":\"黎系星熙\",\"vipInfo\":\"\",\"time\":\"2018-06-29 03:08\",\"id\":1026071664,\"content\":\"精彩，刺激，爽。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/1257fbb558c8934aecebd0610f8cd7b859472.jpg\",\"nick\":\"noe427568217\",\"approve\":2658,\"oppose\":0,\"reply\":70,\"score\":5,\"userId\":832475691,\"nickName\":\"方木\",\"vipInfo\":\"\",\"time\":\"2018-06-30 02:51\",\"id\":1026077836,\"content\":\"总体来说还是很不错的，真的值得一看。虽然电影比较长但是剧情真的很紧凑，完全没有让人放松的感觉。特效做的不错，李易峰在里面的突破也很大，他把角色还原的很好，能看到他的进步，看得出他很用心，从这部电影里也透露出了人性的善良和险恶，很有教育意义，以前看类似这种剧情的电影不太有感觉，也觉得石头剪刀布挺无聊的，但是从这部电影里觉得还是挺有意思的，有点费脑子，突然很佩服李易峰在里面记台词的能力。\uD83D\uDC4D尤其是李易峰在这里面有一种痞帅的感觉，周冬雨的演技也很棒。用心创作。看懂它真的需要聚精会神，因为它会带动人的思考，需要很强的逻辑思维能力。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/9447b38e7da8ce1ccdf34eacf99fd06c22021.jpg\",\"nick\":\"fymz\",\"approve\":976,\"oppose\":0,\"reply\":195,\"score\":3,\"userId\":413837755,\"nickName\":\"fymz\",\"vipInfo\":\"\",\"time\":\"2018-06-29 11:42\",\"id\":1026083818,\"content\":\"石头剪刀布的电影，真心不好看\"},{\"avatarurl\":\"https://img.meituan.net/avatar/9e30a4e24fbd008b367a29efc520f04011446.jpg\",\"nick\":\"楚軒\",\"approve\":823,\"oppose\":0,\"reply\":352,\"score\":2,\"userId\":33111618,\"nickName\":\"楚轩\",\"vipInfo\":\"\",\"time\":\"2018-06-29 11:47\",\"id\":1026083946,\"content\":\"非常非常一般，抄袭的赌博默示录，伊藤开司演得不到位，铺垫太长正戏太少，这波很亏\"},{\"avatarurl\":\"https://img.meituan.net/avatar/a957888a9707385b9d08fb70445d1a5a128119.jpg\",\"nick\":\"zl950802\",\"approve\":1929,\"oppose\":0,\"reply\":39,\"score\":5,\"userId\":139654423,\"nickName\":\"一只流氓兔\",\"vipInfo\":\"\",\"time\":\"2018-06-29 11:12\",\"id\":1026082405,\"content\":\"满昏 看的太爽了！\"},{\"avatarurl\":\"https://img.meituan.net/avatar/e6357da0044dff279db2b8623ffd471014667.jpg\",\"nick\":\"醉月夜朦胧115\",\"approve\":1439,\"oppose\":0,\"reply\":33,\"score\":4.5,\"userId\":172274333,\"nickName\":\"醉月夜朦胧\",\"vipInfo\":\"特邀作者\",\"time\":\"2018-06-29 13:20\",\"id\":1026103434,\"content\":\"影片脑洞大开，剧情有跌宕起伏，智商高度在线，特效超燃超嗨，李易峰超帅超带感！总之，这是一部出人意料观赏值高于期待值的电影。李易峰在影片里，固执、倔强、聪明、睿智、还有内心不变的善良，在这个动物世界里，李易峰是个真正的人！\"},{\"avatarurl\":\"https://img.meituan.net/avatar/3ddf19f924f65b1afa7d9e29ee97735686122.jpg\",\"nick\":\"楼闹闹1207\",\"approve\":88,\"oppose\":0,\"reply\":2,\"score\":3.5,\"userId\":225862523,\"nickName\":\"楼闹闹\",\"vipInfo\":\"猫爪团高级导师\",\"time\":\"2018-06-30 16:58\",\"id\":1026320758,\"content\":\"结尾男主出小黑屋的那个背景音，该走的路我都走过了～老子特别霸气。李易峰在这一部剧里格外帅气，刷新了我对他的认知。脑洞大开，还是那句俗话学好数理化，走遍天下都不怕。\"}],\"cmts\":[{\"avatarurl\":\"\",\"nick\":\"wuxian8989\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":34882893,\"nickName\":\"wuxian8989\",\"vipInfo\":\"\",\"time\":\"2018-07-11 10:02\",\"id\":1028467997,\"content\":\"打破了大陆电影的格局，揭露了人性的丑陋，剖析得很透彻，不过最终还是情感战胜了，让观众看到了一丝希望。\"},{\"avatarurl\":\"https://img.meituan.net/avatar/d2401250bf18e93223b104bce041b50f91187.jpg\",\"nick\":\"oBm927709522\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":52884289,\"nickName\":\"清风云袖\",\"vipInfo\":\"\",\"time\":\"2018-07-11 10:01\",\"id\":1028356990,\"content\":\"把人性贪婪的一面呈现得淋漓精致\"},{\"avatarurl\":\"https://img.meituan.net/avatar/e060186efc0c6a5f49972783378f12c914620.jpg\",\"nick\":\"酱油团的某某某\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":4,\"userId\":102943551,\"nickName\":\"酱油团的某某某\",\"vipInfo\":\"\",\"time\":\"2018-07-11 10:01\",\"id\":1028476247,\"content\":\"五毛钱的特效不是很好\"},{\"avatarurl\":\"\",\"nick\":\"qJV294237572\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":2.5,\"userId\":1010537955,\"nickName\":\"qJV294237572\",\"vipInfo\":\"\",\"time\":\"2018-07-11 10:01\",\"id\":1028480672,\"content\":\"爆米花电影！\"},{\"avatarurl\":\"https://img.meituan.net/avatar/affd83faeec6aaf51aa3a669e651d74224486.jpg\",\"nick\":\"至臻美悦\",\"approve\":0,\"oppose\":0,\"reply\":0,\"score\":5,\"userId\":683904695,\"nickName\":\"至臻美悦\",\"vipInfo\":\"\",\"time\":\"2018-07-11 10:00\",\"id\":1028480657,\"content\":\"非常棒 期待第二部～\"}],\"total\":101798,\"hasNext\":true}}}";
        }
        else if(aid==248566){
            result ="{\"control\":{\"expires\":3600},\"status\":0,\"data\":{\"MovieDetailModel\":{\"cat\":\"爱情,动作,冒险\",\"dealsum\":0,\"dir\":\"姜文 \",\"dra\":\"\n" +
                    "北洋年间，北京以北。习武少年李天然目睹师兄朱潜龙勾结日本特务根本一郎，杀害师父全家。李天然侥幸从枪下逃脱，被美国医生亨德勒救下。李天然伤愈后，赴美学医多年，并同时接受特工训练。1937年初，李天然突然受命回国。“七七事变”前夜，北平，这座国际间谍之城，华洋混杂，山头林立。每时每刻充满诱惑与杀机。一心复仇的李天然，并不知道自己被卷入了一场阴谋，亦搅乱了一盘棋局。彼时彼刻，如同李小龙闯进了谍都卡萨布兰卡。前朝武人蓝青峰，神秘莫测，与朱潜龙、根本一郎关系紧密，更与亨德勒情同手足。是敌是友？面目不清。随着中日危机不断升级，各方博弈愈演愈烈。多次为谎言蛊惑、错失时机的李天然，终于下定决心，在红颜帮助下开启复仇行动。且看负有国恨家仇且智勇双全之李天然，如何荡涤这摊污泥浊水！\n" +
                    "\n" +
                    "\",\"dur\":137,\"id\":248566,\"imax\":true,\"img\":\"http://p1.meituan.net/165.220/movie/8ea36c210973ef97616be7ca1f0d5fc0216565.jpg\",\"isShowing\":true,\"late\":false,\"mk\":0,\"nm\":\"邪不压正\",\"photos\":[\"http://p0.meituan.net/w.h/movie/c1d69e5be726c1866ab85c678b7cc190235025.jpg\",\"http://p0.meituan.net/w.h/movie/f46cf128f4b54efab07cb532fa1f82ec498256.jpg\",\"http://p1.meituan.net/w.h/movie/958e4743a6b03c5c45c6f64cf2c17c9e285134.jpg\",\"http://p0.meituan.net/w.h/movie/1ca4ecbf7d97ae87fb90cb6b15b6ba10332891.jpg\",\"http://p1.meituan.net/w.h/movie/e400202667bd6c4a217c259fe69c73ac424202.jpg\",\"http://p1.meituan.net/w.h/movie/de2aaa70e0c3a0598495471655bb4b6f299221.jpg\",\"http://p0.meituan.net/w.h/movie/b24c72aee238c714cebb7348151b7ba7303747.jpg\",\"http://p1.meituan.net/w.h/movie/ea1126b8069a228dc3891f47e4cbf524297842.jpg\",\"http://p0.meituan.net/w.h/movie/03bcd958c7f2930cc0a544b1f35dcc76313754.jpg\",\"http://p0.meituan.net/w.h/movie/432217f39f36f8a89e3b9e76002083e3259005.jpg\",\"http://p1.meituan.net/w.h/movie/9218339c94fec84db42762897c3b0bca723868.jpg\",\"http://p0.meituan.net/w.h/movie/32adf9afd9767de5ea2d03004e632a1a973290.jpg\",\"http://p1.meituan.net/w.h/movie/a700bfb70a1a0addfc8d98d078e6dee9701211.jpg\",\"http://p1.meituan.net/w.h/movie/d81376c5c5f8481f1515b6408912a6b6772845.jpg\",\"http://p0.meituan.net/w.h/movie/6698bd40d405a1f75ecf4a8c1477f768809022.jpg\",\"http://p0.meituan.net/w.h/movie/c0390016251b1b28684008a0b05578e41576044.png\",\"http://p0.meituan.net/w.h/movie/93233684fbf87d99618bf42b70e15873248568.jpg\",\"http://p0.meituan.net/w.h/movie/253c1b3a3b1f60a7a2fba4645aa0ddce316046.jpg\",\"http://p1.meituan.net/w.h/movie/10826c5cb89f3f2913f75f6f669e77da204374.jpg\",\"http://p1.meituan.net/w.h/movie/539678e1bfe7f913bdc158a237036dcb314597.jpg\"],\"pn\":208,\"preSale\":0,\"rt\":\"本周五上映\",\"sc\":0,\"scm\":\"姜文导演最新力作\",\"showSnum\":false,\"snum\":4878,\"src\":\"中国大陆\",\"star\":\"姜文 彭于晏 廖凡 周韵 许晴 泽田谦也 李梦 \",\"vd\":\"http://maoyan.meituan.net/movie/videos/854x4809c7ff3efedef48319a6204d9f02cea2b.mp4\",\"ver\":\"2D/IMAX 2D/中国巨幕/全景声\",\"vnum\":21,\"wish\":194153,\"wishst\":0},\"CommentResponseModel\":{\"cmts\":[{\"approve\":1,\"userId\":209323477,\"nickName\":\"LSL927291959\",\"score\":5,\"reply\":0,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-07-11 09:54\",\"avatarurl\":\"\",\"id\":1028481338,\"content\":\"必须去 映射崔 一定看 支持 崔加油 邪不压正 加油\",\"nick\":\"LSL927291959\"},{\"approve\":0,\"userId\":1843392887,\"nickName\":\"yKJ824238684\",\"score\":5,\"reply\":0,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-07-11 09:30\",\"avatarurl\":\"https://img.meituan.net/avatar/59083f39cfceeaca5962073dbeee29333094.jpg\",\"id\":1028474061,\"content\":\"正义永远压倒邪恶\",\"nick\":\"yKJ824238684\"},{\"approve\":0,\"userId\":527777584,\"nickName\":\"秃鹫601\",\"score\":5,\"reply\":0,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-07-11 09:22\",\"avatarurl\":\"https://img.meituan.net/avatar/6d872eac72441792a1440934dd10996e55062.jpg\",\"id\":1028479639,\"content\":\"硬硬道理的呀硬\",\"nick\":\"秃鹫601\"},{\"approve\":0,\"userId\":1865479619,\"nickName\":\"vXi806643870\",\"score\":3,\"reply\":0,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-07-11 09:07\",\"avatarurl\":\"https://img.meituan.net/avatar/f2552219db6cffc1aae6ead9ecfe3e8e3287.jpg\",\"id\":1028478499,\"content\":\"b bbybb\",\"nick\":\"vXi806643870\"},{\"approve\":0,\"userId\":1711060933,\"nickName\":\"张峻宁887\",\"score\":5,\"reply\":0,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-07-11 08:30\",\"avatarurl\":\"\",\"id\":1028467395,\"content\":\"真的太好看了\uD83D\uDE0A :\",\"nick\":\"张峻宁887\"}],\"hcmts\":[{\"approve\":1751,\"userId\":9693997,\"nickName\":\"CHAOSAY\",\"score\":5,\"reply\":117,\"vipInfo\":\"影迷会会长\",\"oppose\":0,\"time\":\"2015-12-28 21:10\",\"avatarurl\":\"https://img.meituan.net/avatar/53ca67f73715261e629f620fdb70724022626.jpg\",\"id\":45188279,\"content\":\"支持姜文！懂得人自然懂，不懂的人，你即使喊破喉咙，他也未必能懂！电影这个东西，就是这样的，所有人都可以看，但不是拍给所有人的！所以，产生两极分化的电影就是这么回事，追求表面文章的人会认为它是一部烂片，中途退场；而懂的人会想为什么会这样拍，用意何在？是不是导演把他真正想讲的东西藏了起来，而这个部分是不是就是导演真正想表达的东西！\",\"nick\":\"Jaycool\"},{\"approve\":750,\"userId\":93074505,\"nickName\":\"ykp672781258\",\"score\":5,\"reply\":209,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-03-25 00:32\",\"avatarurl\":\"https://img.meituan.net/avatar/aa26bcd29089c51af20b2d4daa48749025549.jpg\",\"id\":1015320688,\"content\":\"让子弹飞时候我自己看的，这个我要带我男朋友一起看，我还要在电影院里虐狗，我还要把这些年对单身的不满发泄出来，我要做电影院里最秀恩爱的那一对，有男朋友老好了！\",\"nick\":\"ykp672781258\"},{\"approve\":996,\"userId\":507766158,\"nickName\":\"无茕。\",\"score\":5,\"reply\":31,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-02-05 08:32\",\"avatarurl\":\"https://img.meituan.net/avatar/581fed5fb880457b2bee5ca89fc3fa21220972.jpg\",\"id\":138179810,\"content\":\"媽耶～大哥大嫂過年好啊～期待鬼才新作～\",\"nick\":\"无茕。\"},{\"approve\":140,\"userId\":48664290,\"nickName\":\"mlj1119\",\"score\":5,\"reply\":10,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2017-08-07 13:52\",\"avatarurl\":\"https://img.meituan.net/avatar/49ca40a8c4a711e8032e73df835bf0ab28792.jpg\",\"id\":117353711,\"content\":\"有彭于晏必看\",\"nick\":\"mlj1119\"},{\"approve\":143,\"userId\":263049289,\"nickName\":\"jAW472842642\",\"score\":5,\"reply\":3,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-01-03 02:03\",\"avatarurl\":\"https://img.meituan.net/avatar/5f2ea0caef265f22809db9d58070a3ce324771.jpg\",\"id\":134266500,\"content\":\"看完让子弹飞 给姜导跪了……\\n然后去看了太阳照常升起 姜导 对不起我跪早了……\\n最后看脸鬼子来了 姜导 我再跪一个......\\n表演专业学生的偶像！\",\"nick\":\"jAW472842642\"},{\"approve\":122,\"userId\":236840707,\"nickName\":\"起司猫lulu\",\"score\":5,\"reply\":3,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-02-05 13:48\",\"avatarurl\":\"https://img.meituan.net/avatar/0b0b43921b6df70992db45e0fd14f5d3158851.jpg\",\"id\":138201071,\"content\":\"期待，彭于晏终于跟他最喜欢的大导演合作了，好的演员+好的导演，期待成片。\",\"nick\":\"_qqx721437797324\"},{\"approve\":314,\"userId\":28170917,\"nickName\":\"黄英551\",\"score\":5,\"reply\":3,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2017-03-13 21:44\",\"avatarurl\":\"\",\"id\":99685860,\"content\":\"真神奇，看到彭于晏我就会自觉点赞\",\"nick\":\"黄英551\"},{\"approve\":391,\"userId\":118592833,\"nickName\":\"淡定的派\",\"score\":5,\"reply\":3,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-02-05 08:27\",\"avatarurl\":\"https://img.meituan.net/avatar/731dec7a877de30b8f0ea102bac595b7123172.jpg\",\"id\":138179598,\"content\":\"终于爆出预告了，炸裂呀！姜文回来了！\",\"nick\":\"、疯在青春的风～\"},{\"approve\":76,\"userId\":6019133,\"nickName\":\"jpfking\",\"score\":4.5,\"reply\":4,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-02-05 15:21\",\"avatarurl\":\"https://img.meituan.net/avatar/b78828b9394b7e96ed5dfc7af76b3d8230263.jpg\",\"id\":138207498,\"content\":\"先9分预约吧，差一分是因为上部一步之遥，给九分是因为上上部让子弹飞\uD83E\uDD14\",\"nick\":\"jpfking\"},{\"approve\":122,\"userId\":93158927,\"nickName\":\"空白van\",\"score\":5,\"reply\":1,\"vipInfo\":\"\",\"oppose\":0,\"time\":\"2018-02-08 10:34\",\"avatarurl\":\"\",\"id\":138434465,\"content\":\"难道彭于晏我就觉得整个电影完美了10分♥\",\"nick\":\"空白van\"}],\"total\":695,\"hasNext\":true}}}";
        }
        else if(aid==346096){
            result ="{\"control\":{\"expires\":3600},\"status\":0,\"data\":{\"MovieDetailModel\":{\"cat\":\"爱情,动作,奇幻\",\"dealsum\":0,\"dir\":\"张鹏 \",\"dra\":\"\n" +
                    "数百年前，长有三头的阿修罗王（梁家辉饰欲望之头、刘嘉玲饰谋略之头）决定发起毁灭天界之战，改变以善恶来决定生命轮转的自然法则。阿修罗王战败，失去洞察之头（吴磊 饰），被打入炼狱界。百年后，阿修罗王欲再次发起攻天大战，但若要打开天界之门，他必须要找到洞察之头，并与之合体方能成功。人界山村，牧羊少年如意（吴磊 饰）突然被阿修罗王寻到，恍惚之中被带入阿修罗界。阿修罗王宫中，如意受到王者一般的礼遇，体验着各种前所未有的欲望。当他看到阿修罗王肆意摧残生命，并得知自己就是洞察之头的转世，要与阿修罗王合体毁灭天界时，他愤怒地逃离王宫。美丽善良的阿修罗叛军女首领华蕊（张艺上 饰）遵师所愿，守卫六界。她的出现改变了如意的命运，他在华蕊身上发现了一生真正的渴望——爱！在爱的激励下，如意找到了至善的力量，一举消灭了阿修罗王，挽救整个六界，成为新一代的阿修罗王，当世间一切欲望都能供他驱使之际，他却作出惊天之举……\n" +
                    "\n" +
                    "\",\"dur\":141,\"id\":346096,\"imax\":false,\"img\":\"http://p1.meituan.net/165.220/movie/cdebee8c9cd0f220576f47d5a816479c678915.jpg\",\"isShowing\":true,\"late\":false,\"mk\":0,\"nm\":\"阿修罗\",\"photos\":[\"http://p1.meituan.net/w.h/movie/b59cb87beea63a9786cd6017749ef4ae1301886.jpg\",\"http://p0.meituan.net/w.h/movie/4cd35c46c7b93dc8d6df790a2c11d21b1094663.jpg\",\"http://p0.meituan.net/w.h/movie/e625bb6af9345a79e9eb219aee9f41111366909.jpg\",\"http://p1.meituan.net/w.h/movie/5c3d4d571b77c69b7e565ca0bddede351172438.jpg\",\"http://p0.meituan.net/w.h/movie/eff7e2c576e56e00e39ed953491b76dd1336832.jpg\",\"http://p0.meituan.net/w.h/movie/a3af865d776f694ab5c9b808876907e5688876.jpg\",\"http://p1.meituan.net/w.h/movie/8e158b6c4c96bc6d3a3419b02330f851799355.jpg\",\"http://p0.meituan.net/w.h/movie/4e0f7be6f322f62cc79ac21bc5d9f6de755223.jpg\",\"http://p1.meituan.net/w.h/movie/3814c8a08d8cd11554e4044f44479b711263873.jpg\",\"http://p1.meituan.net/w.h/movie/10a58447096a0feb830763c4603fd114742120.jpg\",\"http://p0.meituan.net/w.h/movie/743cb71520089db1c6ad33fb39b26cb3643497.jpg\",\"http://p1.meituan.net/w.h/movie/358118e694c8134401656b368a1e7850676099.jpg\",\"http://p1.meituan.net/w.h/movie/d68491e6bb1671b91339dce642dd54f6849085.jpg\",\"http://p1.meituan.net/w.h/movie/47d80792889ac1e5c488e7a0f25b26f91541680.jpg\",\"http://p0.meituan.net/w.h/movie/2adb0d75ec8dc47a1ec47b83bb6b01901473078.jpg\",\"http://p1.meituan.net/w.h/movie/6bfcc19c853345e97466800fcabf14451002421.jpg\",\"http://p0.meituan.net/w.h/movie/3774a1773db9f628605de1aa7600a1821060003.jpg\",\"http://p1.meituan.net/w.h/movie/27bc54654ac378fe1e15d1e933e66abf1220524.jpg\",\"http://p1.meituan.net/w.h/movie/8ffaf5cac3577b72398928363b2afa31904923.jpg\",\"http://p1.meituan.net/w.h/movie/bd483c7170340d19fc55611c21c6c800859191.jpg\"],\"pn\":318,\"preSale\":0,\"rt\":\"本周五上映\",\"sc\":0,\"scm\":\"六道轮回中，万事皆为空\",\"showSnum\":false,\"snum\":11814,\"src\":\"中国大陆\",\"star\":\"吴磊 梁家辉 刘嘉玲 张艺上 明道 冯嘉怡 董琦 多布杰 图卡 \",\"vd\":\"http://maoyan.meituan.net/movie/videos/854x480fb448fe5dce04a66aba5c819c2caa9b1.mp4\",\"ver\":\"2D/3D/中国巨幕\",\"vnum\":18,\"wish\":159802,\"wishst\":0},\"CommentResponseModel\":{\"hcmts\":[{\"approve\":1199,\"oppose\":0,\"nick\":\"梦想者的骄傲\",\"reply\":97,\"avatarurl\":\"https://img.meituan.net/avatar/6643a0cf4c2f422684cc85d8dce60cb393203.jpg\",\"score\":4,\"vipInfo\":\"\",\"nickName\":\"聂回来\",\"userId\":130960775,\"time\":\"2018-06-10 09:33\",\"id\":1024245394,\"content\":\"看了这特效真不敢相信是国产片现在也开始走大片路线了吗\"},{\"approve\":1028,\"oppose\":0,\"nick\":\"fAO74381359\",\"reply\":47,\"avatarurl\":\"https://img.meituan.net/avatar/1aaf917747e9eb0f1c1005246a2535c1131260.jpg\",\"score\":4.5,\"vipInfo\":\"\",\"nickName\":\"末生\",\"userId\":92311322,\"time\":\"2018-06-09 21:14\",\"id\":1024217035,\"content\":\"居然有吴磊耶，好棒，冲着他一定要去看，感觉特效也不错的样子，很期待！\"},{\"approve\":853,\"oppose\":0,\"nick\":\"爱嘟嘟的小熊\",\"reply\":41,\"avatarurl\":\"https://img.meituan.net/avatar/737b2faff6e4e572b8a2ee5218d28de254807.jpg\",\"score\":4,\"vipInfo\":\"\",\"nickName\":\"帅气的布朗熊\",\"userId\":829175310,\"time\":\"2018-06-09 23:28\",\"id\":1024140297,\"content\":\"TVB的明星，很喜欢，梁家辉刘嘉玲都是实力派，支持\"},{\"approve\":652,\"oppose\":0,\"nick\":\"NRx751786038\",\"reply\":31,\"avatarurl\":\"https://img.meituan.net/avatar/b2e818f7c716babe439a848bbdf48060149542.jpg\",\"score\":4,\"vipInfo\":\"\",\"nickName\":\"为我自己代言\",\"userId\":1730051489,\"time\":\"2018-06-09 21:17\",\"id\":1024220156,\"content\":\"最喜欢看的电影的特效,挺震撼的，感觉还是蛮好看的。\"},{\"approve\":662,\"oppose\":0,\"nick\":\"MNx811308401\",\"reply\":31,\"avatarurl\":\"https://img.meituan.net/avatar/c4b1c0d6b3e49a50a7e8a3e666b842d2102213.jpg\",\"score\":4,\"vipInfo\":\"\",\"nickName\":\"一岁OK\",\"userId\":871417535,\"time\":\"2018-06-10 09:28\",\"id\":1024246291,\"content\":\"很不错的电影，情节跌宕起伏，草蛇灰线延绵千里\"},{\"approve\":659,\"oppose\":0,\"nick\":\"LHi934700575\",\"reply\":30,\"avatarurl\":\"https://img.meituan.net/avatar/683e6c52e3d1334daac68da7789a516b123245.jpg\",\"score\":4,\"vipInfo\":\"\",\"nickName\":\"电热水杯具\",\"userId\":1721689080,\"time\":\"2018-06-09 21:31\",\"id\":1024217328,\"content\":\"片子画面质地像动画片的感觉充满玄幻令人目不暇接\"},{\"approve\":633,\"oppose\":0,\"nick\":\"NQr822641016\",\"reply\":30,\"avatarurl\":\"https://img.meituan.net/avatar/0ab1b1b4ae8463235bd731d550069dfb98294.jpg\",\"score\":4.5,\"vipInfo\":\"\",\"nickName\":\"恒星\",\"userId\":918099692,\"time\":\"2018-06-11 15:07\",\"id\":1024233984,\"content\":\"很棒棒的电影、剧情很好，很喜欢看，\"},{\"approve\":670,\"oppose\":0,\"nick\":\"刘兴许下德愿望\",\"reply\":29,\"avatarurl\":\"https://img.meituan.net/avatar/ca86c3fe588b6d7e8a99e3c651931b5764567.jpg\",\"score\":4.5,\"vipInfo\":\"\",\"nickName\":\"许先生\",\"userId\":1304391879,\"time\":\"2018-06-09 21:45\",\"id\":1024220959,\"content\":\"故事本身传递的精神是很正面的，即使前面的路不满荆棘，也会为了信念而战斗，吴磊的如意演得很传神了。\"},{\"approve\":647,\"oppose\":0,\"nick\":\"tZG589735149\",\"reply\":29,\"avatarurl\":\"https://img.meituan.net/avatar/41bbb2c069e4386e0a019b9a7cb232a513808.jpg\",\"score\":5,\"vipInfo\":\"\",\"nickName\":\"aaaintersting\",\"userId\":968671785,\"time\":\"2018-07-05 12:32\",\"id\":1024245556,\"content\":\"很喜欢的明星阵容，超级喜欢明道的 ，演技超级棒，绝对支持！！\"},{\"approve\":661,\"oppose\":0,\"nick\":\"myp473702084\",\"reply\":29,\"avatarurl\":\"https://img.meituan.net/avatar/67efc30f3cece17af3f0e677930ca5a427205.jpg\",\"score\":4,\"vipInfo\":\"\",\"nickName\":\"花去\",\"userId\":1626393696,\"time\":\"2018-06-10 09:37\",\"id\":1024249054,\"content\":\"吴磊的电影当然要支持啦！希望票房大卖！\"}],\"cmts\":[{\"approve\":1,\"oppose\":0,\"nick\":\"yvL641268975\",\"reply\":0,\"avatarurl\":\"https://img.meituan.net/avatar/949bedab6e44fe69b29c32b50040e58222133.jpg\",\"score\":5,\"vipInfo\":\"\",\"nickName\":\"子非鱼\",\"userId\":1628567096,\"time\":\"2018-07-11 10:04\",\"id\":1028481562,\"content\":\"画面真的超级赞的，而且内容也可圈可点，给十分\"},{\"approve\":0,\"oppose\":0,\"nick\":\"ZpU755541686\",\"reply\":0,\"avatarurl\":\"\",\"score\":2.5,\"vipInfo\":\"\",\"nickName\":\"ZpU755541686\",\"userId\":776327025,\"time\":\"2018-07-11 10:04\",\"id\":1028480725,\"content\":\"还没上映呢，一个个评论得有鼻子有眼的。\"},{\"approve\":0,\"oppose\":0,\"nick\":\"MHl428862852\",\"reply\":0,\"avatarurl\":\"https://img.meituan.net/avatar/9b53333113ba1c27d2417296400fd93d16178.jpg\",\"score\":5,\"vipInfo\":\"\",\"nickName\":\"诀别诗\",\"userId\":1628565814,\"time\":\"2018-07-11 10:03\",\"id\":1028481541,\"content\":\"电影真的给十分，五分特效五分演员，非常好看\"},{\"approve\":0,\"oppose\":0,\"nick\":\"mKk734754901\",\"reply\":0,\"avatarurl\":\"https://img.meituan.net/avatar/f902ffe53445556316898602373d39dc19135.jpg\",\"score\":4.5,\"vipInfo\":\"\",\"nickName\":\"风催夜归人\",\"userId\":1628564508,\"time\":\"2018-07-11 10:03\",\"id\":1028481531,\"content\":\"电影中很多镜头都非常震撼！太好看了\"},{\"approve\":0,\"oppose\":0,\"nick\":\"OQB678197318\",\"reply\":0,\"avatarurl\":\"https://img.meituan.net/avatar/6737bba1d2d3469f2f98f201ead47f4728754.jpg\",\"score\":4.5,\"vipInfo\":\"\",\"nickName\":\"绯色天空\",\"userId\":1628562423,\"time\":\"2018-07-11 10:02\",\"id\":1028480690,\"content\":\"内容真的很有深度的，希望大家去看一下\"}],\"total\":7200,\"hasNext\":true}}}";
        }
        else{
            result="{\"control\":{\"expires\":3600},\"status\":0,\"data\":{\"MovieDetailModel\":{\"cat\":\"动画,冒险,奇幻\",\"dealsum\":0,\"dir\":\"何澄 \",\"dra\":\"\n" +
                    "小头爸爸带全家去俄罗斯出差，大头儿子在俄罗斯发现了自己画的白夜城是真实存在的，一家人半路遇到交通事故，被迫夜宿小旅馆，第二天醒来发现身处大头儿子画中的世界——白夜城。他们发现如果不解决白夜城中的危机便无法走出这个世界，一家人在白夜城中经历了重重困难终于回到了现实世界。\n" +
                    "\n" +
                    "\",\"dur\":80,\"id\":1220713,\"imax\":false,\"img\":\"http://p1.meituan.net/165.220/movie/517fd5611a22ea9b498fb2dac3dcd1461033977.jpg\",\"isShowing\":true,\"late\":false,\"mk\":0,\"nm\":\"新大头儿子和小头爸爸3：俄罗斯奇遇记\",\"photos\":[\"http://p1.meituan.net/w.h/movie/dfcbcd527242ff4392974f8b28580f021059451.jpg\",\"http://p1.meituan.net/w.h/movie/f94f14cf3cb3841849942c8989112cbb1226851.jpg\",\"http://p0.meituan.net/w.h/movie/84141d02da3a18678c5a0e14325f74ee1162969.jpg\",\"http://p1.meituan.net/w.h/movie/51d1413cef358d748a72e2598491cddd642679.jpg\",\"http://p1.meituan.net/w.h/movie/1b092a7887c274de5e7e601f073652b0320209.jpg\",\"http://p0.meituan.net/w.h/movie/69d1a1fd2b9acaae9b4de263e8606904809971.jpg\",\"http://p1.meituan.net/w.h/movie/b161747d7b14096d63c13e48ad9f057c1100412.jpg\",\"http://p0.meituan.net/w.h/movie/d38f1cf8656efd25674b5dcd1479359a910580.jpg\",\"http://p1.meituan.net/w.h/movie/b6bb923c1e558604b3ddb0cc91745c801002482.jpg\",\"http://p0.meituan.net/w.h/movie/587a4f155c2a2dc3b1188e00ed8dac05669993.png\",\"http://p1.meituan.net/w.h/movie/99a5774c01e712ca14542bfea78986351067690.jpg\",\"http://p0.meituan.net/w.h/movie/a12391ed10cd1fbfc0e31bc5079c359e956733.jpg\",\"http://p0.meituan.net/w.h/movie/e2c173099a8758e5a115a6ed9909c5821105333.jpg\",\"http://p1.meituan.net/w.h/movie/7e09e436709cf0ca0f901e9b7d4e5038793539.jpg\",\"http://p1.meituan.net/w.h/movie/c9d8ecc03dde42f6c37cda0d34be97371394940.jpg\",\"http://p1.meituan.net/w.h/movie/f2ebd6f3f5874bd4f102532fef9064c71371007.jpg\",\"http://p1.meituan.net/w.h/movie/837ce8651ebd4f18fdb3eaa30356d3cc602332.jpg\",\"http://p0.meituan.net/w.h/movie/264a768d9376e84892b71cc3ae6b9990833451.jpg\",\"http://p0.meituan.net/w.h/movie/17a79aa2eddf846d4594c6e2041cf3aa1436202.jpg\",\"http://p1.meituan.net/w.h/movie/f91d0458fc8119d0a7714b61e21fedc21305434.jpg\"],\"pn\":97,\"preSale\":0,\"rt\":\"2018-07-06上映\",\"sc\":8.5,\"scm\":\"大头儿子小头爸爸如何摆脱白夜城\",\"showSnum\":true,\"snum\":37260,\"src\":\"中国大陆\",\"star\":\"刘纯燕 董浩 鞠萍 红果果 陈苏 耿晨晨 黄炜 \",\"vd\":\"http://maoyan.meituan.net/movie/videos/854x480b549a67b042149f5bf88205bf7a2b20b.mp4\",\"ver\":\"2D\",\"vnum\":8,\"wish\":47729,\"wishst\":0},\"CommentResponseModel\":{\"hcmts\":[{\"approve\":345,\"oppose\":0,\"reply\":38,\"avatarurl\":\"https://img.meituan.net/avatar/4ebbc23432511ce1642169b69d6c106931465.jpg\",\"nick\":\"Afg663477143\",\"userId\":285049286,\"vipInfo\":\"\",\"nickName\":\"Afg663477143\",\"score\":4.5,\"time\":\"2018-07-06 21:58\",\"id\":1027316327,\"content\":\"去打了大头儿子的卡，不得不说槽点很多。电影是动画片，更是儿童片，坐在一堆孩子中间感触更深。\\n在车祸发生的时候片子竟然看出了鬼片的氛围，有小孩直接扑到妈妈的怀里说妈妈我害怕！\\n不过这显然不是儿童看的那种欢乐片，更加偏向家长，偏向成年人。\\n电影里渲染的焦躁和压力的氛围非常好，同样也是这些吓到了孩子。渲染孩子们天真烂漫的画面也很有童年的氛围，很棒。这样鲜明的对比就突出了孩子与家长的矛盾，是为了呼吁现在浮躁忙碌的家长们多陪陪孩子，多用心去了解孩子们的世界，尊重他们的想法。\\n怎么说呢，感觉更像是对家长的教育片吧。我觉得还是很值得让家长们去看看的，守护孩子们天真的世界吧。导演用心良苦啊，感谢，大家辛苦了。\"},{\"approve\":202,\"oppose\":0,\"reply\":4,\"avatarurl\":\"\",\"nick\":\"HdM462313134\",\"userId\":1796765728,\"vipInfo\":\"\",\"nickName\":\"HdM462313134\",\"score\":4.5,\"time\":\"2018-07-06 10:36\",\"id\":1027165251,\"content\":\"看完想去俄罗斯旅行了\"},{\"approve\":42,\"oppose\":0,\"reply\":0,\"avatarurl\":\"\",\"nick\":\"ilZ184243791\",\"userId\":1796962874,\"vipInfo\":\"\",\"nickName\":\"ilZ184243791\",\"score\":4.5,\"time\":\"2018-07-06 09:54\",\"id\":1027151519,\"content\":\"90后老阿姨表示很喜欢里边的小恐龙\"},{\"approve\":138,\"oppose\":0,\"reply\":8,\"avatarurl\":\"https://img.meituan.net/avatar/6899eadb52d65346809e0b419e33ab9b17162.jpg\",\"nick\":\"波妞是个小淘气\",\"userId\":758930623,\"vipInfo\":\"\",\"nickName\":\"波妞是个小淘气\",\"score\":4,\"time\":\"2018-07-07 01:09\",\"id\":1027384311,\"content\":\"孩子喜欢这个系列的，陪他看，一转头，发现他看哭了!有这么感动咩？2333\"},{\"approve\":75,\"oppose\":0,\"reply\":6,\"avatarurl\":\"\",\"nick\":\"haiyang279896973\",\"userId\":16345706,\"vipInfo\":\"\",\"nickName\":\"haiyang279896973\",\"score\":0.5,\"time\":\"2018-07-07 02:27\",\"id\":1027395015,\"content\":\"真不知道为什么一个动画片拍的跟个鬼片一样，崽崽在电影院虽然没哭，但是跟我说很怕，后面打怪兽那节整个电影院都是小朋友们的哭声一片，我心里还在想我崽还行，没哭！一回来，一晚上没睡，一直喊着妈妈我有点怕，要抱抱，然后抱了一晚上没睡着！我都要骂娘了，小孩子的动画片就不能考虑下小朋友的接受范围吗？！！冲着动画片去看的，这下好了，看成了恐怖片的效果！！\"},{\"approve\":63,\"oppose\":0,\"reply\":9,\"avatarurl\":\"https://img.meituan.net/avatar/091a934527729861c1a15f2f818d427325158.jpg\",\"nick\":\"shawnxj\",\"userId\":43353687,\"vipInfo\":\"\",\"nickName\":\"shawnxj\",\"score\":0.5,\"time\":\"2018-07-06 13:51\",\"id\":1027193355,\"content\":\"影片本身还不错，但是影院给我的感觉糟透了。可能是黄梅季节天气潮湿，座椅全部又脏又油还发霉了，影片10:40开始放映，居然不熄灯的，一直到11:19分才熄灯。。。这样的环境和服务是不是应该改进呢\"},{\"approve\":32,\"oppose\":0,\"reply\":2,\"avatarurl\":\"https://img.meituan.net/avatar/542282b5a65f8764b0a0a825c7ba39b012481.jpg\",\"nick\":\"alrrrrrr\",\"userId\":183679401,\"vipInfo\":\"\",\"nickName\":\"alrrrrrr\",\"score\":5,\"time\":\"2018-07-07 08:42\",\"id\":1027404043,\"content\":\"本来是被迫陪弟弟去看的，出乎意料的好看。适合小朋友看，希望每个小朋友心中都能有属于自己的白夜城，无忧无虑，快乐的成长；亦适合成人看，快节奏的生活，各方面的压力，使我们忧虑，焦躁，忘记了自己也曾是少年。在影片中一方面可以找到自己儿时的影子，另一方面又仿佛回到了过去。听着片场小孩儿们开心的笑声，自己也会被感染，不自觉的跟着笑。不错，很棒的影片！\"},{\"approve\":30,\"oppose\":0,\"reply\":2,\"avatarurl\":\"https://img.meituan.net/avatar/7ff2cc4380b7e6312f8f0ad0c36580b312237.jpg\",\"nick\":\"响风铃_51\",\"userId\":943298824,\"vipInfo\":\"\",\"nickName\":\"响风铃_51\",\"score\":4.5,\"time\":\"2018-07-07 00:06\",\"id\":1027368559,\"content\":\"带着心里苍老的小孩观影，很轻松很惬意哟！原先想看我不是药神来着，看了筒介放弃了，害怕自己无止境想念我的父母呀~有个包袱，因为坐最后一排，一个粑粑安排好小朋友，就躺下呼呼而睡，那呼声。做父母的正不容易呀，就是觉得电影时长太短了。\"},{\"approve\":29,\"oppose\":0,\"reply\":0,\"avatarurl\":\"\",\"nick\":\"NTR665428748\",\"userId\":1796885396,\"vipInfo\":\"\",\"nickName\":\"NTR665428748\",\"score\":4.5,\"time\":\"2018-07-06 09:59\",\"id\":1027159430,\"content\":\"简直就是童年的记忆了！小时候是dvd，现在是大屏幕，感触颇多！\"},{\"approve\":3,\"oppose\":0,\"reply\":0,\"avatarurl\":\"https://img.meituan.net/avatar/901eaee2c9f529cbe649ddab3949e55222700.jpg\",\"nick\":\"孑小羊习\",\"userId\":1450497145,\"vipInfo\":\"\",\"nickName\":\"孑小羊习\",\"score\":4,\"time\":\"2018-07-10 16:44\",\"id\":1028350640,\"content\":\"我觉得这场电影应该是胜在画面/画质吧，剧情我不是很感兴趣。开场第一段的云彩非常漂亮了，白夜城那段看了也的确是很向往（超级想在大树校工头上躺着睡顺便用木棒敲一遍套娃的头！）可以说是很可爱的一部电影，整部电影很色彩缤纷了，很容易让人喜欢上。\\n但是！还没开场就是几分钟的广告，现在的电影都这样吗？？里面有很多不符合逻辑的情节（像围裙妈妈瞬间位移什么的）大多数孩子都看不出来，也没有什么不对…我最想建议的是开头两孩子吃糖时棉花糖的表情和拼完图给爸爸递剑时围裙妈妈的表情能不能改一改，这俩表情……emmm\\n总体来说还是不错的，虽然我去的那场人不多，但是场内小孩子奶声奶气的讨论声真是让人身心愉悦！（手动比心）\"}],\"cmts\":[{\"approve\":0,\"oppose\":0,\"reply\":0,\"avatarurl\":\"\",\"nick\":\"yy7783\",\"userId\":80362024,\"vipInfo\":\"\",\"nickName\":\"yy7783\",\"score\":5,\"time\":\"2018-07-11 09:51\",\"id\":1028481150,\"content\":\"十分更完美。\"},{\"approve\":0,\"oppose\":0,\"reply\":0,\"avatarurl\":\"\",\"nick\":\"nXW36046070\",\"userId\":204058624,\"vipInfo\":\"\",\"nickName\":\"nXW36046070\",\"score\":3.5,\"time\":\"2018-07-11 09:50\",\"id\":1028475196,\"content\":\"感觉一般般吧 不过小孩子看的还挺开心 特效也不是特别好毕竟是2d？\"},{\"approve\":0,\"oppose\":0,\"reply\":0,\"avatarurl\":\"\",\"nick\":\"_weixin490074355\",\"userId\":260248463,\"vipInfo\":\"\",\"nickName\":\"_weixin490074355\",\"score\":4,\"time\":\"2018-07-11 09:50\",\"id\":1028480436,\"content\":\"女儿喜欢看。\"},{\"approve\":0,\"oppose\":0,\"reply\":0,\"avatarurl\":\"https://img.meituan.net/avatar/1a949932ccd9dd535aa7e1a3608cc01b10879.jpg\",\"nick\":\"且行且歌LQ\",\"userId\":50113416,\"vipInfo\":\"\",\"nickName\":\"且行且歌LQ\",\"score\":4.5,\"time\":\"2018-07-11 09:48\",\"id\":1028476088,\"content\":\"我觉得还行，但孩子看睡着了，哈哈，估计是看不懂吧\"},{\"approve\":0,\"oppose\":0,\"reply\":0,\"avatarurl\":\"https://img.meituan.net/avatar/1ff633745b68330e865055adcdcaf5d571904.jpg\",\"nick\":\"jco287531308\",\"userId\":126016741,\"vipInfo\":\"\",\"nickName\":\"影迷天下\",\"score\":4,\"time\":\"2018-07-11 09:47\",\"id\":1028481196,\"content\":\"还行，孩子说好看。\"}],\"total\":7415,\"hasNext\":true}}}";
        }
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
