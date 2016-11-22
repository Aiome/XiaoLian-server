package top.aiome.handler;

import com.jfinal.handler.Handler;
import top.aiome.config.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author malongbo
 */
public class ContextHandler extends Handler {
    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
        Context.me().setRequest(request);
        this.nextHandler.handle(target, request, response, isHandled);
    }
}
