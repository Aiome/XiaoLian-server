package top.aiome.common.bean;

/**
 * @author malongbo
 */
public class Code {

    /**
     * 成功
     */
    public static final int SUCCESS = 1;

    /**
     * 失败 
     */
    public static final int FAIL = 0;

    /**
     * 参数错误: 一般是缺少或参数值不符合要求
     */
    public static final int ARGUMENT_ERROR = 2;

    /**
     * 服务器错误
     */
    public static final int ERROR = 500;

    /**
     * 接口不存在
     */
    public static final int NOT_FOUND = 404;

    /**
     * token无效
     */
    public static final int TOKEN_INVALID = 422;

    /**
     * 帐号已存在*
     */
    public static final int ACCOUNT_EXISTS = 3;

    /**
     * 验证码错误
     */
    public static final int CODE_ERROR = 4;
    /**
     * 没有符合条件的导游
     */
    public static final int NO_GUIDER = 5;
    /**
     * 不能开始旅行
     */
    public static final int TRAVEL_ERROR = 6;
    /**
     * 未查询到符合条件的匹配记录
     */
    public static final int NO_RECORED = 7;
    /**
     * 不能开始匹配
     */
    public static final int MATCH_ERROR = 8;
    /**
     * 该用户没有出游记录
     */
    public static final int NO_TRAVEL_RECORED = 9;
    /**
     * 该用户没有导游记录
     */
    public static final int NO_GUIDE_RECORED = 10;
    /**
     * 不显示remark
     */
    public static final int NO_REMARK = 11;
    /**
     * 显示remark
     */
    public static final int SHOW_REMARK = 12;
}
