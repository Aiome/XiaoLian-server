/** 
* 文件名称:.java
* 姓		名:马红岩
* 学		号:2014011791
* 班		级:6班
* 时		间:2016年12月3日
* 程序说明:
*			
**/ 
package top.aiome.api;

import com.easemob.server.example.api.SendMessageAPI;
import com.easemob.server.example.comm.ClientContext;
import com.easemob.server.example.comm.EasemobRestAPIFactory;
import com.easemob.server.example.comm.body.TextMessageBody;
import com.easemob.server.example.comm.constant.MsgTargetType;

public class temp extends BaseAPIController{
	public void test(){
		EasemobRestAPIFactory factory = ClientContext.getInstance().init(ClientContext.INIT_FROM_PROPERTIES).getAPIFactory();

//		IMUserAPI user = (IMUserAPI)factory.newInstance(EasemobRestAPIFactory.USER_CLASS);
		SendMessageAPI message = (SendMessageAPI)factory.newInstance(EasemobRestAPIFactory.SEND_MESSAGE_CLASS);
		message.sendMessage(new TextMessageBody(MsgTargetType.USERS, new String[]{"test1"}, "test2", null, "我接受了你的邀请!"));
		renderText("success");
	}
}
