package org.springfremaker.bean.mvc.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springfremaker.bean.mvc.RequestPathInfo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandlerMapping {

    private RequestPathInfo requestPathInfo; // uri -> method (get\post)
    private Object controller; // 控制器类
    private Method method; // 当前方法
    private List<String> args; // 当前方法的参数名
    private boolean isResponseBody; // 当前方法是否加了@ResponseBody注解

    private Map<String, Class<?>> requireParameters; // 必须要的参数
    private Map<String, Class<?>> unRequireParameters; // 不必须要的参数

}
