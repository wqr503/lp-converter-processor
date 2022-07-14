package com.cn.lp.converter.entity;

import com.cn.lp.converter.exception.ProcessorException;
import com.cn.lp.converter.processor.ProcessorContext;
import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.util.*;

/**
 * 元素方法封装类
 */
public class ElementMethod {

    /** 方法名 */
    private String name;
    /** 参数列表 */
    private List<ElementParam> paramList = new ArrayList<>();
    /** 返回类型*/
    private ElementType resType;
    /** 是否default方法 */
    private boolean defaultMethod;
    /** 注解列表 */
    private Map<TypeName, ElementAnnotation> annotationMap = new HashMap<>();
    /** 泛型参数列表 */
    private List<? extends TypeParameterElement> argTypeList = new ArrayList<>();
    /** 方法标识 -> 方法名-参数1类型-参数2类型 */
    private String sign;

    /** 静态构造 */
    public static ElementMethod create(ExecutableElement element, Map<TypeVariable, ElementType> argEntityMap, ProcessorContext processorContext) {
        ElementMethod method = new ElementMethod();
        method.name = element.getSimpleName().toString();
        Map<TypeName, ElementAnnotation> annotationMap = new HashMap<>();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            ElementAnnotation elementAnnotation = ElementAnnotation.build(annotationMirror);
            annotationMap.put(elementAnnotation.getTypeName(), elementAnnotation);
        }
        method.annotationMap = annotationMap;
        method.defaultMethod = element.getModifiers().contains(Modifier.DEFAULT);
        StringBuilder sb = new StringBuilder();
        sb.append(method.name).append("-");
        List<ElementParam> paramList = new ArrayList<>();
        method.argTypeList = element.getTypeParameters();
        for (VariableElement parameter : element.getParameters()) {
            ElementParam elementParam = ElementParam.create(parameter, argEntityMap, processorContext);
            ElementType paramType = elementParam.getType();
            if(paramType.isHasArgParam()) {
                sb.append(ElementAide.getSimpleTypeName(paramType.getTypeName())).append("-");
            } else {
                sb.append(paramType.getTypeName()).append("-");
            }
            paramList.add(elementParam);
        }
        method.sign = sb.toString();
        method.resType = ElementType.create(element.getReturnType(), argEntityMap);
        method.paramList = paramList;
        // 判断方式注释是否含有TypeChangeMethod注解
        if(BlankAide.isNotBlank(annotationMap.get(TypeChangeMethodAnnotation.ANNOTATION_TYPE_NAME))) {
            method.checkTypeChangeMethod();
        }
        // 判断方式注释是否含有JudgeEmptyMethod注解
        if(BlankAide.isNotBlank(annotationMap.get(JudgeEmptyMethodAnnotation.ANNOTATION_TYPE_NAME))) {
            method.checkJudgeEmptyMethod();
        }
        // 判断方式注释是否含有DefaultValueMethod注解
        if(BlankAide.isNotBlank(annotationMap.get(DefaultValueMethodAnnotation.ANNOTATION_TYPE_NAME))) {
            method.checkDefaultValueMethod();
        }
        if(BlankAide.isNotBlank(annotationMap.get(JudgeSameMethodAnnotation.ANNOTATION_TYPE_NAME))) {
            method.checkJudgeSameMethod();
        }
        return method;
    }

    /** 校验JudgeSameMethod */
    private void checkJudgeSameMethod() {
        // 校验是否合法
        if(BlankAide.isBlank(this.paramList) || this.paramList.size() != 2) {
            throw new ProcessorException("JudgeSameMethod " + this.getName() + " must has two param");
        }
        TypeName resType = this.resType.getTypeName();
        if(!TypeName.BOOLEAN.equals(resType)) {
            throw new ProcessorException("JudgeSameMethod " + this.getName() + " must return boolean");
        }
        if(!this.isDefaultMethod()) {
            throw new ProcessorException("JudgeSameMethod " + this.getName() + " must default");
        }
    }

    /** 校验DefaultValueMethod */
    private void checkDefaultValueMethod() {
        // 校验是否合法
        if(BlankAide.isNotBlank(this.paramList)) {
            throw new ProcessorException("DefaultValueMethod " + this.getName() + " must not has param");
        }
        TypeName resType = this.resType.getTypeName();
        if(TypeName.VOID.equals(resType)) {
            throw new ProcessorException("TypeChangeMethod " + this.getName() + " must has return");
        }
        if(!this.isDefaultMethod()) {
            throw new ProcessorException("DefaultValueMethod " + this.getName() + " must default");
        }
    }

    /** 校验TypeChangMethod */
    private void checkJudgeEmptyMethod() {
        // 校验是否合法
        if(BlankAide.isBlank(this.paramList) || this.paramList.size() != 1) {
            throw new ProcessorException("JudgeEmptyMethod " + this.getName() + " must only has one param");
        }
        TypeName resType = this.resType.getTypeName();
        if(!TypeName.BOOLEAN.equals(resType)) {
            throw new ProcessorException("JudgeEmptyMethod " + this.getName() + " must return boolean");
        }
        if(!this.isDefaultMethod()) {
            throw new ProcessorException("JudgeEmptyMethod " + this.getName() + " must default");
        }
    }

    /** 校验TypeChangMethod */
    private void checkTypeChangeMethod() {
        // 校验是否合法
        if(BlankAide.isBlank(this.paramList) || this.paramList.size() != 1) {
            throw new ProcessorException("TypeChangeMethod " + this.getName() + " must only has one param");
        }
        TypeName resType = this.resType.getTypeName();
        if(TypeName.VOID.equals(resType)) {
            throw new ProcessorException("TypeChangeMethod " + this.getName() + " must has return");
        }
        if(!this.isDefaultMethod()) {
            throw new ProcessorException("TypeChangeMethod " + this.getName() + " must default");
        }
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

    public List<? extends TypeParameterElement> getArgTypeList() {
        return argTypeList;
    }

    public String getName() {
        return name;
    }

    public List<ElementParam> getParamList() {
        return paramList;
    }

    public ElementType getResType() {
        return resType;
    }

    public boolean isDefaultMethod() {
        return defaultMethod;
    }

    public Map<TypeName, ElementAnnotation> getAnnotationMap() {
        return annotationMap;
    }

    public String getSign() {
        return sign;
    }

    protected ElementMethod setDefaultMethod(boolean defaultMethod) {
        this.defaultMethod = defaultMethod;
        return this;
    }

}
