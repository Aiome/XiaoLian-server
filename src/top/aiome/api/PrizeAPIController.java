package top.aiome.api;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;

import top.aiome.common.Require;
import top.aiome.common.bean.Code;
import top.aiome.common.bean.DatumResponse;
import top.aiome.common.model.Allocateprize;
import top.aiome.common.model.CollegeComment;
import top.aiome.common.model.Match;
import top.aiome.common.model.Remainprize;
import top.aiome.common.model.Travel;
import top.aiome.common.model.User;
import top.aiome.common.model.UserComment;
import top.aiome.common.utils.RandomUtils;
import top.aiome.interceptor.TokenInterceptor;

@Before(TokenInterceptor.class)
public class PrizeAPIController extends BaseAPIController {
	/**
	 * 获得未分配奖品列表及余量
	 */
	@Clear
	public void getRemainPrizeList(){
		List<Remainprize> lrp = Remainprize.dao.find("select * from `remainprize`");
        
		DatumResponse response = new DatumResponse();
        
        if (lrp.isEmpty()) {
            response.setCode(Code.FAIL).setMessage("no any prize now");
        } else {
            response.setDatum(lrp);
        }

        renderJson(response);
	}
	
	/**
	 * 根据客户端选择，将被选奖品余量减一，并放进allocateprize表
	 */
	public void managePrize(){
		User user = getUser();
		String userId = user.getUserId();
		String remainPrizeId = getPara("remainPrizeId");
		String prizeName = getPara("prizeName");
		
		if(!notNull(Require.me()
				.put(userId, "userId can not be null"))){		
			return;
		}
		if(!notNull(Require.me()
    			.put(remainPrizeId, "remainPrizeId can not be null"))){
    		return;
    	}
		if(!notNull(Require.me()
    			.put(prizeName, "prizeName can not be null"))){
    		return;
    	}
		
		// 从remainPrize表查出remainPrizeId=remainPrizeId的记录，将余量减一
		Remainprize currentCount = Remainprize.dao.findFirst("select prizeCount from `remainPrize` where remainPrizeId=?",remainPrizeId);
		String sql = "SELECT * FROM `remainPrize` where remainPrizeId=?";
		Remainprize prize  = Remainprize.dao.findFirst(sql,remainPrizeId);
		if(prize == null){
			renderFailed("not found prize records");
			return;
		}
		int nowCount = prize.getInt("prizeCount");
		int newCount = nowCount-1;
		
		prize.setPrizeCount(newCount);
		prize.update();

		// 在已分配奖品表中增加记录
		String allocatePrizeId = RandomUtils.randomCustomUUID();
        new Allocateprize()
		.set("userId", userId)
		.set("allocatePrizeId",allocatePrizeId)
		.set("prizeName", prizeName)
		.save();
		
	}
	
	/**
	 * 获取当前用户的奖品列表
	 */
	public void getAllocatePrizeList(){
		User user = getUser();
		String userId = user.getUserId();
		
		if(!notNull(Require.me()
				.put(userId, "userId can not be null"))){		
			return;
		}
		List<Allocateprize> lap = Allocateprize.dao.find("select * from `allocateprize` where userId=?",userId);
		
		DatumResponse response = new DatumResponse();
        
        if (lap.isEmpty()) {
            response.setCode(Code.FAIL).setMessage("the user has no prize");
        } else {
            response.setDatum(lap);
        }

        renderJson(response);
	}
}
