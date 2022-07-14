package com.cn.lp.converter.entity;

import com.cn.lp.converter.annotation.DefaultValueMethod;
import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import java.util.ArrayList;
import java.util.List;

/**
 * DefaultValueMethod 注解
 * */
public class DefaultValueMethodAnnotation extends ElementAnnotation {

    public static final TypeName ANNOTATION_TYPE_NAME = ClassName.get(DefaultValueMethod.class);

    public static DefaultValueMethodAnnotation build(AnnotationMirror annotationMirror) {
        DefaultValueMethodAnnotation elementAnnotation = new DefaultValueMethodAnnotation();
        elementAnnotation.init(annotationMirror);
        return elementAnnotation;
    }

    /** 获取生效注解 - TpeChangeMethod */
    public List<AnnotationValue> getAssignAnnotation() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(), "assignAnnotation", new ArrayList<AnnotationValue>());
    }

    /** 判断是否有注解过滤 -TpeChangeMethod */
    public boolean isHasAssign() {
        return BlankAide.isNotBlank(getAssignAnnotation()) || BlankAide.isNotBlank(getAssignIgnoreAnnotation());
    }

    /** 获取检查字段列表 */
    public List<AnnotationValue> getAssignFieldName() {
        return ElementAide.getAnnotationValue(getAnnotationType(), "assignFieldName", new ArrayList<AnnotationValue>());
    }

    /** 获取不生效注解 - TpeChangeMethod */
    public List<AnnotationValue> getAssignIgnoreAnnotation() {
        return ElementAide.getAnnotationValue(getAnnotationType(), "assignIgnoreAnnotation", new ArrayList<AnnotationValue>());
    }

    /** 是否主方法(优先级提高) - TpeChangeMethod */
    public boolean isPrimary() {
        return ElementAide.getAnnotationValue(getAnnotationType(), "primary", false);
    }

    public String createMethodSign(ElementMethod elementMethod) {
        return elementMethod.getResType().getTypeName().toString();
    }

}
