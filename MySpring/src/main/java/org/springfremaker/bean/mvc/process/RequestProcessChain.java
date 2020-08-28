package org.springfremaker.bean.mvc.process;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class RequestProcessChain {
    private List<RequestProcess> list;
    private HttpServletRequest request;
    private HttpServletResponse response;
    public RequestProcessChain(List<RequestProcess> list,HttpServletRequest request,HttpServletResponse response){
        this.list = list;
        this.request = request;
        this.response = response;
    }

    public void doProcessChain() throws Throwable {
        if(list==null||list.size()==0)return;

        for (RequestProcess requestProcess : list) {
            boolean process = requestProcess.process(this, request, response);
            if(!process)break;
        }
    }
}
