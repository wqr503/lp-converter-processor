package com.cn.lp.converter.entity;

import com.cn.lp.converter.annotation.CheckFieldSetting;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import java.util.ArrayList;
import java.util.List;

/**
 * CheckFieldSetting 注解
 */
public class CheckFieldSettingAnnotation extends ElementAnnotation {

    public static final TypeName ANNOTATION_TYPE_NAME = ClassName.get(CheckFieldSetting.class);

    public static CheckFieldSettingAnnotation build(AnnotationMirror annotationMirror) {
        CheckFieldSettingAnnotation elementAnnotation = new CheckFieldSettingAnnotation();
        elementAnnotation.init(annotationMirror);
        return elementAnnotation;
    }

    /** 获取生效注解 - TpeChangeMethod */
    public List<AnnotationValue> getCheckFieldAnnotation() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(), "checkFieldAnnotation", new ArrayList<AnnotationValue>());
    }

    /** 获取不生效注解 - TpeChangeMethod */
    public List<AnnotationValue> getCheckFieldIgnoreAnnotation() {
        return ElementAide.getAnnotationValue(getAnnotationType(), "checkFieldIgnoreAnnotation", new ArrayList<AnnotationValue>());
    }

    /** 获取检查字段列表 */
    public List<AnnotationValue> getCheckFieldName() {
        return ElementAide.getAnnotationValue(getAnnotationType(), "checkFieldName", new ArrayList<AnnotationValue>());
    }

}
