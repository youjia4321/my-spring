package org.springfremaker.bean.mvc.process;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RequestProcess {
    boolean process(RequestProcessChain processChain,
                    HttpServletRequest request,
                    HttpServletResponse response) throws Throwable;
}
