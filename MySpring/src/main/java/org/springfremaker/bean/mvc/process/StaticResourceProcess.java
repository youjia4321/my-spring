package org.springfremaker.bean.mvc.process;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//处理前端请求 静态资源
public class StaticResourceProcess implements RequestProcess{
    private static final String CSS_STATIC=".css";
    private static final String PNG_STATIC=".png";
    private static final String JPG_STATIC=".jpg";
    //tomcat给我们提供的一个处理静态资源的servlet default
    private static final String DEFAULT_SERVLET="default";


    @Override
    public boolean process(RequestProcessChain processChain, HttpServletRequest request, HttpServletResponse response) throws Throwable {
        ServletContext servletContext = request.getServletContext();
        RequestDispatcher namedDispatcher = servletContext.getNamedDispatcher(DEFAULT_SERVLET);
        //是静态资源 就自己处理了 不用走后面走了
        if(isStatic(request)){
            response.setStatus(HttpServletResponse.SC_OK);
            namedDispatcher.forward(request,response);
            return false;
        }
       return true;
    }
    private boolean isStatic(HttpServletRequest request){
        String requestURI = request.getRequestURI();
        return requestURI.contains(CSS_STATIC) || requestURI.contains(PNG_STATIC) || requestURI.contains(JPG_STATIC);
    }
}
