package top.aiome.api;

import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import top.aiome.common.bean.BaseResponse;
import top.aiome.common.bean.Code;
import top.aiome.common.bean.DatumResponse;
import top.aiome.common.model.FeedBack;
import top.aiome.common.model.User;
import top.aiome.common.Require;
import top.aiome.common.utils.DateUtils;
import top.aiome.interceptor.TokenInterceptor;
import top.aiome.version.Version;
import top.aiome.version.VersionManager;


/**
 * 公共模块的api*
 * 
 * 意见反馈: POST /api/feedback
 * 版本更新检查: GET /api/version/check
 *
 * @author malongbo
 */
public class CommonAPIController extends BaseAPIController{

    /**
     * 处理用户意见反馈
     */
    @Before(TokenInterceptor.class)
    public void feedback(){
        if (!"post".equalsIgnoreCase(getRequest().getMethod())) {
            renderJson(new BaseResponse(Code.NOT_FOUND));
            return;
        }

        //内容
        String suggestion=getPara("suggestion");
        if(!notNull(Require.me()
                .put(suggestion, "suggestion can not be null"))){
            return;
        }

        FeedBack feedBack = new FeedBack().set(FeedBack.SUGGESTION, suggestion)
                .set(FeedBack.CREATION_DATE, DateUtils.getNowTimeStamp());

        User user = getUser();
        if (user != null) {
            feedBack.set(FeedBack.USER_ID, user.userId());
        }

        //保存反馈
        boolean flag = feedBack.save();

        renderJson(new BaseResponse(flag ? Code.SUCCESS : Code.FAIL, flag ? "意见反馈成功" : "意见反馈失败"));
    }

    /**
     * 版本更新检查*
     */
    @ActionKey("/api/version/check")
    public void checkVersion() {
        String version = getPara("version");//版本号
        String client = getPara("client"); //终端类型, 可选值有android, iphone
        if (!notNull(Require.me()
                .put(version, "version can not be null")
                .put(client, "client can not be null"))) {
            return;
        }
        
        //检查值是否有效
        if (!Version.checkType(client)) {
            renderArgumentError("client is invalid");
            return;
        }
        
        Version result = VersionManager.me().check(version, client);
        DatumResponse response = new DatumResponse(result);
        if (result == null) {
            response.setCode(Code.FAIL);//表示无更新
        }
        
        renderJson(response);
        
    }
}
