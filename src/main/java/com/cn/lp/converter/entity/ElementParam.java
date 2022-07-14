package com.cn.lp.converter.entity;

import com.cn.lp.converter.processor.ProcessorContext;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 元素参数封装类
 */
public class ElementParam {
    /** 参数名 */
    private String name;
    /** 注解名 */
    private List<ElementAnnotation> annotationList = new ArrayList<>();
    /** 参数类型 */
    private ElementType type;

    public static ElementParam create(Element element, Map<TypeVariable, ElementType> argEntityMap, ProcessorContext processorContext) {
        ElementParam param = new ElementParam();
        param.name = element.getSimpleName().toString();
        param.type = ElementType.create(element.asType(), argEntityMap);
        List<ElementAnnotation> annotationList = new ArrayList<>();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            annotationList.add(ElementAnnotation.build(annotationMirror));
        }
        param.annotationList = annotationList;
        return param;
    }

    public String getName() {
        return name;
    }

    public ElementType getType() {
        return type;
    }

    public List<ElementAnnotation> getAnnotationList() {
        return annotationList;
    }
}
