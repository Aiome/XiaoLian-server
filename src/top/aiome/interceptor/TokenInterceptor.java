package top.aiome.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import top.aiome.common.bean.BaseResponse;
import top.aiome.common.bean.Code;
import top.aiome.common.model.User;
import top.aiome.common.token.TokenManager;
import top.aiome.common.utils.StringUtils;

/**
 * Token拦截器
 * @author malongbo
 * @date 15-1-18
 * @package com.pet.project.interceptor
 */
public class TokenInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation ai) {
        Controller controller = ai.getController();
        String token = controller.getPara("token");
        if (StringUtils.isEmpty(token)) {
            controller.renderJson(new BaseResponse(Code.ARGUMENT_ERROR, "token can not be null"));
            return;
        }

        User user = TokenManager.getMe().validate(token);
        if (user == null) {
            controller.renderJson(new BaseResponse(Code.TOKEN_INVALID, "token is invalid"));
            return;
        }
        
        controller.setAttr("user", user);
        ai.invoke();
    }
}
