package nju.software.controller;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import nju.software.dao.IUserDao;
import nju.software.model.Status;
import nju.software.model.User;
import nju.software.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class UserController {
    private final Logger logger = LogManager.getLogger();
    // 可以对类成员变量、方法及构造函数进行标注，完成自动装配的工作。
    // 此处设置required=false，可避免在应用上下文创建时异常的出现。
    // 同时，设置required=false后，如果没有匹配的bean Spring将会让这个bean出于未装配的状态。
    @Autowired(required = false)
    private IUserDao iUserDao;

    /**
     * 增加用户
     * @param user
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "addUser",method = RequestMethod.POST)
    public void addUserInfo(User user, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("尝试添加用户信息！");

        HttpSession session = request.getSession(false);
        String userNickname = (String)session.getAttribute("nickname");

        User nowUser = iUserDao.findByNickname(userNickname);
        System.out.println(nowUser.getRoot());
        if(nowUser.getRoot() == 0){
            Status status = new Status();
            status.setStatus("failed");
            JSONObject jsonObject = JSONObject.fromObject(status);
            JsonUtils.ajaxJson(jsonObject.toString(),response);
        }else{
            System.out.println(user.getName());
            System.out.println(user.getNickname());
            user.setHead_img("\\ht_lawyer_pic\\head\\1.jpg");
            user.setPassword("123456");
            user.setRoot(0);
            user.setModule("");
            iUserDao.addUser(user);

            List<User> userList = iUserDao.findAll();
            JSONArray jsonArray = JSONArray.fromObject(userList);
            JsonUtils.ajaxJson(jsonArray.toString(),response);
            logger.info("添加用户信息 成功！");
        }
    }

    /**
     * 更新用户信息
     * @param user
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "updateUser",method = RequestMethod.POST)
    public void updateUserInfo(User user, HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        logger.info("尝试更新用户信息！");
        System.out.println(user.getNickname());
        iUserDao.updateUser(user);
        User updatedUser = iUserDao.findByNickname(user.getNickname());
        JSONObject jsonObject = JSONObject.fromObject(updatedUser);
        JsonUtils.ajaxJson(jsonObject.toString(),response);
        logger.info("更新用户信息 成功！");
    }

    /**
     * 删除用户
     * @param request
     * @param response
     */
    @RequestMapping(value = "deleteUser",method = RequestMethod.POST)
    public void deleteUserInfo(User user,HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        logger.info("准备删除用户信息！");
        int flag = iUserDao.deleteByNickname(user.getNickname());
        Status status = new Status();
        status.setStatus("success");
        JSONObject jsonObject = JSONObject.fromObject(status);
        JsonUtils.ajaxJson(jsonObject.toString(),response);
        logger.info("删除用户信息 成功！");
    }


    /*
     *采用spring提供的上传文件的方法
     */
    @RequestMapping(value = "uploadImg",method = RequestMethod.POST)
    public void springUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws IllegalStateException, IOException, ServletException {
        logger.info("准备更新用户头像！");
//      String path="C:\\Users\\Himory\\Desktop\\ideaSpringMVC\\src\\main\\webapp\\resources\\images\\ht_lawyer_firm\\head\\"+file.getOriginalFilename();
        String path="F:\\ht_lawyer_pic\\head\\"+file.getOriginalFilename();
        String fileName = file.getOriginalFilename();
        if(fileName != null){
            file.transferTo(new File(path));

            // 在数据库中更新
            User updateUser = new User();
            HttpSession session = request.getSession(false);
            updateUser.setHead_img("\\ht_lawyer_pic\\head\\"+fileName);
            updateUser.setNickname((String)session.getAttribute("nickname"));
            iUserDao.updateUserImg(updateUser);

            // 将对象返回给前端
            JSONObject jsonObject = JSONObject.fromObject(updateUser);
            JsonUtils.ajaxJson(jsonObject.toString(),response);
            logger.info("更新用户头像 成功！");
        }else{
            Status status = new Status();
            status.setStatus("failed");
            JSONObject jsonObject = JSONObject.fromObject(status);
            JsonUtils.ajaxJson(jsonObject.toString(),response);
            logger.warn("更新用户头像 失败！");
        }
    }

    @RequestMapping(value = "updateUserPrivateInfo",method = RequestMethod.POST)
    public void updateUserPrivateInfo(User u,HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        logger.info("准备更新用户信息");
        Status status = new Status();
        HttpSession httpSession = request.getSession();
        u.setNickname((String)httpSession.getAttribute("nickname"));
        int flag = iUserDao.updateUserPrivateInfo(u);
        if(flag == 1){
            status.setStatus("success");
            JSONObject jsonObject = JSONObject.fromObject(status);
            JsonUtils.ajaxJson(jsonObject.toString(),response);
            logger.info("更新用户信息 成功！");
        }else{
            status.setStatus("failed");
            JSONObject jsonObject = JSONObject.fromObject(status);
            JsonUtils.ajaxJson(jsonObject.toString(),response);
            logger.warn("更新用户信息 失败！");
        }
    }
}