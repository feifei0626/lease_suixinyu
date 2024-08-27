package com.atguigu.lease.common.login;


//为避免重复解析，通常会在拦截器将Token解析完毕后，
//将结果保存至ThreadLocal中，这样一来，我们便可以在整个请求的处理流程中进行访问了。
public class LoginUserHolder {
    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();


    public static void setLoginUser(LoginUser loginUser) {
        threadLocal.set(loginUser);
    }

    public static LoginUser getLoginUser() {
        return threadLocal.get();
    }

    public static void clear() {
        threadLocal.remove();
    }
}
