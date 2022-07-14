package com.cn.lp.converter.entity;

import com.cn.lp.converter.annotation.ConverterMapping;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;

/**
 * ConverterMapping 注解
 */
public class BeanConverterMappingAnnotation extends ElementAnnotation{

    public static final TypeName ANNOTATION_TYPE_NAME = ClassName.get(ConverterMapping.class);

    public static BeanConverterMappingAnnotation build(AnnotationMirror annotationMirror) {
        BeanConverterMappingAnnotation elementAnnotation = new BeanConverterMappingAnnotation();
        elementAnnotation.init(annotationMirror);
        return elementAnnotation;
    }

    /** 是否忽略泛型 */
    public boolean isIgnoreGenericType() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(), "ignoreGenericType", true);
    }

    /** 是否忽略空值 */
    public boolean isIgnoreEmpty() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(), "ignoreEmpty", false);
    }

    /** 获取是否匹配类型 */
    public boolean isMatchType() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(),
            "matchType", false);
    }

}
