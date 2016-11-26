package top.aiome.api;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;

import top.aiome.common.Require;
import top.aiome.common.bean.BaseResponse;
import top.aiome.common.bean.Code;
import top.aiome.common.bean.DatumResponse;
import top.aiome.common.model.College;
import top.aiome.common.model.CollegeComment;
import top.aiome.common.model.UserComment;
import top.aiome.common.utils.RandomUtils;
import top.aiome.interceptor.TokenInterceptor;

@Before(TokenInterceptor.class)
public class CommentAPIController extends BaseAPIController{
	@Clear
	public void getUserComment(){
		String userId = getPara("userId");
		if(!notNull(Require.me()
				.put(userId, "userId can not be null"))){		
			return;
		}
		List<UserComment> lu = UserComment.dao.find("select * from `user_comment` where guiderId=?",userId);
		
		DatumResponse response = new DatumResponse();
        
        if (lu.isEmpty()) {
            response.setCode(Code.FAIL).setMessage("the user has no comment");
        } else {
            response.setDatum(lu);
        }

        renderJson(response);
	}
	//添加个人评论
	public void setUserComment(){
		String userId = getPara("userId");
		String comment = getPara("comment");
		int score = getParaToInt("score",0);
		if(!notNull(Require.me()
				.put(userId, "userId can not be null"))){		
			return;
		}
		
		new UserComment()
					.set("commentId", RandomUtils.randomCustomUUID())
					.set("guiderId", userId)
					.set("comment", comment)
					.set("score", score)
					.set("userId", getUser().getUserId())
					.save();
		renderJson(new BaseResponse(Code.SUCCESS, "success"));

	}
	//获取学校评论
	@Clear
	public void getCollegeComment(){
		
		String collegeId = getPara("collegeId");
		if(!notNull(Require.me()
				.put(collegeId, "collegeId can not be null"))){		
			return;
		}        
		List<CollegeComment> lc = CollegeComment.dao.find("select * from `college_comment` where schoolId=?",collegeId);
        
		DatumResponse response = new DatumResponse();
        
        if (lc.isEmpty()) {
            response.setCode(Code.FAIL).setMessage("the school has no comment");
        } else {
            response.setDatum(lc);
        }

        renderJson(response);
	}
	
	//添加学校评论
	public void setCollegeComment(){
		//拒绝除post以外的其他请求
		String method = getRequest().getMethod();
        if (!"post".equalsIgnoreCase(method)) { 
            render404(); 
        }
        
		String collegeId = getPara("collegeId");
		String comment = getPara("comment");
		int score = getParaToInt("score",0);
		//暂时这样上传图片，可能更改
		String image = getPara("image");
		if(!notNull(Require.me()
				.put(collegeId, "collegeId can not be null")
				)){		
			return;
		}
		College college = College.dao.findById(collegeId);
		//学校不存在
		if(college == null){
			renderJson(new BaseResponse(Code.FAIL,"School does not exist "));
			return;
		}
		
		new CollegeComment()
						.set("commentId", RandomUtils.randomCustomUUID())
						.set("schoolId", collegeId)
						.set("comment",comment)
						.set("score", score)
						.set("image", image)
						.set("userId", getUser().getUserId())
						.save();
		renderJson(new BaseResponse(Code.SUCCESS,"success"));
	}
	
	
}
