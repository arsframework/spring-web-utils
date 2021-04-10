# spring-web-utils
该插件基于Spring web提供工具包。

## 1 环境依赖
- Java JDK1.8+

## 2 部署配置
在Maven配置中添加如下依赖：
```
<plugin>
    <groupId>com.arsframework</groupId>
    <artifactId>spring-web-utils</artifactId>
    <version>1.0.0</version>
</plugin>
```

## 3 功能描述
该插件基于Spring web框架，并在此基础上提供开发工具包，如针对表单请求参数的重命名。

### 3.1 表单参数对象字段重命名
通过```com.arsframework.spring.web.utils.param.Rename```注解可以对表单参数对象字段进行参数重命名，该注解不影响参数校验处理逻辑。

- 表单参数重命名处理器配置
```
@Configuration
public class ParameterRenameConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this.renamingProcessor());
    }

    @Bean
    ParameterRenamingProcessor renamingProcessor() {
        return new ParameterRenamingProcessor();
    }
}
```

- 表单参数对象
```
import com.arsframework.spring.web.utils.param.Rename;

public class UserSaveParam {
    @Rename("user_code")
    private String userCode;

    private String userName;

    ...
}
```

- Controller接口方法
```
public void saveUser(UserSaveParam param) {

}
```
## 4 版本更新日志
