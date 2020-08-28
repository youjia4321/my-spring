package org.springfremaker.bean.mvc;

import org.springfremaker.bean.ioc.Container;
import org.springfremaker.bean.mvc.process.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class DispatcherServlet extends HttpServlet {
    private static final String INIT_PARAMETER="contextLocation";
    private static final String PARAMETER_PRE="classpath:";
    //创建一个集合 放入责任链
    List<RequestProcess> list = new CopyOnWriteArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //获得classpath:ioc.properties
        String initParameter = config.getInitParameter(INIT_PARAMETER);
        //ioc.properties
        String path = initParameter.replace(PARAMETER_PRE, "");
        Container instance = Container.getInstance();
        instance.bootstrap(path);
        //所有被controller注解修饰的类
        Map<Class<?>, Object> controllerMap = instance.getControllerMap();

        list.add(new PreRequestProcess());
        list.add(new StaticResourceProcess());
        list.add(new JspRequestProcess());
        list.add(new ControllerRequestProcess(controllerMap));

    }

    //每请求一次执行一次
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestProcessChain requestProcessChain = new RequestProcessChain(list, req, resp);
        try {
            requestProcessChain.doProcessChain();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
