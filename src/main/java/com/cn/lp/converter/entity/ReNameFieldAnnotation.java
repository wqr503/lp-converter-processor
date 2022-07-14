package com.cn.lp.converter.entity;

import com.cn.lp.converter.annotation.ReNameField;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;

/**
 * 改名字段注解
 */
public class ReNameFieldAnnotation extends ElementAnnotation{

    public static final TypeName ANNOTATION_TYPE_NAME = ClassName.get(ReNameField.class);

    public static ReNameFieldAnnotation build(AnnotationMirror annotationMirror) {
        ReNameFieldAnnotation elementAnnotation = new ReNameFieldAnnotation();
        elementAnnotation.init(annotationMirror);
        return elementAnnotation;
    }

    /** 源字段名 */
    public String getSourceName() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(),
            "sourceName", "");
    }

    /** 目标字段名 */
    public String getTargetName() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(),
            "targetName", "");
    }

}

