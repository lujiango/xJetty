package org.github.x.jetty.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    /**
     * �ú����βζ�Ӧ��URL�������֣�Ĭ���뺯���β���ͬ���ơ�
     * 
     * @return String
     */
    String name() default "";

    /**
     * ������URL��δ����ʱ��ȱʡֵ�����������URL�г��ֵ���ֵ,������ַ�������ȱʡֵ��
     * 
     * @return String
     */
    String defaultValue() default "~n$U@l!L4^";

    /**
     * Java�ֶ�ӳ�䵽JSON�ֶζ�Ӧ���������ͣ�Ĭ��ΪAuto���Զ��ƶϡ�
     * 
     * @return Type
     */
    Type type() default Type.Auto;

    /**
     * Java�����ֶ�ֵ���л����ַ����ĸ�ʽ������
     * 
     * @return String
     */
    String format() default "";

    /** Java�ֶ�ӳ�䵽JSON�ֶε��������ͳ�������type����ʹ�á� */
    enum Type {
        String, Number, Boolean, Object, Array, Auto, Ignore
    }
}