package org.springfremaker.bean.mvc.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springfremaker.bean.model.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandlerAdapter {

    private HandlerMapping handlerMapping;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private boolean flag = true;
    private List<String> requireName;

    public HandlerAdapter(HandlerMapping handlerMapping, HttpServletRequest request, HttpServletResponse response) {
        this.handlerMapping = handlerMapping;
        this.request = request;
        this.response = response;
    }

    public void handle() throws InvocationTargetException, IllegalAccessException, IOException, ServletException {
        // 获取参数
        Object[] parameters = new Object[handlerMapping.getArgs().size()];
        int i = 0;

        this.requireName = new CopyOnWriteArrayList<>();
        for (String paramName : handlerMapping.getArgs()) {
            // 必须参数里面含有这个参数名
            if(handlerMapping.getRequireParameters().containsKey(paramName)) {
                i = doParameter(parameters, handlerMapping.getRequireParameters().get(paramName), i, paramName, true);
            } else if(handlerMapping.getUnRequireParameters().containsKey(paramName)) { // 不必须包含
                i = doParameter(parameters, handlerMapping.getUnRequireParameters().get(paramName), i, paramName, false);
            }
        }

        // 当必须参数为null时 运行下面
        if(!this.flag) {
            response.setCharacterEncoding("utf-8");
            String errorMessage = "当前请求的路径:"+request.getRequestURL()+",必须请求参数："+this.requireName+" 没有找到";
            response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMessage);
        }

        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), parameters);
        if(handlerMapping.isResponseBody()) { // 加了@ResponseBody注解的
            response.setContentType("application/json;charset=utf-8");
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().print(objectMapper.writeValueAsString(result));
        } else { // 没有加@ResponseBody注解
            if(result instanceof String) { // 判断返回对象是不是字符串
                if(((String) result).contains("redirect:/")) {
                    String value = ((String) result).split("redirect:/")[1];
                    ModelAndView mv = new ModelAndView(value);
                    redirect(mv, request, response);
                } else { // 判断用户写的是forward还是直接写的字符串
                    String value = (String) result;
                    if(value.contains("forward:/")) {
                        value = ((String) result).split("forward:/")[1];
                    }
                    request.setAttribute("username", "forward");
                    ModelAndView mv = new ModelAndView(value);
                    render(mv, request, response);
                }
            } else { // 返回对象是ModelAndView
                ModelAndView mv = (ModelAndView) result;
                for (Map.Entry<String, Object> entry : mv.getMap().entrySet()) {
                    request.setCharacterEncoding("utf-8");
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
                render(mv, request, response);
            }
        }


    }

    // 处理参数
    private int doParameter(Object[] parameters, Class<?> clazz, int i, String name, boolean flag) {
        String parameterValue = request.getParameter(name);
        if(flag) { // 必须包含的参数
            if(parameterValue == null) {
                this.flag = false;
                this.requireName.add(name);
                parameters[i++] = null;
            } else {
                i = typeCast(parameters, clazz, i, name, parameterValue);
            }
        } else {
            i = typeCast(parameters, clazz, i, name, parameterValue);
        }
        return i;
    }

    // 参数类型处理
    private int typeCast(Object[] parameters, Class<?> clazz, int i, String name, String parameterValue) {
        if(clazz == HttpServletRequest.class) {
            parameters[i++] = request;
        } else if (clazz == HttpServletResponse.class) {
            parameters[i++] = response;
        } else if (clazz == ModelAndView.class) {
            parameters[i++] = new ModelAndView();
        } else if(clazz == int.class || clazz == Integer.class) {
            parameters[i++] = Integer.valueOf(parameterValue);
        } else if(clazz == long.class || clazz == Long.class) {
            parameters[i++] = Long.valueOf(parameterValue);
        } else if(clazz.isArray()) { // 数组类型
            i = doArray(parameters, i, name, clazz);
        } else { // String类型
            parameters[i++] = parameterValue;
        }
        return i;
    }

    // 处理数组
    private int doArray(Object[] parameters, int i, String name, Class<?> clazz) {
        if(clazz.getSimpleName().equalsIgnoreCase("int[]")) {
            String[] ints = request.getParameterValues(name);
            if(ints != null) {
                int[] objects = new int[ints.length];
                int j = 0;
                for (String string : ints) {
                    int o = (int)myConvertSingle(clazz, string);
                    objects[j++] = o;
                }
                parameters[i++] = objects;
            } else {
                parameters[i++] = null;
            }


        } else { // 只考虑int[] 和 String[]数组类型
            String[] strings = request.getParameterValues(name);
            if(strings != null) {
                String[] objects = new String[strings.length];
                int j = 0;
                for (String string : strings) {
                    String o = (String)myConvertSingle(clazz, string);
                    objects[j++] = o;
                }
                parameters[i++] = objects;
            } else {
                parameters[i++] = null;
            }
        }
        return i;
    }

    // 只完成8大基本数据类型+String类型
    private Object myConvertSingle(Class<?> clazz, String string) {
        if(clazz==int.class||clazz.getSimpleName().equalsIgnoreCase("int[]")){
            return Integer.valueOf(string);
        }else  if(clazz==boolean.class||clazz ==Boolean.class){
            return Boolean.valueOf(string);
        } else if(clazz==float.class||clazz.getSimpleName().equalsIgnoreCase("float[]")){
            return Float.valueOf(string);
        } else if(clazz==short.class||clazz.getSimpleName().equalsIgnoreCase("short[]")){
            return Short.valueOf(string);
        } else if(clazz==long.class||clazz.getSimpleName().equalsIgnoreCase("long[]")){
            return Long.valueOf(string);
        } else if(clazz==double.class||clazz.getSimpleName().equalsIgnoreCase("double[]")){
            return Double.valueOf(string);
        } else if(clazz==byte.class||clazz.getSimpleName().equalsIgnoreCase("byte[]")){
            return Byte.valueOf(string);
        }
        return string;
    }

    private void render(ModelAndView mv, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + mv.getViewName() + ".jsp").forward(req, resp);
    }

    private void redirect(ModelAndView mv, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("/" + mv.getViewName() + ".jsp");
    }

}
