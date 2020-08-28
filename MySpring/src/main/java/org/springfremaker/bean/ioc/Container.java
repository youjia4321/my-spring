package org.springfremaker.bean.ioc;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springfremaker.bean.annotation.*;
import org.springfremaker.bean.aop.ProceedingJoinPoint;
import org.springfremaker.bean.util.TransactionManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Container {
    private static final Container CONTAINER = new Container();
    private final boolean flag=true;
    public static final String componentScan="componentScan";
    //类的全路径 线程安全的hashSet cn.cdqf.controller.UserController
    private Set<String> stringSet = new CopyOnWriteArraySet<>();
    //装切面的集合
    private Set<Class<?>> aopSet = new CopyOnWriteArraySet<>();
    //定义ioc容器  iocMap  key:UserServiceImpl.class  就是被添加了service,controller等注解得类class对象
    //value :就是对应对象  new UserServiceImpl
    private Map<Class<?>,Object> iocMap = new ConcurrentHashMap<>();
    //定义一个map<Class<?>,List<Object>>  key:接口类  List<Object>:实现类对象
    //key:UserService  value 所有实现了该接口得子类对象
    private Map<Class<?>,List<Object>> superIocMap = new ConcurrentHashMap<>();
    //根据指定的bean名字来存储
    //key:userServiceImpl(默认首字母小写)  value：new UserServiceImpl
    private Map<String,Object> nameIocMap = new ConcurrentHashMap<>();
    //存储controller注解 为了springmvc服务
    private Map<Class<?>,Object> controllerMap = new ConcurrentHashMap<>();
    //path:要扫描得包路径  通过ioc.properties读取
    private Container(){
    }
    public static Container getInstance(){
        return CONTAINER;
    }
    public  void bootstrap(String path) {
        //定位 cn.cdqf
        String packagePath = loadResource(path);
        //载入类
        loadAllClass(packagePath);
        //初始化
        doInit();
        System.out.println("初始化ioc成功");
        //做切面
        doAop();
        //依赖注入
        doPopulation();
        System.out.println("依赖注入成功");
    }

    private void doAop() {
        if(aopSet==null||aopSet.size()==0)return;
        //循环获得每一个切面类  clazz:MyAop
        for(Class<?> clazz:aopSet) {
            //获得当前切面类所有的方法
            Method[] declaredMethods = clazz.getDeclaredMethods();
            try {
                //只能代理一个方法
                for (Method method : declaredMethods) {
                    boolean annotationPresent = method.isAnnotationPresent(Around.class);
                    if (annotationPresent) {
                        Around annotation = method.getAnnotation(Around.class);
                        //被切的方法
                        //cn.cdqf.service.impl.UserServiceImpl.shoot
                        String value = annotation.value();
                        //cn.cdqf.service.impl.UserServiceImpl
                        String classFullPath = value.substring(0, value.lastIndexOf("."));
                        //shoot:定死了 只切这个方法
                        String methodName = value.substring(value.lastIndexOf(".") + 1);
                        //根据类名可以获得该类的class对象  根据class对象可以取ioc容器中获得它的对象
                        Class<?> aClass = Class.forName(classFullPath);
                        //被代理对象 被代理之前先注入
                        populationOne(aClass);
                        JdkProxyDemo<Object> objectJdkProxyDemo = new JdkProxyDemo<>(aClass,clazz,methodName,method);
                        //已经生成代理对象
                        Object instance = objectJdkProxyDemo.getInstance();
                        //替换掉容器中对象
                        iocMap.remove(aClass);


                        //默认等于首字母小写
                        String simpleName = aClass.getSimpleName();
                        String name = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
                        //修改名字 取原对象上面的所有注解  加了注解就应该以value属性为准
                        Service annotation1 = aClass.getAnnotation(Service.class);
                        if(annotation1!=null){//说明被代理类就是加的service注解
                            String value1 = annotation1.value();
                            if(!value1.equals("")){//说明指定了名字
                                name = value1;
                            }
                        }else{//说明加的controller注解
                            Controller controller = aClass.getAnnotation(Controller.class);
                            String value1 = controller.value();
                            if(!value1.equals("")){//说明指定了名字
                                name = value1;
                            }

                        }
                        nameIocMap.put(name, instance); //根据名字替换原来的类成功

                        //开始替换接口
                        Class<?>[] interfaces = aClass.getInterfaces();
                        if(interfaces==null)continue;
                        //每个接口
                        for (Class<?> anInterface : interfaces) {
                            //当前接口所实现的所有子类对象
                            List<Object> objects = superIocMap.get(anInterface);
                            for (int i = 0; i < objects.size(); i++) {
                                Object eachObj = objects.get(i);
                                if(eachObj.getClass()==aClass){
                                    objects.set(i, instance);
                                    break;
                                }
                            }
                        }

                        //几个容器都得替换
                        //比如根据名字 查找哪个map 根据接口找个那个也要替换
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    //生成代理对象


    private class JdkProxyDemo<T> {
        //被代理对象
        Class<?> aClass;
        //切面对象
        Class<?> clazz;
        //被代理对象的方法名字,只能代理一个方法
        String methodName;
        //切面对象的方法
        Method myMethod;
        Object obj;
        //target：被代理类
        public JdkProxyDemo(Class<?> aClass,Class<?> clazz,String methodName,Method method){
            this.aClass = aClass;
            this.clazz = clazz;
            this.myMethod = method;
            this.methodName = methodName;
            obj = iocMap.get(aClass);
        }
        public Object getInstance(){
            return  Proxy.newProxyInstance(aClass.getClassLoader(),
                    aClass.getInterfaces(), new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            //method:被代理对象得方法
                            if(method.getName().equals(methodName)){//说明当前调用得方法 就是需要被代理得
                                ProceedingJoinPoint proceedingJoinPoint = new ProceedingJoinPoint(method,args,obj);
                                //切面方法
                                return myMethod.invoke(clazz.newInstance(), proceedingJoinPoint);
                            }
                            //不被代理
                            return method.invoke(obj, args);
                        }
                    });
        }
    }
    ////对ioc map进行依赖注入
    private void doPopulation() {

        //所有交给spring管理的类的Class对象
        Set<Class<?>> classes = iocMap.keySet();
        for (Class<?> aClass : classes) {
            populationOne(aClass);
        }

    }

    private void populationOne(Class<?> aClass) {
        //获得当前class里面所有属性 ，然后判断这些属性上面是否有autowired
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            //判断属性上是否有Autowired注解
            boolean annotationPresent = declaredField.isAnnotationPresent(Autowired.class);
            //就需要依赖注入
            if(annotationPresent){
                //根据类型注入最优先
                Object bean = this.getBean(declaredField.getType());
                if(bean==null){
                    //名字其次
                    String name = declaredField.getName();
                    bean = this.getBean(name);
                    if(bean==null){//有可能是接口
                        bean = this.getBeanByInterface(declaredField.getType());
                        if(bean==null){
                            //没有对象能依赖注入
                            throw new RuntimeException("一个都没有expected at least 1 bean which qualifies as autowire candidate");
                        }
                    }
                }
                //bean已经在ioc容器中取到了 赋值
                declaredField.setAccessible(true);
                try {

                    //对属性赋值
                    declaredField.set(iocMap.get(aClass), bean);

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    List<Class<? extends Annotation>> list = Arrays.asList(Controller.class, Service.class);
    private void doInit() {
        try {
            if (stringSet != null)
                for (String path : stringSet) {
                    Class<?> aClass = Class.forName(path);
                    //说明是切面类
                    if(aClass.isAnnotationPresent(Aspect.class)){
                        aopSet.add(aClass);
                        continue;
                    }
                    //判断这些class里面是否存在我们标记的注解
                    for (Class<? extends Annotation> aClass1 : list) {
                        //aClass对面上面是否有aClass1这个注解
                        if(aClass.isAnnotationPresent(aClass1)){
                            //要交给spring管理
                            //创建对象 调用无参构造创建对象
                            //obj :UserController对象 以及UserServiceImpl对象
                            Object obj = aClass.newInstance();
                            //把有controller或者service注解的类 加入了ioc容器
                            //判断类上面是否有Transactional注解,需要事务管理
                            //动态代理
                            boolean annotationPresent = aClass.isAnnotationPresent(Transactional.class);
                            if(annotationPresent){//用事务增强
                                CglibTest cglibTest = new CglibTest(aClass);
                                obj = cglibTest.create();//生成的代理对象
                            }
                            //存入iocMap中
                            iocMap.put(aClass, obj);
                            Class<?>[] interfaces = aClass.getInterfaces();
                            if(interfaces!=null){
                                //一个接口有多个实现类 有可能当前接口已经都放入了实现类
                                for (Class<?> anInterface : interfaces) {
                                    List<Object> objects = superIocMap.get(anInterface);
                                    if(objects==null){//当前接口对应的对象还没有放过
                                        ArrayList<Object> objects1 = new ArrayList<>();
                                        objects1.add(obj);
                                        superIocMap.put(anInterface,objects1);
                                    }else{//当前接口对应的对象放过
                                        objects.add(obj);
                                    }
                                }
                            }
                            //获取名字
                            if(aClass1==Service.class){
                                Service annotation = aClass.getAnnotation(Service.class);
                                String value = annotation.value();
                                if(value.equals("")){
                                    //首字母小写
                                    String simpleName = aClass.getSimpleName();
                                    value = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
                                }
                                if(nameIocMap.containsKey(value)){
                                    throw new RuntimeException("相同的bean名字non-compatible bean definition of same name");
                                }
                                nameIocMap.put(value,obj);
                            }
                            if(aClass1==Controller.class){
                                Controller annotation = aClass.getAnnotation(Controller.class);
                                String value = annotation.value();
                                if(value.equals("")){
                                    //首字母小写
                                    String simpleName = aClass.getSimpleName();
                                    value = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
                                }
                                if(nameIocMap.containsKey(value)){
                                    throw new RuntimeException("相同的bean名字non-compatible bean definition of same name");
                                }
                                nameIocMap.put(value,obj);
                                //所有被controller注解修饰的类
                                controllerMap.put(aClass,obj);
                            }


                            break;
                        }
                    }
                }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public Map<Class<?>,Object> getControllerMap(){
        return controllerMap;
    }
    private static class CglibTest{
        //被代理对象
        Class<?> aClass;
        public CglibTest(Class<?> aClass){
            this.aClass = aClass;
        }
        public Object create(){
            Enhancer enhancer = new Enhancer();
            //被代理类
            enhancer.setSuperclass(aClass);

            enhancer.setCallback(new MethodInterceptor() {
                     public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                         Connection connection =null;
                         try {
                             connection = TransactionManager.connection();
                             connection.setAutoCommit(false);
                             //设置autocommit=false
                             //真正的调用转账方法zhuanZhang
                             Object result = methodProxy.invokeSuper(o, objects);
                             //提交
                             System.out.println("事务提交");
                             connection.commit();
                             return result;
                         }catch (Exception e){
                             System.out.println("事务回滚");
                             assert connection != null;
                             connection.rollback();
                             //回滚
                            throw new RuntimeException(e);
                         }

                     }
                 }
            );

            return enhancer.create();
        }
    }
    //cn.cdqf.xx.xxx --->cn/cdqf
    private void loadAllClass(String packagePath) {
        ClassLoader classLoader = this.getClassLoader();
        URL resource = classLoader.getResource("");
        String replace = packagePath.replace(".", File.separator);
        assert resource != null;
        File file = new File(File.separator+resource.toString().replace("file:/","") + replace);
        //获得所有的class结尾的文件
        diGui(file,packagePath);
    }
    public void diGui(File file,String packagePath){
        if(file==null)return;
        if(file.isDirectory()){
            //获得当前包下面所有文件  controller service
            File[] files = file.listFiles();
            if(files!=null)
                for(File f:files){
                    if(f.isDirectory()){
                        diGui(f,packagePath+"."+f.getName());
                    }else {
                        //UserController.class
                        String name = f.getName();
                        if(name.endsWith(".class")) {
                            String substring = name.substring(0, name.indexOf("."));
                            stringSet.add(packagePath+"."+substring);
                        }
                    }
                }
        }else{
            //UserController.class
            String name = file.getName();
            if(name.endsWith(".class")) {
                //UserController cn.cdqf.controller.UserController
                String substring = name.substring(0, name.indexOf("."));
                stringSet.add(packagePath+"."+substring);
            }

        }
    }

    //定位 要扫描的包
    public String loadResource(String path){
        //hashTable的子类
        Properties properties = new Properties();
        //定位
        ClassLoader classLoader = getClassLoader();
        //把ioc.properties转换为io输入流
        InputStream resourceAsStream = classLoader.getResourceAsStream(path);
        try {
            properties.load(resourceAsStream);
            return properties.getProperty(componentScan);//cn.cdqf
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public ClassLoader getClassLoader(){
        //当前线程的累加载器
        return Thread.currentThread().getContextClassLoader();
    }
    //1.按照接口类型来取子类对象
    public Object getBeanByInterface(Class<?> clazz){
        List<Object> objects = superIocMap.get(clazz);
        if(objects==null)return null;
        if(objects.size()>1){
            throw new RuntimeException(clazz.getName()+" 想找一个 但是有多个 available: expected single matching bean but found "+objects.size());
        }
        return objects.get(0);
    }
    //2.按照本类型来取对象
    public Object getBean(Class<?> clazz){
        return iocMap.get(clazz);
    }
    //3.按照名字来取对象
    public Object getBean(String beanName){
        return nameIocMap.get(beanName);
    }
}
