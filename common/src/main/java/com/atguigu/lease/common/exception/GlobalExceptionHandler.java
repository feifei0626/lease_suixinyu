package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//定义全局异常处理器
@ControllerAdvice  //由SpringMVC提供
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody   //@ResponseBody 表示该方法的返回结果直接写入 HTTP response body 中,将java对象转为json格式的数据
    public Result handle(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }

    @ExceptionHandler(value = LeaseException.class)
    @ResponseBody   //@ResponseBody 表示该方法的返回结果直接写入 HTTP response body 中,将java对象转为json格式的数据
    public Result handle(LeaseException e) {
        e.printStackTrace();
        return Result.fail(e.getCode(), e.getMessage());
    }
}
