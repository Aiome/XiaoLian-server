
package top.aiome.api;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;

import push.Demo;
import top.aiome.common.Require;
import top.aiome.common.bean.Code;
import top.aiome.common.bean.DatumResponse;
import top.aiome.common.model.Match;
import top.aiome.common.model.Travel;
import top.aiome.common.model.User;
import top.aiome.common.token.TokenManager;
import top.aiome.common.utils.RandomUtils;
import top.aiome.common.utils.StringUtils;
import top.aiome.interceptor.TokenInterceptor;

@Before(TokenInterceptor.class)
public class MatchAPIController extends BaseAPIController{
	//发出请求
	//导游确认
	//出游者确认
	//获取出游聊天列表
	//获取导游聊天列表	
	//从聊天列表删除联系人
	
	/**
	 * 1.客户端提交出游用户数据(userId,条件，token)   start() post
	 * 		条件:性别、学校、专业、入学年份、星座、年龄段
	 * 2.服务端筛选出符合条件的用户
	 * 		将筛选出的用户保存至匹配表中
	 * 3.服务器将消息推送至符合条件的用户
	 * 		将出游用户置为不可发送请求状态
	 * 
	 * 4.客户端提交数据(导游用户) guider()	post
	 * 		条件：出游者id，确认结果
	 * 5.服务器将导游用户确认结果推送至出游用户
	 * 
	 * 6.客户端(出游用户)提交最终选择结果  matchEnd()	post
	 * 		条件：导游id，确认结果
	 * 		将匹配数据从匹配表删除
	 */
	public void guiderChatList(){
		User guider = getUser();
		String guiderId = guider.getUserId();
		String travelIdGuider = guider.getTravelIdGuider();
		//若用户还未担任过导游，则没有聊天列表
		if(!notNull(Require.me()
    			.put(travelIdGuider, "user has not served as a guide"))){
    		return;
    	}
		String sql = "SELECT * FROM `match` where userId=? and travelIdGuider=? and flagUser=1";
		List<Match> lm = Match.dao.find(sql,guiderId,travelIdGuider);
	}
	/**
	 * 获取出游者聊天列表
	 */
	public void userChatList(){
		
		User user = getUser();
		String userId = user.getUserId();
		String travelIdUser = user.getTravelIdUser();
		//若用户还未发出过请求，则没有聊天列表
		if(!notNull(Require.me()
    			.put(travelIdUser, "user has not yet issued a request"))){
    		return;
    	}
		String sql = "SELECT * FROM `match` where userId=? and travelIdUser=? and flagGuider=1";
		List<Match> lm = Match.dao.find(sql,userId,travelIdUser);
		
		DatumResponse response = new DatumResponse();
		if(lm.isEmpty()){
        	response.setCode(Code.FAIL).setMessage("not found available chat list");
        }else {
        	response.setDatum(lm);
        }
        renderJson(response);		
        
	}
	/**
	 * 出游者再次确认
	 */
	public void matchEnd(){
		String method = getRequest().getMethod();
		if ("post".equalsIgnoreCase(method)) { 
            render404();
		}
		String guiderId = getPara("guiderId");
		/**
		 * result 0 / 1
		 */
		String result = getPara("result");
		String userId = getUser().getUserId();
		
		if(!notNull(Require.me()
    			.put(guiderId, "guiderId can not be null")
    			.put(result,"user choose result can not be null"))){
    		return;
    	}
		//从Match表查出用户id=userid and 导游id = guiderid的记录 
		String sql = "SELECT * FROM `match` where userId=? and guiderId=? AND travelId NOT IN(SELECT travelId FROM travel)";
		Match match = Match.dao.findFirst(sql,userId,guiderId);
		
		if(match == null){
			renderFailed("not found matching records");
			return;
		}
		
		if(result.equals("0")){
			match.setCurrent(Match.CODE_FAIL_USER_REFUSE);
		}else if(result.equals("1")){
			match.setCurrent(Match.CODE_USER_ACCEPT);
			
			//匹配成功    创建出游记录
			String travelId = match.getTravelId();
			String userId2 = match.getUserId();
			String guilderId = match.getGuiderId();
			String schoolId = match.getSchoolId();
			Date travelTime = match.getTime();
			Boolean flag = true;
			new Travel()
					.set("travelId", travelId)
					.set("userId", userId2)
					.set("guiderId", guilderId)
					.set("travelTime", travelTime)
					.set("flag", flag)
					.set("schoolId", schoolId)
					.save();
		}else{
			renderArgumentError("result is not valid");
			return;
		}
		match.update();
		
		
		
		//将结果推送至导游用户
		/**
		 * 集成友盟推送,暂用返回json
		 */
		DatumResponse response = new DatumResponse();
		User guider = User.user.findById(userId);
		if(guider == null){
	    	response.setCode(Code.FAIL).setMessage("not found user");
	    }else {
	    	HashMap<String, Object> map = new HashMap<String, Object>(guider.getAttrs());
            map.remove("password");
            response.setDatum(map);
	    }
		//将出游者匹配状态置为 可匹配
		getUser().set("flag", 1).update();
		//匹配结束删除记录
//		 Db.update("DELETE FROM `match` WHERE userId", getUser().getUserId());
		 
		renderJson(response);
	}
	
	/**
	 * 导游者确认
	 */
	public void guider(){
		String method = getRequest().getMethod();
		if ("post".equalsIgnoreCase(method)) { 
            render404();
		}
		String userId = getPara("userId");
		/**
		 * result 0 / 1
		 */
		String result = getPara("result");
		String guiderId = getUser().getUserId();
		
		
		if(!notNull(Require.me()
    			.put(userId, "userId can not be null")
    			.put(result,"guider choose result can not be null"))){
    		return;
    	}
		//从Match表查出用户id=userid and 导游id = guiderid的记录 状态置为2 
		String sql = "SELECT * FROM `match` where userId=? and guiderId=? AND travelId NOT IN(SELECT travelId FROM travel)";
		Match match = Match.dao.findFirst(sql,userId,guiderId);
		
		if(match == null){
			renderFailed("not found matching records");
			return;
		}
		
		if(result.equals("0")){
			match.setCurrent(Match.CODE_FAIL_GUIDER_REFUSE);
		}else if(result.equals("1")){
			match.setCurrent(Match.CODE_GUIDER_ACCEPT);
		}else{
			renderArgumentError("result is not valid");
			return;
		}
		match.update();
		//将本次匹配的出游Id添加至user表
		getUser().set("travelIdGuider", match.getTravelId());
		
		//将结果推送至出游者用户
		/**
		 * 集成友盟推送,暂用返回json
		 */
		DatumResponse response = new DatumResponse();
		User user = User.user.findById(userId);
		if(user == null){
	    	response.setCode(Code.FAIL).setMessage("not found user");
	    }else {
	    	HashMap<String, Object> map = new HashMap<String, Object>(user.getAttrs());
            map.remove("password");
            response.setDatum(map);
	    }
	    renderJson(response);
	}
	
	/**
	 * 出游者发出请求
	 */
	public void start(){
		String method = getRequest().getMethod();
		if ("post".equalsIgnoreCase(method)) { 
            render404();
		}
		
		//判断能否进行请求
		if(getUser().getFlag() == false){
			renderFailed("user can't match");
			return;
		}
		
		//获取筛选条件
		int sex = getParaToInt("sex");
		String schoolId = getPara("schoolId");
		String majorId = getPara("majorId");
		String enrollment = getPara("enrollment");
		String constellation = getPara("constellation");
		String birthdayMin = getPara("birthdayMin");
		String birthdayMax = getPara("birthdayMax");
		String remark = getPara("remark");
		String time = getPara("time");
		
		if(!notNull(Require.me()
    			.put(sex, "sex password can not be null")
    			.put(schoolId, "schoolId can not be null")
    			.put(majorId, "majorId can not be null")
    			.put(enrollment, "enrollment can not be null")
    			.put(constellation, "constellation can not be null")
    			.put(birthdayMin, "min birthday can not be null")
    			.put(birthdayMax, "max birthday can not be null")
    			.put(time, "time can not be null"))){
    		return;
    	}
		//筛选出符合条件的用户
		String sql = "select * from user where sex=? and schoolId=? and majorId=? and enrollment=? and constellation=? and birthday>? and birthday<? and userId!=?";
		List<User> lo = User.user.find(sql,sex,schoolId,majorId,enrollment,constellation,birthdayMin,birthdayMax,getUser().getUserId());
		
		//保存数据
		String userId = getUser().userId();
		String travelId = RandomUtils.randomCustomUUID();
		for(int i = 0; i < lo.size(); i++){
			new Match()
					.set("matchId", RandomUtils.randomCustomUUID())
					.set("userId", userId)
					.set("guiderId", lo.get(i).getUserId())
					.set("schoolId", schoolId)
					.set("flagUser", true)
					.set("flagGuider", true)
					.set("current", "1")
					.set("time", time)
					.set("remark", remark)
					.set("travelId", travelId)
					.save();
		}
		
		//将发送请求的出游者匹配状态置为不能匹配
		getUser().set("flag",false).update();
		//将本次匹配的出游Id添加至user表
		getUser().set("travelIdUser", travelId).update();
		//清除当前出游者的聊天列表
		
		//推送消息
			/**
			 * 集成友盟推送,暂用返回json
			 */
		DatumResponse response = new DatumResponse();
		if(lo.isEmpty()){
        	response.setCode(Code.FAIL).setMessage("no guider");
        }else {
        	response.setDatum(lo);
        }
        renderJson(response);	
	}
	
//	/**
//	 * 查询当前用户的匹配情况
//	 */
//	public void progress(){
//		//没有发出请求，请求已发出，导游确认中，没有符合的导游，没有收到请求，出游者确认中，出游中，导游中
//		String userId = getUser().getUserId();
//		String guiderId = getUser().getUserId();
//		//判断出游用户是否确认
//		String sqlUser = "select * from `match` where userId=? and current=3";
//		//判断导游是否确认
//		String sqlGuider = "select * from `match` where userId=? and current=2";
//		//判断是否发出请求
//		String sqlSend = "select * from `match` where userId=? and current=1";
//		
//		//出游者
//		Match match = Match.dao.findFirst(sqlUser,userId);
//		if(match != null){
//			// 出游者已经确认 匹配成功
//		}
//		match = Match.dao.findFirst(sqlGuider,userId);
//		if(match != null){
//			// 导游已经确认
//		}
//		match = Match.dao.findFirst(sqlSend,userId);
//		if(match != null){
//			//当前已经发出请求
//		}else{
//			//当前没有进行匹配/没有符合的导游		
//		}
//	}
}
