import cn.spring.controller.UserController;
import cn.spring.service.UserService;
import cn.spring.service.impl.UserServiceImpl;
import org.springfremaker.bean.ioc.Container;

import java.io.File;
import java.net.URL;

public class Test {
    public static void main(String[] args) {
        Container container = Container.getInstance();
        container.bootstrap("ioc.properties");
        UserController userController =  (UserController)container.getBean(UserController.class);
        userController.test();

    }
    //通过反射获得子类或者父类
    private static void test2() {
        System.out.println(UserServiceImpl.class.getInterfaces());
        //前面是否是后面的父类
        System.out.println(UserService.class.isAssignableFrom(UserServiceImpl.class));
        System.out.println();
    }

    private static void demo1() {
        Container container = Container.getInstance();
        container.bootstrap("ioc.properties");
        System.out.println("--------------------------------");
        ClassLoader classLoader = container.getClassLoader();
        URL resource = classLoader.getResource("");
        System.out.println(resource.toString());
        //F:/java2002/MySpring/target/classes/cn/cdqf
        File file = new File(resource.toString().replace("file:/","")+ "cn/spring");
        System.out.println(file.isDirectory());
        System.out.println(file);
        System.out.println(resource.toString()+ "cn/spring");
        //递归这个file
        // Class.forName("cn.cdqf.controller.UserController");
        System.out.println( File.separator);
    }
}
