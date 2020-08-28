package org.springfremaker.bean.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelAndView {

    //携带的数据 待会放入request作用域
    private Map<String, Object> map = new ConcurrentHashMap<>();
    //要跳转的页面
    private String viewName;

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public ModelAndView addObject(String attributeName, Object attributeValue){
        map.put(attributeName,attributeValue);
        return this;
    }

}
