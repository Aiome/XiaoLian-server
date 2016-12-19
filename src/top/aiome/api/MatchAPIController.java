
package top.aiome.api;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.easemob.server.example.api.SendMessageAPI;
import com.easemob.server.example.comm.ClientContext;
import com.easemob.server.example.comm.EasemobRestAPIFactory;
import com.easemob.server.example.comm.body.CommandMessageBody;
import com.easemob.server.example.comm.body.TextMessageBody;
import com.easemob.server.example.comm.constant.MsgTargetType;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;

import top.aiome.common.Require;
import top.aiome.common.bean.BaseResponse;
import top.aiome.common.bean.Code;
import top.aiome.common.bean.DatumResponse;
import top.aiome.common.model.Major;
import top.aiome.common.model.Match;
import top.aiome.common.model.Travel;
import top.aiome.common.model.User;
import top.aiome.common.utils.RandomUtils;
import top.aiome.interceptor.TokenInterceptor;
/**
 * 删除导游:		DELETE api/match/userDelGuider
 * 删除出游:		DELETE api/match/guiderDelUser
 * 获取导游聊天列表:	GET    api/match/guiderChatList
 * 获取出游者聊天列表:GET    api/match/userChatList
 * 出游者确认:		POST   api/match/matchEnd
 * 导游选择: 		POST   api/match/guider
 * 开始匹配:	POST   api/match/start
 * 开始旅程	flag 4->5
 * 旅程结束      
 * 获取当前用户类型     
 * flag
 * 1->未出游    	 (可发送邀请)  		旅程结束/注册        	4->1
 * 2->匹配中         (显示匹配中) 		 开始匹配 		1->2
 * 3->匹配成功     (显示开始旅程) 	 出游者确认                2->3
 * 4->出游中         (显示结束出游)   	开始旅程                     3->4
 * @author Aiome
 *
 */
@Before(TokenInterceptor.class)
public class MatchAPIController extends BaseAPIController{
	
	/**
	 * 匹配流程:
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
	 * 
	 * 
	 * 
	 */
	public void guiderGetRemark(){
		/**
		 * 导游显示remark条件
		 * 1.收到了请求
		 * 2.还没有接受
		 * 3.出游表没有记录
		 */	
		String userId = getPara("userId");
		User user = getUser();
		String guiderId = user.getUserId();
		user = User.user.findById(guiderId);
		String travelId = user.getTravelIdGuider();
		
		if(!notNull(Require.me()
    			.put(userId, "userId can not be null")
    			.put(travelId, "user has not yet issued a request"))){
    		return;
    	}
		
		String sql = "SELECT time,remark FROM `match` where userId=? and guiderId=? and travelId=? and current=1";
		String sqlt = "select * from travel where travelId=?";
		Match match = Match.dao.findFirst(sql,userId,guiderId,travelId);
		Travel travel = Travel.dao.findFirst(sqlt,travelId);
		DatumResponse datum = new DatumResponse();
		if(match == null){
			datum.setCode(Code.NO_REMARK);
			datum.setDatum(match);
			System.out.println("1");
			datum.setMessage("hide remark");
			renderJson(datum);
			return;
		}
		if(travel != null){
			datum.setCode(Code.NO_REMARK);
			datum.setDatum(match);
			System.out.println("2");
			datum.setMessage("hide remark");
			renderJson(datum);
			return;
		}else{
			datum.setCode(Code.SHOW_REMARK);
			datum.setDatum(match);
			datum.setMessage("show remark");
			renderJson(datum);
		}
	}
	public void userGetRemark(){
		/**
		 * 出游者显示remark条件
		 * 	1.出游者发出了请求
		 *  2.导游接受了
		 *  3.出游表没有记录
		 */
		String guiderId = getPara("guiderId");
		User user = getUser();
		String userId = user.getUserId();
		user = User.user.findById(userId);
		String travelId = user.getTravelIdUser();
		
		if(!notNull(Require.me()
    			.put(guiderId, "guiderId can not be null")
    			.put(travelId, "user has not yet issued a request"))){
    		return;
    	}
		
		String sql = "SELECT time,remark FROM `match` where userId=? and guiderId=? and travelId=? and current=2";
		String sqlt = "select * from travel where travelId=?";
		Match match = Match.dao.findFirst(sql,userId,guiderId,travelId);
		Travel travel = Travel.dao.findFirst(sqlt,travelId);
		DatumResponse datum = new DatumResponse();
		if(match == null){
			datum.setCode(Code.NO_REMARK);
			datum.setDatum(match);
			datum.setMessage("hide remark");
			renderJson(datum);
			return;
		}
		if(travel != null){
			datum.setCode(Code.NO_REMARK);
			datum.setDatum(match);
			datum.setMessage("hide remark");
			renderJson(datum);
			return;
		}else{
			datum.setCode(Code.SHOW_REMARK);
			datum.setDatum(match);
			datum.setMessage("show remark");
			renderJson(datum);
		}		
	}
	/**
	 * 从出游聊天列表删除导游
	 */
	public void userDelGuider(){
		String method = getRequest().getMethod();
		if (!"delete".equalsIgnoreCase(method)) { 
            render404();
		}
		User user = getUser();
		String guiderId = getPara("guiderId");
		String userId = user.getUserId();
		String travelId = user.getTravelIdUser();
		
		if(!notNull(Require.me()
    			.put(guiderId, "guiderId can not be null")
    			.put(travelId, "user has not yet issued a request"))){
    		return;
    	}
		
		String sql = "SELECT * FROM `match` where userId=? and guiderId=? and travelId=?";
		Match match = Match.dao.findFirst(sql,userId,guiderId,travelId);
		if(match == null){
			renderFailed("not found matching records");
			return;
		}
		
		match.set("flagGuider", false).update();
		
		BaseResponse response = new BaseResponse();
		response.setCode(Code.SUCCESS);
		response.setMessage("success");
		
		renderJson(response);
	
	}
	/**
	 * 从导游聊天列表删除出游者
	 */
	public void guiderDelUser(){
		String method = getRequest().getMethod();
		if (!"delete".equalsIgnoreCase(method)) { 
            render404();
		}
		//获取要删除的联系人userId
		//将 userid = userId guider = current guider置为false
		User guider = getUser();
		String userId = getPara("userId");
		String guiderId = guider.getUserId();
		String travelId = guider.getTravelIdGuider();
		if(!notNull(Require.me()
    			.put(userId, "userId can not be null")
    			.put(travelId, "user has not served as a guider"))){
    		return;
    	}
		String sql = "SELECT * FROM `match` where userId=? and guiderId=? and travelId=?";
		Match match = Match.dao.findFirst(sql,userId,guiderId,travelId);
		if(match == null){
			renderFailed("not found matching records");
			return;
		}
		match.set("flagUser", false).update();
		
		BaseResponse response = new BaseResponse();
		response.setCode(Code.SUCCESS);
		response.setMessage("success");
		
		renderJson(response);
	}
	/**
	 * 获取导游聊天列表
	 */
	public void guiderChatList(){
		User guider = getUser();
		String guiderId = guider.getUserId();
		String travelId = guider.getTravelIdGuider();
		//若用户还未担任过导游，则没有聊天列表
		if(!notNull(Require.me()
    			.put(travelId, "user has not served as a guider"))){
    		return;
    	}
		System.out.println("guiderid:"+guiderId);
		System.out.println("gtaverid" + travelId);
		String sql = "SELECT * FROM `match` where guiderId=? and travelId=? and flagUser=1";
		List<Match> lm = Match.dao.find(sql,guiderId,travelId);
		
		DatumResponse response = new DatumResponse();
		if(lm.isEmpty()){
        	response.setCode(Code.FAIL).setMessage("not found available chat list");
        }else {
        	EasemobRestAPIFactory factory = ClientContext.getInstance().init(ClientContext.INIT_FROM_PROPERTIES).getAPIFactory();
        	SendMessageAPI message = (SendMessageAPI)factory.newInstance(EasemobRestAPIFactory.SEND_MESSAGE_CLASS);  	
            Map<String, String> map = new HashMap<String, String>();
            map.put("guider", "1");
        	String to = guiderId;
        	String from = "";
        	for(int i = 0; i < lm.size(); i++){
//        		from = User.user.findById(lm.get(i).getUserId()).getUserName();
        		from = lm.get(i).getUserId();
        		message.sendMessage(new TextMessageBody(MsgTargetType.USERS, new String[]{to}, from, map, "我接受了邀请!"));
        	}
        	response.setCode(Code.SUCCESS).setMessage("success");
        }
        renderJson(response);
	}
	/**
	 * 获取出游者聊天列表
	 */
	public void userChatList(){
		
		User user = getUser();
		String userId = user.getUserId();
		String travelId = user.getTravelIdUser();
		//若用户还未发出过请求，则没有聊天列表
		if(travelId.isEmpty()){
			renderJson(new BaseResponse(Code.NO_TRAVEL_RECORED,"user has not yet issued a request!"));
    		return;
    	}
		String sql = "SELECT * FROM `match` where userId=? and travelId=? and flagGuider=1 and current=3";
		List<Match> lm = Match.dao.find(sql,userId,travelId);
		if(lm.isEmpty()){
			lm = Match.dao.find("SELECT * FROM `match` where userId=? and travelId=? and flagGuider=1 and current=2",userId,travelId);
		}
		
		DatumResponse response = new DatumResponse();
		if(lm.isEmpty()){
        	response.setCode(Code.FAIL).setMessage("not found available chat list");
        }else {
        	EasemobRestAPIFactory factory = ClientContext.getInstance().init(ClientContext.INIT_FROM_PROPERTIES).getAPIFactory();
        	SendMessageAPI message = (SendMessageAPI)factory.newInstance(EasemobRestAPIFactory.SEND_MESSAGE_CLASS);
        	String to = userId;
        	String from = "";
        	for(int i = 0; i < lm.size(); i++){
//        		from = User.user.findById(lm.get(i).getGuiderId()).getUserName();
        		from = lm.get(i).getGuiderId();
        		message.sendMessage(new CommandMessageBody(MsgTargetType.USERS, new String[]{userId}, from, null, "show"));
        	}
        	
        	response.setCode(Code.SUCCESS).setMessage("success");
        }
        renderJson(response);		
        
	}
	/**
	 * 结束旅程
	 */
	public void travelEnd(){
		/**
		 * 判断用户类型,只有出游者才能操作
		 */
		int flag = getUser().getFlag();
		if(flag != 4){
			renderJson(new BaseResponse(Code.TRAVEL_ERROR,"can not end travel!"));
			return;
		}
		
		
		//将当前出游记录置为出游结束
		String travelId = getUser().getTravelIdUser();
		String sql = "select * from travel where travelId=?";
		Travel travel = Travel.dao.findFirst(sql,travelId);
		
		if(travel == null){
			renderFailed("not found matching records");
			return;
		}
		User user = User.user.findById(travel.getGuiderId());
		user.set("flag", 1).update();
		//将出游者状态置为未出游
		getUser().set("flag", 1).update();
		travel.set("flag",false);
		travel.update();
		renderJson(new BaseResponse(Code.SUCCESS,"success!"));
	}
	/**
	 * 开始旅程
	 */
	public void travelStart(){
		/**
		 * 判断用户类型，只有出游者且匹配成功才能操作
		 */
		int flag = getUser().getFlag();
		if(flag != 3){
			renderJson(new BaseResponse(Code.TRAVEL_ERROR,"can not start travel!"));
			return;
		}
		//将出游者状态置为出游中
		getUser().set("flag", 4).update();
		renderJson(new BaseResponse(Code.SUCCESS,"success!"));
	}
	/**
	 * 出游者再次确认
	 */
	public void matchEnd(){
		String method = getRequest().getMethod();
		if (!"post".equalsIgnoreCase(method)) { 
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
		//将出游者匹配状态置为出游中
		getUser().set("flag", 3).update();
		 
		renderJson(response);
	}
	
	/**
	 * 导游者确认
	 */
	public void guider(){
		/**
		 * 
		 * 添加判断当前匹配是否结束
		 * 
		 * 
		 */
		String method = getRequest().getMethod();
		if (!"post".equalsIgnoreCase(method)) { 
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
			renderJson(new BaseResponse(Code.NO_RECORED,"not found matching records!"));
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
		if (!"post".equalsIgnoreCase(method)) { 
            render404();
		}
		System.out.println(getUser().getFlag());
		//判断能否进行请求
		if((getUser().getFlag() != 1)){
			renderJson(new BaseResponse(Code.MATCH_ERROR,"user can not match!"));
			return;
		}
		
		
		//获取筛选条件
		int sex = getParaToInt("sex",5);
		String schoolId = getPara("schoolId");
		String majorId = getPara("majorId");
		
		String enrollment = getPara("enrollment");
		String constellation = getPara("constellation");
		String birthdayMin = getPara("birthdayMin");
		String birthdayMax = getPara("birthdayMax");
		String remark = getPara("remark");
		String time = getPara("time");

		StringBuffer sb = new StringBuffer("select * from user where schoolId=" + schoolId + " and userId!='" + getUser().getUserId() + "'");
		if(sex != 5){
			sb.append(" and sex=" + sex);
		}
		if(majorId != null){
			sb.append(" and majorId=" + majorId);
		}
		if(enrollment != null){
			sb.append(" and enrollment=" + enrollment);
		}
		if(constellation != null){
			sb.append(" and constellation='" + constellation + "'");
		}
		if((birthdayMin != null) && (birthdayMax != null)){
			sb.append(" and birthday>'" + birthdayMin + "'" + " and birthday<'" + birthdayMax + "'");
		}

		if(!notNull(Require.me()
    			.put(schoolId, "schoolId can not be null")
    			.put(time, "time can not be null"))){
    		return;
    	}
		//筛选出符合条件的用户
//		String sql = "select * from user where sex=? and schoolId=? and majorId=? and enrollment=? and constellation=? and birthday>? and birthday<? and userId!=?";
		String sql = sb.toString();
		System.out.println(sql);
		List<User> lo = User.user.find(sql);
		//推送消息
		/**
		 * 集成友盟推送,暂用返回json
		 */
	
		DatumResponse response = new DatumResponse();
		if(lo.isEmpty()){
        	response.setCode(Code.NO_GUIDER).setMessage("no guider");
        	renderJson(response);
        	return;
        }else {
    		
        	response.setDatum(lo);
        }
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
			
		//将发送请求的出游者匹配状态置为匹配中
		getUser().set("flag",2).update();
		//将本次匹配的出游Id添加至user表
		getUser().set("travelIdUser", travelId).update();

		//将本次匹配的出游Id添加至user表
    	for(int i = 0; i < lo.size(); i++){
    		User.user.findFirst("select * from `user` where userId=?",lo.get(i).getUserId())
    		.set("travelIdGuider", travelId).update();
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
