package com.cn.lp.converter.generator;

import com.cn.lp.converter.entity.*;
import com.cn.lp.converter.exception.EmptyMethodException;
import com.cn.lp.converter.processor.ProcessorContext;
import com.cn.lp.converter.utils.BlankAide;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import java.util.*;

/**
 * 普通方法生成器
 */
public class MethodGenerator {

    protected ElementMethod elementMethod;

    protected boolean defaultMethod;

    protected Map<TypeName, ElementAnnotation> annotationMap = new HashMap<>();

    public static MethodGenerator create(ElementMethod elementMethod) {
        MethodGenerator methodGenerator = new MethodGenerator();
        methodGenerator.elementMethod = elementMethod;
        methodGenerator.defaultMethod = elementMethod.isDefaultMethod();
        methodGenerator.annotationMap = new HashMap<>(elementMethod.getAnnotationMap());
        return methodGenerator;
    }

    public void mergeAnnotationList(Collection<ElementAnnotation> annotationList) {
        for (ElementAnnotation elementAnnotation : annotationList) {
            this.annotationMap.put(elementAnnotation.getTypeName(), elementAnnotation);
        }
    }

    public Map<TypeName, ElementAnnotation> getAnnotationMap() {
        return annotationMap;
    }

    public MethodGenerator setDefaultMethod(boolean defaultMethod) {
        this.defaultMethod = defaultMethod;
        return this;
    }

    public ElementMethod getElementMethod() {
        return elementMethod;
    }

    public boolean isDefaultMethod() {
        return defaultMethod;
    }

    public MethodSpec buildSpec(MethodHolder methodHolder, ProcessorContext processorContext) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(this.elementMethod.getName())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .returns(this.elementMethod.getResType().getTypeName());
        for (ElementParam argType : this.elementMethod.getParamList()) {
            builder.addParameter(argType.getType().getTypeName(), argType.getName());
        }
        for (TypeParameterElement typeParameterElement : this.elementMethod.getArgTypeList()) {
            builder.addTypeVariable(TypeVariableName.get(typeParameterElement));
        }
        builder.addStatement("throw new $T()", ClassName.get(EmptyMethodException.class));
        return builder.build();
    }

    public ElementAnnotation getAnnotation(TypeName typeName) {
        return annotationMap.get(typeName);
    }

    public boolean isTypeChangeMethod() {
        return BlankAide.isNotBlank(this.annotationMap.get(TypeChangeMethodAnnotation.ANNOTATION_TYPE_NAME));
    }

    public boolean isJudgeEmptyMethod() {
        return BlankAide.isNotBlank(this.annotationMap.get(JudgeEmptyMethodAnnotation.ANNOTATION_TYPE_NAME));
    }

    public boolean isJudgeSameMethod() {
        return BlankAide.isNotBlank(this.annotationMap.get(JudgeSameMethodAnnotation.ANNOTATION_TYPE_NAME));
    }

    public boolean isDefaultValueMethod() {
        return BlankAide.isNotBlank(this.annotationMap.get(DefaultValueMethodAnnotation.ANNOTATION_TYPE_NAME));
    }

}
