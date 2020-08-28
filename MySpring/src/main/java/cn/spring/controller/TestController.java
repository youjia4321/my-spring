package cn.spring.controller;

import org.springfremaker.bean.annotation.Controller;
import org.springfremaker.bean.model.ModelAndView;
import org.springfremaker.bean.mvc.annotation.RequestMapping;
import org.springfremaker.bean.mvc.annotation.RequestParam;
import org.springfremaker.bean.mvc.annotation.ResponseBody;
import org.springfremaker.bean.mvc.type.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/test")
public class TestController {

    @RequestMapping(value = "/update1", method= RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> test1(HttpServletRequest request,
                     HttpServletResponse response,
                     @RequestParam("id") Integer id,
                     @RequestParam("aid")int[] aid,
                     String[] hobby,
                     String username) {
        Map<String, Object> map = new HashMap<>();
        map.put("uri", request.getRequestURI());
        map.put("status", response.getStatus());
        map.put("id", id);
        map.put("aid", aid);
        map.put("hobby", hobby);
        map.put("username", username);
        return map;
    }

    @RequestMapping(value = "/forward", method= RequestMethod.GET)
    public String forward(HttpServletRequest request) {
        return "forward:/model";
    }

    @RequestMapping(value = "/index", method= RequestMethod.GET)
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/redirect", method= RequestMethod.GET)
    public String redirect() {
        return "redirect:/redirect";
    }

    @RequestMapping("/modelandview")
    public ModelAndView modelAndView(ModelAndView modelAndView) {
        modelAndView.addObject("username", "modelandview");
        modelAndView.setViewName("model");
        return modelAndView;
    }

}
