package cn.spring.controller;

import cn.spring.service.UserService;
import org.springfremaker.bean.annotation.Autowired;
import org.springfremaker.bean.annotation.Controller;

@Controller
public class UserController {

    @Autowired
    private UserService aa;
    @Autowired
    private UserService bb;

    public void test(){
        aa.zhuanZhang();

    }
}
