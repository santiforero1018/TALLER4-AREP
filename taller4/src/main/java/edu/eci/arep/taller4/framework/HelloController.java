package edu.eci.arep.taller4.framework;

import edu.eci.arep.taller4.annotations.Component;
import edu.eci.arep.taller4.annotations.RequestMapping;

@Component
public class HelloController {
    
    @RequestMapping("/hello")
    public static String hello(){
        return "Hello framework";
    }

    @RequestMapping("/message")
    public static String queryHello(String param) {
        return "Hello "+param;
    }

    @RequestMapping("/raiz")
    public static Double sqrtOp(String num){
        return Math.sqrt(Double.parseDouble(num));
    }


}
