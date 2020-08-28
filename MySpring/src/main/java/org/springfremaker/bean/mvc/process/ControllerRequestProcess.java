package org.springfremaker.bean.mvc.process;

import org.springfremaker.bean.mvc.core.HandlerAdapter;
import org.springfremaker.bean.mvc.core.HandlerMapping;
import org.springfremaker.bean.model.ModelAndView;
import org.springfremaker.bean.mvc.RequestPathInfo;
import org.springfremaker.bean.mvc.annotation.RequestMapping;
import org.springfremaker.bean.mvc.annotation.RequestParam;
import org.springfremaker.bean.mvc.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

//真正的请求controller
public class ControllerRequestProcess implements RequestProcess{

    // 存储URI和对象的方法映射关系
    private List<HandlerMapping> handlerMappings = new ArrayList<>();

    public ControllerRequestProcess(Map<Class<?>,Object> controllerMap) {
        // 初始化映射器
        initHandlerMappings(controllerMap);
    }

    @Override
    public boolean process(RequestProcessChain processChain, HttpServletRequest request, HttpServletResponse response) throws Throwable {

        String requestURI = request.getRequestURI();
        //获得get或者post
        String method = request.getMethod();
        //组装成requestPathInfo
        RequestPathInfo requestPathInfo = new RequestPathInfo(requestURI, method);

        HandlerMapping handlerMapping = getHandler(requestPathInfo);
        if(handlerMapping == null) {
            noHandlerFound(request, response, method);
            return false;
        }

        //调用HandlerAdapter进行参数适配 并且调用方法 获得返回值
        HandlerAdapter ha = new HandlerAdapter(handlerMapping, request, response);
        try {
            ha.handle();
        } catch (Exception e) {
            errorRequest(response, e);
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * 获取handlerMapping
     * @param requestPathInfo requestPathInfo
     * @return HandlerMapping
     */
    private HandlerMapping getHandler(RequestPathInfo requestPathInfo) {
        // /get/user
        for (HandlerMapping handlerMapping : handlerMappings) {
            if(handlerMapping.getRequestPathInfo().equals(requestPathInfo)) {
                return handlerMapping;
            }
        }
        return null;
    }

    /**
     *
     * @param controllerMap
     */
    private void initHandlerMappings(Map<Class<?>,Object> controllerMap) {
        // 判断controller容器中是否有对象
        if(controllerMap.isEmpty()) {
            throw new RuntimeException("没有@Controller注解对象");
        }

        for (Map.Entry<Class<?>, Object> entry: controllerMap.entrySet()) {
            Class<?> clazz  = entry.getKey();
            Object ctl = entry.getValue();
            parseHandlerFromController(clazz, ctl);
        }
    }

    /**
     *
     * @param clazz 当前class
     * @param ctl 当前对象
     */
    private void parseHandlerFromController(Class<?> clazz, Object ctl) {
        // 如果当前类包含RequestMapping注解
        String uriPrefix = "";
        if(clazz.isAnnotationPresent(RequestMapping.class)){
            uriPrefix = clazz.getDeclaredAnnotation(RequestMapping.class).value();
        }
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            // 判断是否有@ResponseBody注解
            boolean isResponseBody = false;
            if(declaredMethod.isAnnotationPresent(ResponseBody.class)) {
                isResponseBody = true;
            }
            // 判断方法上是否有RequestMapping注解
            if(declaredMethod.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = declaredMethod.getAnnotation(RequestMapping.class);
                String URI = uriPrefix + requestMapping.value();
                String requestMethod = requestMapping.method().toString();
                // 封装RequestPathInfo
                RequestPathInfo requestPathInfo = new RequestPathInfo(URI, requestMethod);
                // 处理请求参数
                Map<String, Class<?>> classMap = new ConcurrentHashMap<>();
                List<String> paramList = new CopyOnWriteArrayList<>();
                Map<String, Class<?>> requireParameters = new ConcurrentHashMap<>();
                Map<String, Class<?>> unRequireParameters = new ConcurrentHashMap<>();
                for(Parameter parameter: declaredMethod.getParameters()) {
                    String paramName;
                    if(parameter.isAnnotationPresent(RequestParam.class)) {
                        // 控制当前方法形参是否为必须的 后期还需优化required = false的时候
                        if(parameter.getDeclaredAnnotation(RequestParam.class).required()) {
                            paramName = parameter.getDeclaredAnnotation(RequestParam.class).value();
                            requireParameters.put(paramName, parameter.getType());
                        } else {
                            paramName = parameter.getName();
                            unRequireParameters.put(paramName, parameter.getType());
                        }
                    } else {
                        paramName = parameter.getName();
                        unRequireParameters.put(paramName, parameter.getType());
                    }
                    paramList.add(paramName);
                }
                HandlerMapping handlerMapping = new HandlerMapping(requestPathInfo, ctl, declaredMethod, paramList, isResponseBody, requireParameters, unRequireParameters);
                handlerMappings.add(handlerMapping);
            }
        }

    }

    /**
     *
     * @param request
     * @param response
     * @param method
     * @throws IOException
     */
    private void noHandlerFound(HttpServletRequest request, HttpServletResponse response, String method) throws IOException {
        response.setCharacterEncoding("utf-8");
        String errorMessage = "当前请求的路径:"+request.getRequestURL()+",请求方式:"+method+"没有找到";
        response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMessage);
    }

    /**
     *
     * @param response
     * @param e
     * @throws IOException
     */
    private void errorRequest(HttpServletResponse response, Exception e) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"服务器内部错误:"+e);
    }

}
