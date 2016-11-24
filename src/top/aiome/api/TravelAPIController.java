package top.aiome.api;

import java.util.List;

import com.jfinal.aop.Before;

import top.aiome.common.Require;
import top.aiome.common.bean.Code;
import top.aiome.common.bean.DatumResponse;
import top.aiome.common.model.Travel;
import top.aiome.common.model.User;
import top.aiome.interceptor.TokenInterceptor;

@Before(TokenInterceptor.class)
public class TravelAPIController  extends BaseAPIController{
	/**
	 * 返回出游记录
	 */
	public void getTravelList(){
		User user = getUser();
		String userId = user.getUserId();
		
		if(!notNull(Require.me()
				.put(userId, "userId can not be null"))){		
			return;
		}        
		List<Travel> lo = Travel.dao.find("select * from `travel` where userId=?",userId);
        
		DatumResponse response = new DatumResponse();
        
        if (lo.isEmpty()) {
            response.setCode(Code.FAIL).setMessage("This user have no travel");
        } else {
            response.setDatum(lo);
        }

        renderJson(response);
	}
}
