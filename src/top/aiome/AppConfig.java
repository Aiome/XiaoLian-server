package top.aiome;

import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.render.ViewType;

import top.aiome.config.Context;
import top.aiome.handler.APINotFoundHandler;
import top.aiome.handler.ContextHandler;
import top.aiome.interceptor.ErrorInterceptor;
import top.aiome.plugin.HikariCPPlugin;
import top.aiome.router.APIRouter;
import top.aiome.router.ActionRouter;
import top.aiome.common.model.*;


/**
 * JFinal总配置文件，挂接所有接口与插件
 * @author mlongbo
 */
public class AppConfig extends JFinalConfig {

    /**
     * 常量配置
     */
	@Override
	public void configConstant(Constants me) {
		me.setDevMode(true);//开启开发模式
		me.setEncoding("UTF-8");
        me.setViewType(ViewType.FREE_MARKER);
	}

    /**
     * 所有接口配置
     */
	@Override
	public void configRoute(Routes me) {
		me.add(new APIRouter());//接口路由
        me.add(new ActionRouter()); //页面路由
	}

    /**
     * 插件配置
     */
	@Override
	public void configPlugin(Plugins me) {
//		C3p0Plugin cp = new C3p0Plugin(loadPropertyFile("jdbc.properties"));
//		me.add(cp);
        
        //初始化连接池插件
        loadPropertyFile("jdbc.properties");
        HikariCPPlugin hcp = new HikariCPPlugin(getProperty("jdbcUrl"), 
                getProperty("user"), 
                getProperty("password"), 
                getProperty("driverClass"), 
                getPropertyToInt("maxPoolSize"));
     
        
        me.add(hcp);
        
        ActiveRecordPlugin arp = new ActiveRecordPlugin(hcp);
		me.add(arp);
		// 所有配置在 MappingKit 中搞定
 		_MappingKit.mapping(arp);
//		arp.addMapping("t_user", User.USER_ID, User.class);//用户表
//        arp.addMapping("t_register_code", RegisterCode.MOBILE, RegisterCode.class); //注册验证码对象
//        arp.addMapping("t_feedback", FeedBack.class); //意见反馈表
	}

	public static C3p0Plugin createC3p0Plugin() {
		return new C3p0Plugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim());
	}
	
    /**
     * 拦截器配置
     */
	@Override
	public void configInterceptor(Interceptors me) {
		me.add(new ErrorInterceptor());
		
	}

    /**
     * handle 配置*
     */
	@Override
	public void configHandler(Handlers me) {
        me.add(new ContextHandler());
		me.add(new APINotFoundHandler());
	}

    @Override
    public void afterJFinalStart() {
        Context.me().init();
    }

    @Override
    public void beforeJFinalStop() {
        Context.me().destroy();
    }
    
    /**
	 * 建议使用 JFinal 手册推荐的方式启动项目
	 * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
	 */
	public static void main(String[] args) {
		JFinal.start("WebRoot", 80, "/", 5);
	}
}