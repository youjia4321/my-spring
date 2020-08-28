package org.springfremaker.bean.mvc.process;



import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//处理前端请求jsp
public class JspRequestProcess implements RequestProcess{
    private static final String JSP=".jsp";
    private static final String JSP_SERVLET="jsp";
    @Override
    public boolean process(RequestProcessChain processChain,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Throwable {
        RequestDispatcher namedDispatcher = request.getServletContext().getNamedDispatcher(JSP_SERVLET);
        if(isJspResource(request)){//说明当次请求是jsp请求
            response.setStatus(HttpServletResponse.SC_OK);
            namedDispatcher.forward(request,response);
            return false;
        }
        return true;
    }

    public boolean isJspResource(HttpServletRequest request){
        String requestURI = request.getRequestURI();
        return requestURI.contains(JSP);
    }
}
