package com.cps.middleware.db.router.dynamic;

import com.cps.middleware.db.router.DBContextHolder;
import com.cps.middleware.db.router.annotation.DBRouterStrategy;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cps
 * @description: TODO
 * @date 2024/3/19 15:34
 * @OtherDescription: Other things
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DynamicMybatisPlugin implements Interceptor {

    //（from|into|update)[\\s]{1,}(\\w{1,}) => 匹配满足： form或者into或者update+至少一个特殊字符（空格）+至少一个单词字符
    //[\\s] 是指下一个字符为\s ,\s是指特殊字符； {1,} 表示前面的字符至少出现一次（1可以换成其他数字; \w表示数字和字母
    // ｜表示或； 【】表示只能在中括号中的字符才能匹配 （）也没有特殊意思就是表示一个整体
    //主要就是来匹配 sql语句中 “...... from Lottery ...."
    //Pattern.CASE_INSENSITIVE 表示忽略大小写 （主要就是处理FROM INTO UPDATE的情况，怕和前面的from into update匹配不上）
    private Pattern pattern = Pattern.compile("(from|into|update)[\\s]{1,}(\\w{1,})", Pattern.CASE_INSENSITIVE);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //获取StatementHandler
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        //获取自定义注解判断是否进行分表操作
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        Class<?> clazz= Class.forName(className);
        DBRouterStrategy dbRouterStrategy = clazz.getAnnotation(DBRouterStrategy.class);
        if(null == dbRouterStrategy || !dbRouterStrategy.splitTable()){
            return invocation.proceed();
        }

        //获取SQL
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();

        //替换SQL表名 USER 为 USER_03,获得一个处理完数据的匹配对象
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        //判断匹配对象能不能和正则表达式匹配上
        if(matcher.find()){
            //获取匹配对象中被匹配的哪部分去掉头尾空格，也就是获得了表名
            tableName = matcher.group().trim();
        }
        //断言表名是否有
        assert null != tableName;
        //将匹配对象中所匹配的部位替换成方法内部的字符串，返回就是替换之后的完整Text
        String replaceSql = matcher.replaceAll(tableName + "_" + DBContextHolder.getTBKey());

        //通过反射修改SQL语句 ，获得这个属性的所有方法
        Field field = boundSql.getClass().getDeclaredField("sql");
        //开启权限，提高检查的速度
        field.setAccessible(true);
        field.set(boundSql, replaceSql);
        field.setAccessible(false);

        return invocation.proceed();
    }
}
