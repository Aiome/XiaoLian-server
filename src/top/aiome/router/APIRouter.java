package top.aiome.router;

import com.jfinal.config.Routes;
import top.aiome.api.*;

/**
 * @author malongbo
 */
public class APIRouter extends Routes {
    @Override
    public void config() {
        //公共api
        add("/api", CommonAPIController.class);
        //用户相关
        add("/api/account", AccountAPIController.class);
        //文件相关
        add("/api/fs",FileAPIController.class);
        //匹配相关
        add("/api/match",MatchAPIController.class);
        //评论相关
        add("/api/comment",CommentAPIController.class);
        //出游相关
        add("/api/travel",TravelAPIController.class);
        //学校相关
        add("/api/college",CollegeAPIController.class);
        //奖品相关
        add("/api/prize",CollegeAPIController.class);
        
        add("/api/temp",temp.class);
    }
}
