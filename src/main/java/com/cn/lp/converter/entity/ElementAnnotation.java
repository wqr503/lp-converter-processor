package com.cn.lp.converter.entity;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;

/**
 * 注解封装类
 */
public class ElementAnnotation {

    private AnnotationMirror annotationType;

    private TypeName typeName;

    public static ElementAnnotation build(AnnotationMirror annotationMirror) {
        TypeName typeName = TypeName.get(annotationMirror.getAnnotationType());
        if(TypeChangeMethodAnnotation.ANNOTATION_TYPE_NAME.equals(typeName)) {
            return TypeChangeMethodAnnotation.build(annotationMirror);
        }
        if(BeanConverterMapperAnnotation.ANNOTATION_TYPE_NAME.equals(typeName)) {
            return BeanConverterMapperAnnotation.build(annotationMirror);
        }
        if(BeanConverterMappingAnnotation.ANNOTATION_TYPE_NAME.equals(typeName)) {
            return BeanConverterMappingAnnotation.build(annotationMirror);
        }
        if(JudgeEmptyMethodAnnotation.ANNOTATION_TYPE_NAME.equals(typeName)) {
            return JudgeEmptyMethodAnnotation.build(annotationMirror);
        }
        if(CheckFieldSettingAnnotation.ANNOTATION_TYPE_NAME.equals(typeName)) {
            return CheckFieldSettingAnnotation.build(annotationMirror);
        }
        if(DefaultValueMethodAnnotation.ANNOTATION_TYPE_NAME.equals(typeName)) {
            return DefaultValueMethodAnnotation.build(annotationMirror);
        }
        if(JudgeSameMethodAnnotation.ANNOTATION_TYPE_NAME.equals(typeName)) {
            return JudgeSameMethodAnnotation.build(annotationMirror);
        }
        ElementAnnotation elementAnnotation = new ElementAnnotation();
        elementAnnotation.init(annotationMirror);
        return elementAnnotation;
    }

    protected void init(AnnotationMirror annotationMirror) {
        this.annotationType = annotationMirror;
        this.typeName = TypeName.get(annotationMirror.getAnnotationType());
    }

    public AnnotationMirror getAnnotationType() {
        return annotationType;
    }

    public TypeName getTypeName() {
        return typeName;
    }
}
