package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.mapper.SystemUserMapper;
import com.atguigu.lease.web.admin.service.LoginService;
import com.atguigu.lease.web.admin.vo.login.CaptchaVo;
import com.atguigu.lease.web.admin.vo.login.LoginVo;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserInfoVo;
import com.wf.captcha.SpecCaptcha;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SystemUserMapper systemUserMapper;

    @Override
    public CaptchaVo getCaptcha() {
        //获取验证码图片
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        //得到验证码密码
        String code = specCaptcha.text().toLowerCase();
        //生成一个UUID
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();  //Redis中key命名有规范 本项目为 项目名:功能模块名:其他
        //将UUID和验证码密码保存至redis,并设置验证码有效时长
        stringRedisTemplate.opsForValue().set(key,code,RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);
        //返回验证码和UUID给前端
        return new CaptchaVo(specCaptcha.toBase64(),key);
    }

    @Override
    public String login(LoginVo loginVo) {
        //用户登录的校验:校验验证码、校验用户状态和校验密码
        //1.校验验证码
        if(loginVo.getCaptchaCode() == null) throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        String code = stringRedisTemplate.opsForValue().get(loginVo.getCaptchaKey());
        if(code == null) throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        if(!code.equals(loginVo.getCaptchaCode().toLowerCase())) throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        //2.校验用户状态
            //根据username查询数据库的用户信息  这里不能用MP提供的方法查询密码，因为已经设置select不可查，要自定义方法查询
        SystemUser systemUser = systemUserMapper.selectOneByUsername(loginVo.getUsername());
            //验证用户是否存在
        if(systemUser == null) throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
            //验证用户状态
        if(systemUser.getStatus() == BaseStatus.DISABLE) throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
            //密码校验
        if(!systemUser.getPassword().equals(DigestUtils.md5Hex(loginVo.getPassword()))) throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        //创建并返回Token
        return JwtUtil.createToken(systemUser.getId(),systemUser.getUsername());
    }

    @Override
    public SystemUserInfoVo getLoginUserInfo(Long userId) {
        SystemUser systemUser = systemUserMapper.selectById(userId);
        SystemUserInfoVo systemUserInfoVo = new SystemUserInfoVo();
        systemUserInfoVo.setName(systemUser.getUsername());
        systemUserInfoVo.setAvatarUrl(systemUser.getAvatarUrl());
        return systemUserInfoVo;
    }
}
