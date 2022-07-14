package com.cn.lp.converter.generator;

import com.cn.lp.converter.entity.DefaultValueMethodAnnotation;
import com.cn.lp.converter.entity.ElementAnnotation;
import com.cn.lp.converter.entity.RootElementField;
import com.cn.lp.converter.utils.BlankAide;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;


/**
 * DefaultValueMethod持有者
 */
public class DefaultValueMethodHolder {

    /** 注解过滤的方法 */
    private LinkedList<MethodGenerator> assignList = new LinkedList<>();

    /** 普通方法 */
    private MethodGenerator methodGenerator;

    public static DefaultValueMethodHolder create(MethodGenerator methodGenerator) {
        DefaultValueMethodHolder defaultValueMethodHolder = new DefaultValueMethodHolder();
        DefaultValueMethodAnnotation annotation = (DefaultValueMethodAnnotation) methodGenerator
            .getAnnotation(DefaultValueMethodAnnotation.ANNOTATION_TYPE_NAME);
        if(annotation.isHasAssign()) {
            defaultValueMethodHolder.addAssignMethod(methodGenerator);
        } else {
            defaultValueMethodHolder.updateMethod(methodGenerator);
        }
        return defaultValueMethodHolder;
    }

    public void addAssignMethod(MethodGenerator methodGenerator) {
        DefaultValueMethodAnnotation annotation = (DefaultValueMethodAnnotation) methodGenerator
            .getAnnotation(DefaultValueMethodAnnotation.ANNOTATION_TYPE_NAME);
        if(annotation.isPrimary()) {
            assignList.addFirst(methodGenerator);
        } else {
            assignList.addLast(methodGenerator);
        }
    }

    public void updateMethod(MethodGenerator methodGenerator) {
        DefaultValueMethodAnnotation annotation = (DefaultValueMethodAnnotation) methodGenerator
            .getAnnotation(DefaultValueMethodAnnotation.ANNOTATION_TYPE_NAME);
        if(this.methodGenerator == null || annotation.isPrimary()) {
            this.methodGenerator = methodGenerator;
        }
    }

    public Optional<MethodGenerator> getMatchFieldMethod(RootElementField field) {
        if(BlankAide.isNotBlank((Collection<MethodGenerator>) this.assignList)) {
            for (MethodGenerator generator : assignList) {
                DefaultValueMethodAnnotation annotation = (DefaultValueMethodAnnotation) generator
                    .getAnnotation(DefaultValueMethodAnnotation.ANNOTATION_TYPE_NAME);
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
