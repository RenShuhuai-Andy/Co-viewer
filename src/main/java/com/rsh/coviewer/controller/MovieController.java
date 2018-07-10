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
        String result = HttpUtils.maoyan(url);
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
        String result = HttpUtils.maoyan(url);
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
        String result = HttpUtils.maoyan(url);
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
        String result = HttpUtils.maoyan(url);
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
