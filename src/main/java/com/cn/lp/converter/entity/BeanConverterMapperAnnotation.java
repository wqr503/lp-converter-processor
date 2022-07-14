package com.cn.lp.converter.entity;

import com.cn.lp.converter.annotation.ConverterMapper;
import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * ConverterMapper 注解
 */
public class BeanConverterMapperAnnotation extends ElementAnnotation{

    public static final TypeName ANNOTATION_TYPE_NAME = ClassName.get(ConverterMapper.class);

    private Map<String, String> reNameMap = new HashMap<>();

    public static BeanConverterMapperAnnotation build(AnnotationMirror annotationMirror) {
        BeanConverterMapperAnnotation elementAnnotation = new BeanConverterMapperAnnotation();
        elementAnnotation.init(annotationMirror);
        List<ReNameFieldAnnotation> reNameField = elementAnnotation.getReNameField();
        Map<String, String> reNameMap = new HashMap<>();
        for (ReNameFieldAnnotation reNameFieldAnnotation : reNameField) {
            reNameMap.put(reNameFieldAnnotation.getTargetName(), reNameFieldAnnotation.getSourceName());
        }
        elementAnnotation.reNameMap = reNameMap;
        return elementAnnotation;
    }

    public String getSourceFieldName(String name) {
        String targetName = reNameMap.get(name);
        if(targetName != null) {
            return targetName;
        }
        return name;
    }

    /** 是否忽略泛型 */
    public boolean isIgnoreGenericType() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(), "ignoreGenericType", true);
    }

    /** 是否实现spring接口 */
    public boolean isImplSpringInterface() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(), "implSpringInterface", false);
    }

    /** 是否忽略空值 */
    public boolean isIgnoreEmpty() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(), "ignoreEmpty", false);
    }

    /** 获取检查字段列表 */
    public List<String> getAssignFieldName() {
        List<AnnotationValue> assignFieldName = ElementAide.getAnnotationValue(getAnnotationType(),
            "assignFieldName", new ArrayList<>());
        if(BlankAide.isBlank(assignFieldName)) {
            return Collections.EMPTY_LIST;
        }
        List<String> resultList = new ArrayList<>();
        for (AnnotationValue annotationValue : assignFieldName) {
            resultList.add((String) annotationValue.getValue());
        }
        return resultList;
    }

    /** 获取改名字段 */
    public List<ReNameFieldAnnotation> getReNameField() {
        List<AnnotationValue> assignAnnotation = ElementAide.getAnnotationValue(getAnnotationType(),
            "reNameField", new ArrayList<>());
        if(BlankAide.isBlank(assignAnnotation)) {
            return Collections.EMPTY_LIST;
        }
        List<ReNameFieldAnnotation> resultList = new ArrayList<>();
        for (AnnotationValue annotationValue : assignAnnotation) {
            resultList.add(ReNameFieldAnnotation.build((AnnotationMirror) annotationValue.getValue()));
        }
        return resultList;
    }

    /** 获取不生效注解 - TpeChangeMethod */
    public List<TypeMirror> getAssignIgnoreAnnotation() {
        List<AnnotationValue> assignAnnotation = ElementAide.getAnnotationValue(getAnnotationType(),
            "assignIgnoreAnnotation", new ArrayList<>());
        if(BlankAide.isBlank(assignAnnotation)) {
            return Collections.EMPTY_LIST;
        }
        List<TypeMirror> resultList = new ArrayList<>();
        for (AnnotationValue annotationValue : assignAnnotation) {
            resultList.add((TypeMirror) annotationValue.getValue());
        }
        return resultList;
    }

    /** 获取生效注解 - TpeChangeMethod */
    public List<TypeMirror> getAssignAnnotation() {
        List<AnnotationValue> assignAnnotation = ElementAide.getAnnotationValue(this.getAnnotationType(),
            "assignAnnotation", new ArrayList<>());
        if(BlankAide.isBlank(assignAnnotation)) {
            return Collections.EMPTY_LIST;
        }
        List<TypeMirror> resultList = new ArrayList<>();
        for (AnnotationValue annotationValue : assignAnnotation) {
            resultList.add((TypeMirror) annotationValue.getValue());
        }
        return resultList;
    }

    /** 获取是否匹配类型 */
    public boolean isMatchType() {
        return ElementAide.getAnnotationValue(this.getAnnotationType(),
            "matchType", false);
    }

}
