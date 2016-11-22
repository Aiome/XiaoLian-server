package top.aiome.common.token;


import top.aiome.common.model.User;
import top.aiome.common.utils.TokenUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author malongbo
 * @date 15-1-18
 * @package com.pet.project.common.token
 */
public class TokenManager {
    private static TokenManager me = new TokenManager();
    
    private Map<String, User> tokens;
    private Map<String, String> userToken;

    public TokenManager() {
        tokens = new ConcurrentHashMap<String, User>();
        userToken = new ConcurrentHashMap<String, String>();
    }

    /**
     * 获取单例对象
     * @return
     */
    public static TokenManager getMe() {
        return me;
    }

    /**
     * 验证token
     * @param token
     * @return
     */
    public User validate(String token) {
        return tokens.get(token);
    }

    /**
     * 生成token值
     * @param user
     * @return
     */
    public String generateToken(User user) {
        String token = TokenUtil.generateToken();
        userToken.put(user.getStr(User.USER_ID), token);
        tokens.put(token, user);
        return token;
    }
}
