package org.springfremaker.bean.mvc.process;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * 处理器链条中的第一个处理器
 * 做字符集设置
 */
public class PreRequestProcess implements RequestProcess{
    // chain有很多个执行器，
    // process方法返回true就继续执行 否则不执行了
    @Override
    public boolean process(RequestProcessChain processChain, HttpServletRequest request, HttpServletResponse response) throws Throwable {
        request.setCharacterEncoding("utf-8");
        //前置处理 认为该请求对项目有害.....
        return true;
    }
}
