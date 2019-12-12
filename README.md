# netty-web

netty-web在netty4的基础上做了轻量级封装及增强，提供方便快捷开发web应用，特别适合用来开发api类应用。

本人在此基础上将其整合为SpringBoot-Starter，同时增加了文件下载功能

## features

* 支持路由
* 支持rest风格url
* 支持表单参数注入
* 支持文件上传（仅支持小文件，大约64mb）
* SpringBoot-Starter
* 支持异步文件下载

## 文件下载
- 1、在配置文件中指定netty.web.download-flag
- 2、@Router的value中包含download-flag的Controller方法将被标识为文件下载
- 3、controller返回FileMessage对象
- 4、将根据FileMessage中的att找到文件位置，异步传输给客户端

### FileMessage对象说明：

正常情况下，FileMessage的file属性为true，att为下载文件的绝对路径；
如果提供文件下载功能的Controller方法不能返回文件，可设置FileMessage的file属性为false，att中附上的对象将转为Json字符串发送给客户端

```java
public class FileMessage {

    private Boolean file;

    /**
     * 如果是文件的话，att为文件路径，否则为返回的json对象
     */
    private Object att;


    public FileMessage(Boolean isFile, Object att){
        this.file = isFile;
        this.att = att;
    }

    ...
}
```

## hello world

```java
@Component
@Controller
public class Helloworld {

    @Router("/hello1/{name}")
    public void hello1(WebContext context, @PathValue("name") String name) {
        context.getResponse().writeBody("Hello," + name + "\r\n");
    }

    @Router("/hello2")
    public void hello2(WebContext context, @ParamValue("name") String name) {
        context.getResponse().writeBody("Hello," + name + "\r\n");
    }

    @Router(value = "/hello3", method = HttpMethod.POST)
    public void hello3(WebContext context, @BodyValue byte[] body) throws IOException {
    }

    @Router(value = "/hello5", method = HttpMethod.POST)
    public void hello5(WebContext context, @Dto Requset requset) throws IOException {
        context.getResponse().writeBody("Hello," + requset.getFileName() + "\r\n");
    }

    //返回值可以任意类型，netty-web默认把放回值序列化json放回给客户端
    @Router("/hello4/{name}")
    public Hello hello4(@PathValue("name") String name) {
        return new Result(200, "Hello," + name);
    }

    @Router(value = "/download", method = HttpMethod.POST)
    public FileMessage download(@Dto Hello request){
        return new FileMessage(true,request.getFileName());
    }

    @Router(value = "/download2", method = HttpMethod.GET)
    public FileMessage download2(@ParamValue("name") String fileName){
        return new FileMessage(true,fileName);
    }
}

class Hello {

    private int code;
    private String msg;
    private String fileName;

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
```

## 支持的注解

* @Controller 
    Controller类不需要继续特定接口，只需要使用@Controller注解就可以了
* @Router
    注册路由信息，其中method属性标记http请求方法，默认的情况下为HttpMethod.GET，注册为通用处理方法可以使用HttpMethod.ALL,相当于spring mvc @RequestMapping
* @PathValue
    用于处理方法参数注入，@PathValue是用来获得请求url中的动态参数,相当于spring mvc @PathVariable
* @ParamValue
    用于处理方法参数注入，@ParamValue是用来获得请求参数,相当于spring mvc @RequestParam
* @BodyValue
    用于处理方法参数注入，@BodyValue是用来获得请求playload
* @Dto
    用于处理方法参数注入，将请求体由Json自动解析为Java对象    
    
    
## 路由
* 非正则路由
    "/api/index",只能匹配/api/index请求url
* 正则路由
    "/api/{appkey:[A-Z]+}/videos/{pagesize:\d+}",其中appkey必须为大写字母&pagesize为数字，/api/ABC/videos/10能匹配上，/api/abc/videos/10则不能

## cookie、session
* 不支持session
* 对cookie的获取可以通过webContext获取Http请求头中的cookie
* 推荐使用token来判断状态


  
