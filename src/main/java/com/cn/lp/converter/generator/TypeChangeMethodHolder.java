package com.cn.lp.converter.generator;

import com.cn.lp.converter.entity.*;
import com.cn.lp.converter.utils.BlankAide;
import com.squareup.javapoet.TypeName;


import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * TypeChangeMethod持有者
 */
public class TypeChangeMethodHolder {

    /** 注解过滤的方法 */
    private LinkedList<MethodGenerator> assignList = new LinkedList<>();

    /** 普通方法 */
    private MethodGenerator methodGenerator;

    public static TypeChangeMethodHolder create(MethodGenerator methodGenerator) {
        TypeChangeMethodHolder typeChangeMethodHolder = new TypeChangeMethodHolder();
        TypeChangeMethodAnnotation annotation = (TypeChangeMethodAnnotation) methodGenerator
            .getAnnotation(TypeChangeMethodAnnotation.ANNOTATION_TYPE_NAME);
        if(annotation.isHasAssign()) {
            typeChangeMethodHolder.addAssignMethod(methodGenerator);
        } else {
            typeChangeMethodHolder.updateMethod(methodGenerator);
        }
        return typeChangeMethodHolder;
    }

    public void addAssignMethod(MethodGenerator methodGenerator) {
        TypeChangeMethodAnnotation annotation = (TypeChangeMethodAnnotation) methodGenerator
            .getAnnotation(TypeChangeMethodAnnotation.ANNOTATION_TYPE_NAME);
        if(annotation.isPrimary()) {
            assignList.addFirst(methodGenerator);
        } else {
            assignList.addLast(methodGenerator);
        }
    }

    public void updateMethod(MethodGenerator methodGenerator) {
        TypeChangeMethodAnnotation annotation = (TypeChangeMethodAnnotation) methodGenerator
            .getAnnotation(TypeChangeMethodAnnotation.ANNOTATION_TYPE_NAME);
        if(this.methodGenerator == null || annotation.isPrimary()) {
            this.methodGenerator = methodGenerator;
        }
    }

    public Optional<MethodGenerator> getMatchFieldMethod(RootElementField field) {
        if(BlankAide.isNotBlank((Collection<MethodGenerator>) this.assignList)) {
            for (MethodGenerator generator : assignList) {
                TypeChangeMethodAnnotation annotation = (TypeChangeMethodAnnotation) generator
                    .getAnnotation(TypeChangeMethodAnnotation.ANNOTATION_TYPE_NAME);
                for (AnnotationValue annotationClass : annotation.getAssignAnnotation()) {
                    ElementAnnotation elementAnnotation = field.getAnnotationMap().get(TypeName.get((TypeMirror) annotationClass.getValue()));
                    if(BlankAide.isNotBlank(elementAnnotation)) {
                        return Optional.of(generator);
                    }
                }
                for (AnnotationValue annotationClass : annotation.getAssignIgnoreAnnotation()) {
                    ElementAnnotation elementAnnotation = field.getAnnotationMap().get(TypeName.get((TypeMirror) annotationClass.getValue()));
                    if(BlankAide.isBlank(elementAnnotation)) {
                        return Optional.of(generator);
                    }
                }
            }
        }
        return Optional.ofNullable(methodGenerator);
    }
}
