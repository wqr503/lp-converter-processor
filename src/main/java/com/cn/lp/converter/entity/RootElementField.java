package com.cn.lp.converter.entity;

import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeVariable;
import java.util.*;

/**
 * BeanConvert interface中source 和 target 实体的属性封装类
 */
public class RootElementField {
    /** 注解列表 */
    private Map<TypeName, ElementAnnotation> annotationMap = new HashMap<>();
    /** 类型 */
    private ElementType type;
    /** 名字 */
    private String name;
    /** get方法 */
    private ElementMethod getMethod;
    /** set方法 */
    private ElementMethod setMethod;

    public static RootElementField create(Element element, Map<TypeVariable, ElementType> argEntityMap, Map<String, ElementMethod> methodMap) {
        RootElementField field = new RootElementField();
        ElementType argType = argEntityMap.get(element.asType());
        if(argType != null) {
            field.type = argType;
        } else {
            field.type = ElementType.create(element.asType(), argEntityMap);
        }
        field.name = element.getSimpleName().toString();
        Map<TypeName, ElementAnnotation> annotationMap = new HashMap<>();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            ElementAnnotation elementAnnotation = ElementAnnotation.build(annotationMirror);
            annotationMap.put(elementAnnotation.getTypeName(), elementAnnotation);
        }
        field.annotationMap = annotationMap;
        String getMethodName = "get" + StringUtils.capitalize(field.getName()) + "-";
        ElementMethod getMethod = methodMap.get(getMethodName);
        if(getMethod != null) {
            field.getMethod = getMethod;
        }
        if(TypeName.get(field.getType().getType()).equals(TypeName.BOOLEAN)) {
            getMethodName = "is" + StringUtils.capitalize(field.getName()) + "-";
            getMethod = methodMap.get(getMethodName);
            if(getMethod != null) {
                field.getMethod = getMethod;
            }
        }
        ElementMethod setMethod = methodMap.get(ElementAide.buildSetMethodName(field.getType().getType(), field.getName()));
        if(setMethod != null) {
            field.setMethod = setMethod;
        }
        return field;
    }

    /** 合并注解 */
    public void mergeAnnotationList(Collection<ElementAnnotation> annotationList) {
        for (ElementAnnotation elementAnnotation : annotationList) {
            ElementAnnotation oldAnnotation = annotationMap.get(elementAnnotation.getTypeName());
            if(BlankAide.isNotBlank(oldAnnotation)) {
                continue;
            }
            this.annotationMap.put(elementAnnotation.getTypeName(), elementAnnotation);
        }
    }

    public Map<TypeName, ElementAnnotation> getAnnotationMap() {
        return annotationMap;
    }

    public ElementType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ElementMethod getGetMethod() {
        return getMethod;
    }

    public ElementMethod getSetMethod() {
        return setMethod;
    }
}
