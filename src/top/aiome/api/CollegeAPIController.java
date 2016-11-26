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

public class CollegeAPIController extends BaseAPIController{
	public void college(){
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
			
		List<College> lc = College.dao.find("select name,coid from `college` where `name` like '"+ keyWord + "' ORDER BY binary CONVERT(`name` USING GBK) ASC");
		DatumResponse response = new DatumResponse();
        
        if (lc.isEmpty()) {
            response.setCode(Code.FAIL).setMessage("not found");
        } else {
            response.setDatum(lc);
        }

        renderJson(response);
	}
}
