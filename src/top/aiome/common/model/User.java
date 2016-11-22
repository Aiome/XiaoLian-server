package top.aiome.common.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

import top.aiome.common.model.base.BaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author malongbo
 * @date 2015/2/13
 */
public class User extends BaseUser<User> {
	public static String USER_ID = "userId";
	public static String LOGIN_NAME = "mobile";
	public static String USER_NAME = "userName";
	public static String PASSWORD = "password";
	public static String SEX = "sex";
	public static String CREATION_DATE = "creatDate";
	public static String AVATAR = "profileImage";

	
	private static final long serialVersionUID = 1L;
	public static final User user = new User();

    /**
     * 获取用户id*
     * @return 用户id
     */
    public String userId() {
        return getStr(USER_ID);      
    }
    /**
     * 获取学校名称
     * @param schoolId
     * @return 学校名
     */
    public static String getSchoolName(String schoolId){
    	return College.dao.findById(schoolId).getName();
    }
    /**
     * 获取专业名称
     * @param majorId
     * @return 专业名
     */
    public static String getMajorName(String majorId){
    	return Major.dao.findById(majorId).getName();
    }
    /**
     * 获取用户年龄
     * @param userId
     * @return 年龄(周岁)
     */
    public static int getAge(String userId){
    	Date date = new Date();
    	Date d = Db.findFirst("select birthday from user where userId=?",userId).getDate("birthday");

    	long day=(date.getTime()-d.getTime())/(24*60*60*1000) + 1;
    	int age= (int) (day / 365);
    	
    	return age;
    }

    /**
     * 检查值是否有效*
     * @param sex 性别值
     * @return 有效性
     */
    public static final boolean checkSex(int sex) {
        
        return sex == 1 || sex == 0;
    }
    
	@Override
	public Map<String, Object> getAttrs() {
		return super.getAttrs();
	}
}
