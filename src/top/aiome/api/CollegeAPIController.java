/** 
* 文件名称:.java
* 姓		名:马红岩
* 学		号:2014011791
* 班		级:6班
* 时		间:2016年11月26日
* 程序说明:
*			
**/ 
package top.aiome.api;

import java.util.List;

import top.aiome.common.Require;
import top.aiome.common.bean.Code;
import top.aiome.common.bean.DatumResponse;
import top.aiome.common.model.College;
import top.aiome.common.model.Major;

public class CollegeAPIController extends BaseAPIController{
/**
	 * 根据学校ID查询所选学校的专业
	 */
	public void searchMajor(){
		String collegeId = getPara("collegeId");
		
		if(!notNull(Require.me()
    			.put(collegeId, "collegeId can not be null"))){
    		return;
    	}	
		List<Major> lm = Major.dao.find("select *  from `major` where `collegeID`=?",collegeId);
		DatumResponse response = new DatumResponse();
        
        if (lm.isEmpty()) {
            response.setCode(Code.FAIL).setMessage("not found");
        } else {
            response.setDatum(lm);
        }

        renderJson(response);
	}
	/**
	 * 查询所选学校经纬度
	 */
	public void searchLaAndLo(){
		String collegeName = getPara("collegeName");
		
		if(!notNull(Require.me()
    			.put(collegeName, "collegeName can not be null"))){
    		return;
    	}	
		List<College> lc = College.dao.find("select lantitude,longitude  from `college` where `name`=?",collegeName);
		DatumResponse response = new DatumResponse();
        
        if (lc.isEmpty()) {
            response.setCode(Code.FAIL).setMessage("not found");
        } else {
            response.setDatum(lc);
        }

        renderJson(response);
	}
	
	public void searchCollege(){
		String keyWord = getPara("keyWord");
		
		if(!notNull(Require.me()
    			.put(keyWord, "keyWord can not be null"))){
    		return;
    	}
		
		StringBuffer sb = new StringBuffer();
		//搜 河大 能搜索到 *河**大*
		for (int i = 0; i < keyWord.length(); i++) {
			sb.append('%');
			sb.append(keyWord.charAt(i));
		}
		sb.append('%');
		keyWord = sb.toString();
			
		List<College> lc = College.dao.find("select * from `college` where `name` like '"+ keyWord + "' ORDER BY binary CONVERT(`name` USING GBK) ASC");
		DatumResponse response = new DatumResponse();
        
        if (lc.isEmpty()) {
            response.setCode(Code.FAIL).setMessage("not found");
        } else {
            response.setDatum(lc);
        }

        renderJson(response);
	}
}
