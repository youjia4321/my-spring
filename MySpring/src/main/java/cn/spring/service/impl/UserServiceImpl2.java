package cn.spring.service.impl;

import cn.spring.service.UserService;
import org.springfremaker.bean.annotation.Service;

@Service("bb")
public class UserServiceImpl2  implements UserService{
    @Override
    public String shoot(String target) {
        System.out.println("不被代理的对象调用shoot方法");
        return null;
    }

    @Override
    public void zhuanZhang() {
        System.out.println("不被代理的对象调用shoot2方法");
    }
}
