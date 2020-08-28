package org.springfremaker.bean.mvc;

import java.util.Objects;

/**
 * url:唯一路径
 * method:请求方式
 *  Map<RequestPathInfo, HandlerMapping>
 * 容器启动的时候 会遍历所有controller
 * 把路径拼接 /test/update------>testcontroller,update方法  handlerMapping
 *
 * 调用update方法 ，通过前端的request获得请求参数，把update方法的参数赋值
 * 调用update方法 获得返回值   HandlerAdapter:参数适配,调用方法
 *
 *获得返回值了以后，判断方法是否有ResponseBody
 * 有-->json(在springmvc.xml配置文件配置的json解析器) --->给前端返回json
 * 没有--->视图解析器 ---》ModelAndView---
 */
public class RequestPathInfo {
    //http://localhost:8888/test/update  Get
    private String url;
    private String method;

    public RequestPathInfo(String url, String method) {
        this.url = url;
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestPathInfo that = (RequestPathInfo) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }

    public RequestPathInfo() {
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "RequestPathInfo{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
