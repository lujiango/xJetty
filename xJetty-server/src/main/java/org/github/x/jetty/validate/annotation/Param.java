package org.github.x.jetty.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    /**
     * 该函数形参对应的URL参数名字，默认与函数形参相同名称。
     * 
     * @return String
     */
    String name() default "";

    /**
     * 参数在URL中未出现时的缺省值。如果参数在URL中出现但无值,则传入空字符串而非缺省值。
     * 
     * @return String
     */
    String defaultValue() default "~n$U@l!L4^";

    /**
     * Java字段映射到JSON字段对应的数据类型，默认为Auto即自动推断。
     * 
     * @return Type
     */
    Type type() default Type.Auto;

    /**
     * Java日期字段值序列化成字符串的格式化串。
     * 
     * @return String
     */
    String format() default "";

    /** Java字段映射到JSON字段的数据类型常量。供type参数使用。 */
    enum Type {
        String, Number, Boolean, Object, Array, Auto, Ignore
    }
}