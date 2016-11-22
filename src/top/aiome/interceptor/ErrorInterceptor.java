package top.aiome.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import top.aiome.common.bean.BaseResponse;
import top.aiome.common.bean.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 捕获所有api action异常
 * @author malongbo
 */
public class ErrorInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(ErrorInterceptor.class);
    @Override
    public void intercept(Invocation ai) {
        try {
            ai.invoke();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            ai.getController().renderJson(new BaseResponse(Code.ERROR, "server error"));
        }
    }
}
