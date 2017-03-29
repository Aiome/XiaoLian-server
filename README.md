# XiaoLian-server
实训项目“校脸”服务端,搭建api，通过jfinal极速开发框架搭建完成
# 感谢
* [Jfinal](http://www.jfinal.com/)
* [jfinal脚手架项目] (https://github.com/lenbo-ma/jfinal-api-scaffold)

### 资源

[现有的API接口文档](校脸接口开发文档.pdf)

### 数据响应规范

避免手拼json导致的错误，而使用将Java Bean序列化为JSON的方式。

  json数据的根节点使用code字段标识本次响应的状态，如成功、失败、参数缺少或参数值有误，以及服务器错误等；message节点包含服务器响应回来的一些提示信息，主要是方便客户端开发人员在对接接口时定位错误出现的原因。

一般的，响应数据会分为两种，第一种为列表数据，如一组用户信息，另外一种是实体信息，如一条用户信息。data字段值为数组，携带列表数据，datum字段值为json对象，携带实体数据。

如:
```javascript
//实体数据, 此结构对应DatumResponse类
{
  "code": 1,
  "message": "成功查询到该用户信息",
  "datum": {
    "name": "jack",
  "lover": "rose",
  "sex": 1,
  "email": "jack@gmail.com"
  }
}
//列表数据, 此结构对应DataResponse类
{
  "code": 1,
  "message": "成功查询到两条用户信息",
  "data": [{
    "name": "jack",
  "lover": "rose",
  "sex": 1,
  "email": "jack@gmail.com"
  },{
    "name": "rose",
  "lover": "jack",
  "sex": 0,
  "email": "rose@gmail.com"
  }]
}
//登录成功, 此结构对应LoginResponse类
{
  "code": 1,
  "message": "登录成功",
  "constant": {
    "resourceServer": "http://fs.mlongbo.com" //文件地址前缀
  }
  "info": {
    "name": "jack",
  "lover": "rose",
  "sex": 1,
  "email": "jack@gmail.com"
  }
}
//多文件上传, 部分成功，部分失败. 此结构对应FileResponse类
{
  "code": 0, //只要有一个文件上传失败，code就会是0
  "failed": ["file3"]
  "datum": {
    "file1": "/upload/images/file1.jpg",
  "file2": "/upload/images/file2.jpg"
  }
}
//缺少参数
{
  "code": 2,
  "message": "缺少name参数"
}
//参数值有误
{
  "code": 2,
  "message": "sex参数值只能为0或1"
}

//token无效
{
  "code": 422,
  "message": "token值是无效的，请重新登录获取"
}
```

附上本人常用的几种code值:


* 1 ok - 成功状态。查询成功，操作成功等；
* 0 faild - 失败状态
* 2 argument error - 表示请求参数值有误, 或未携带必需的参数值
* 3 帐号已存在
* 4 注册验证码错误
* 500 error - 服务器错误
* 404 not found - 请求的资源或接口不存在
* 422 token error - 未传递token参数,或token值非法

### 请求参数校验

最多的还是非空检查, 这里重点说一下。因此，我写了一个工具。使用方法如下：

```java
String name = getPara("name");
String lover = getPara("lover");
//使用此方式的前提是当前controller类要继承自BaseAPIController类
if (!notNull(Require.me().put(name, "name参数不能为空").put(lover,"lover参数不能为空"))) {
  return;
}

//效果等同于如下代码:
if (StringUtils.isEmpty(name)) {
  renderJson(new BaseResponse(2, "name参数不能为空"));
  return;
}
if (StringUtils.isEmpty(lover)) {
  renderJson(new BaseResponse(2, "lover参数不能为空"));
  return;
}

```
```javascript
//如果没有传递name参数，将会得到如下响应:
{
  "code": 2,
  "message": "name参数不能为空"
}
```

### 已实现的公共模块

公共模块实现了基本功能，你可以根据自己的业务需求自由调整数据字段。

#### Token模块

token, 顾名思义, 表示令牌，用于标识当前用户，同时增加接口的安全性。目前不支持过期策略，也仅支持一个用户一个终端的方式，即用户在一处登录后，再在另一处登录会使之前登录的token失效。

要启用token功能只需要配置TokenInterceptor拦截器类即可。

在使用时，客户端必须在配置了拦截器的接口请求中携带名为"token"的请求参数。

服务端在继承了BaseAPIController类后可以直接调用`getUser();`函数获取当前用户对象. **注意: ** 为了正常地使用getUser函数，必须在登录接口中查出用户对象后，使用类似如下代码建立token与用户对象的映射：
```java
User nowUser = User.user.findFirst(sql, loginName, StringUtils.encodePassword(password, "md5"));

//之后要将token值响应给客户端
String token = TokenManager.getMe().generateToken(nowUser));
```

#### 文件上传模块

在以往的接口开发过程中，我们都是使用一个统一的文件上传接口上传文件后，服务器响应上传成功后的文件地址，客户端再使用业务接口将文件地址作为参数值发送到服务器。这样做的好处之一是便于服务端将文件统一管理，比如做缓存或CDN；另一方面是为了减小耦合度，比如此时要换成七牛CDN存放静态文件，客户端只需要改写文件上传部分的代码即可。

目前的文件上传接口已实现一或多个文件同时上传，客户端在上传时，必须要为每个文件指定一个请求参数名, 参数名用于在上传结束后，根据服务器的响应数据判断哪些文件是上传失败的，哪些是上传成功的，以及成功后的文件地址是什么。

服务器响应实例如下：
```javascript
//全部上传成功
{
  "code": 1,
  "message": "success",
  "datum": {
    "file1": "/upload/images/file1.jpg",
  "file2": "/upload/images/file2.jpg"
  }
}
//全部上传失败
{
  "code": 0,
  "message": "failed",
  "failed": ["file1", "file2"]
}
//部分成功，部分失败
{
  "code": 0, //只要有一个文件上传失败，code就会是0
  "failed": ["file3"]
  "datum": {
    "file1": "/upload/images/file1.jpg",
  "file2": "/upload/images/file2.jpg"
  }
}
```
#### 工具

* Jetty插件: 无需使用tomcat，直接使用maven的jetty插件启动项目；
* ant工具： 一般情况下，我们的项目是在服务端使用maven自动构建的，但在开发过程中，代码经常改变需要重新部署，如果重新打包更新又比较麻烦，因此在服务端使用maven命令重新构建后，可直接执行ant命令将已改动的文件copy到tomcat应用目录。所以，若想正常使用该工具，你需要修改build.xml，将tomapp值修改为你的tomcat应用路径。
