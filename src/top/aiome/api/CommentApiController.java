package top.aiome.api;

import java.util.List;

import top.aiome.common.Require;
import top.aiome.common.bean.Code;
import top.aiome.common.bean.DatumResponse;
import top.aiome.common.model.CollegeComment;

public class CommentApiController extends BaseAPIController{
	public void collegeComment(){
		String method = getRequest().getMethod();
        if ("get".equalsIgnoreCase(method)) { //查询评论
            getCollegeComment();
        } else if ("post".equalsIgnoreCase(method)) { //添加评论
            setCollegeComment();
        } else {
            render404();
        }
	}
	//获取学校评论
	private void getCollegeComment(){
		String collegeId = getPara("collegeId");
		if(!notNull(Require.me()
				.put(collegeId, "collegeId can not be null"))){		
			return;
		}        
		List<CollegeComment> lo = CollegeComment.dao.find("select * from `college_comment` where schoolId=?",collegeId);
        
		DatumResponse response = new DatumResponse();
        
        if (lo.isEmpty()) {
            response.setCode(Code.FAIL).setMessage("the school has no comment");
        } else {
            response.setDatum(lo);
        }

        renderJson(response);
	}
	//添加学校评论
	private void setCollegeComment(){
	
	}
}
