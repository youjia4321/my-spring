package cn.spring.service.impl;

import cn.spring.dao.UserDao;
import cn.spring.service.UserService;
import org.springfremaker.bean.annotation.Autowired;
import org.springfremaker.bean.annotation.Service;
import org.springfremaker.bean.annotation.Transactional;

@Service("aa")
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Override
    public String shoot(String target) {
        System.out.println("射击的目标是："+target);
        System.out.println(userDao);
        return "射击成功";
    }

    @Override
    public void zhuanZhang() {
        System.out.println(userDao);
        userDao.insert("zhangsan",500);
        System.out.println("转账执行了");
        userDao.insert("lisi",-500);
//        int i = 1/0;
    }
}
