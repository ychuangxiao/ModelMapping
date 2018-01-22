package com.banditcat.compiler.helper;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * 文件名称：{@link AnnotationHelper}
 * <br/>
 * 功能描述：注解帮助类
 * <br/>
 * 创建作者：banditcat
 * <br/>
 * 创建时间：2018/1/22 11:13
 * <br/>
 * 修改作者：banditcat
 * <br/>
 * 修改时间：2018/1/22 11:13
 * <br/>
 * 修改备注：
 */
public class AnnotationHelper {

    public static AnnotationMirror getAnnotationMirror(Element element, Class<?> annotationType) {
        AnnotationMirror result = null;

        String annotationClassName = annotationType.getName();
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals(annotationClassName)) {
                result = mirror;
                break;
            }
        }

        return result;
    }

    public static AnnotationValue getAnnotationValue(AnnotationMirror annotation, String field) {
        if (annotation != null) {
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation
                    .getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().toString().equals(field)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * @param value
     * @return
     */
    public static String getPackName(String value)
    {
        return  value.substring(0,value.lastIndexOf('.'));
    }

    /**
     * @param value
     * @return
     */
    public static String getClassName(String value)
    {
        return  value.substring(value.lastIndexOf('.')+1);
    }

    public static String toUpperCamelCase(String filedName) {
        return filedName.substring(0, 1).toUpperCase().concat(filedName.substring(1));
    }

    public static String toLowerCamelCase(String filedName) {
        return filedName.substring(0, 1).toLowerCase().concat(filedName.substring(1));
    }
}
