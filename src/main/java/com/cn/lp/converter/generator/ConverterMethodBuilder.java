package com.cn.lp.converter.generator;

import com.cn.lp.converter.entity.*;
import com.cn.lp.converter.exception.ProcessorException;
import com.cn.lp.converter.processor.ProcessorContext;
import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * convertTo方法构建器
 */
public class ConverterMethodBuilder implements GeneratorMethodBuilder {

    private ElementMethod elementMethod;

    private RootElementClass targetEntity;

    private RootElementClass sourceEntity;

    private boolean ignoreGenericType;

    private boolean ignoreEmpty;

    private boolean isMatchType;

    private CheckFieldSettingAnnotation checkFieldAnnotation;

    private BeanConverterMapperAnnotation beanConverterMapperAnnotation;

    public void init(ConverterMethodGenerator methodGenerator, ElementInterface rootInterface) {
        this.elementMethod = methodGenerator.getElementMethod();
        Map<TypeVariable, RootElementClass> entityMap = methodGenerator.getConvertInterface().getEntityMap();
        this.targetEntity = entityMap.get(elementMethod.getResType().getType());
        this.sourceEntity = entityMap.get(elementMethod.getParamList().get(0).getType().getType());
        BeanConverterMapperAnnotation beanConverterMapperAnnotation = (BeanConverterMapperAnnotation)
            rootInterface.getAnnotation(BeanConverterMapperAnnotation.ANNOTATION_TYPE_NAME);
        this.beanConverterMapperAnnotation = beanConverterMapperAnnotation;
        BeanConverterMappingAnnotation beanConverterMappingAnnotation = (BeanConverterMappingAnnotation)
            methodGenerator.getAnnotation(BeanConverterMappingAnnotation.ANNOTATION_TYPE_NAME);
        CheckFieldSettingAnnotation checkFieldAnnotation = (CheckFieldSettingAnnotation)
            methodGenerator.getAnnotation(CheckFieldSettingAnnotation.ANNOTATION_TYPE_NAME);
        if(checkFieldAnnotation == null) {
            checkFieldAnnotation = (CheckFieldSettingAnnotation)
                rootInterface.getAnnotation(CheckFieldSettingAnnotation.ANNOTATION_TYPE_NAME);
        }
        this.checkFieldAnnotation = checkFieldAnnotation;
        this.ignoreGenericType = beanConverterMapperAnnotation.isIgnoreGenericType();
        if(beanConverterMappingAnnotation != null) {
            this.ignoreGenericType = beanConverterMappingAnnotation.isIgnoreGenericType();
        }
        this.ignoreEmpty = beanConverterMapperAnnotation.isIgnoreEmpty();
        if(beanConverterMappingAnnotation != null) {
            this.ignoreEmpty = beanConverterMappingAnnotation.isIgnoreEmpty();
        }
        this.isMatchType = beanConverterMapperAnnotation.isMatchType();
        if(beanConverterMappingAnnotation != null) {
            this.isMatchType = beanConverterMappingAnnotation.isMatchType();
        }
    }

    /**
     * // 如果存在DefaultValueMethod方法
     * if(param0 == null) {
     *     return null;
     * }
     * $T target = new $T();
     * $T field1 = param0.get$T();
     * // 如果存在JudgeEmptyMethod方法
     * if(JudgeEmptyMethod(field1)) {
     *      // 如果存在DefaultValueMethod方法
     *      field1 = DefaultValueMethod()；
     * }
     * // 如果存在TypeChangeMethod方法
     * target.set(TypeChangeMethod(field1));
     *
     * // 如果不存在DefaultValueMethod方法
     * if(param0 == null) {
     *     return null;
     * }
     * $T target = new $T();
     * $T field1 = param0.get$T();
     * // 如果存在JudgeEmptyMethod方法
     * if(!JudgeEmptyMethod(field1)) {
     *      // 如果存在TypeChangeMethod方法
     *      target.set(TypeChangeMethod(field1));
     * }
     *
     *
     * checkField逻辑
     * 1.如果不存在TypeChangeMethod
     * 2.类型不匹配
     * 3.source没有对应字段的get方法
     * 4.source没有对应的字段
     *
     */
    @Override
    public void createMethod(MethodSpec.Builder builder, MethodHolder methodHolder, ProcessorContext processorContext) {
        TypeName targetEntityTypeName = TypeName.get(targetEntity.getType());
        List<RootElementField> targetFieldList = new ArrayList<>();
        for (RootElementField field : targetEntity.getFieldMap().values()) {
            if( BlankAide.isNotBlank(field.getSetMethod())) {
                targetFieldList.add(field);
            }
        }
        List<String> errorFieldList = new ArrayList<>();
        List<String> warnFieldList = new ArrayList<>();
        builder.beginControlFlow("if($L == null)", SOURCE_PARAM_NAME);
        builder.addStatement("return null");
        builder.endControlFlow();
        builder.addStatement("$T target = new $T()", targetEntityTypeName, targetEntityTypeName);
        for(int index = 0; index < targetFieldList.size(); index ++) {
            RootElementField targetField = targetFieldList.get(index);
            String fieldName = beanConverterMapperAnnotation.getSourceFieldName(targetField.getName());
            RootElementField sourceField = sourceEntity.getFieldMap().get(fieldName);
            if(BlankAide.isNotBlank(sourceField)) {
                ElementMethod sourceGetMethod = sourceField.getGetMethod();
                if(BlankAide.isNotBlank(sourceGetMethod)) {
                    Optional<MethodGenerator> methodGeneratorOpt = methodHolder.matchTypeChangeMethod(
                        sourceField,
                        sourceField.getGetMethod().getResType(),
                        targetField.getType().getTypeName(),
                        processorContext,
                        this.ignoreGenericType);
                    // 先判断是否有TypeChangeMethod 或者 类型相同
                    if(methodGeneratorOpt.isPresent() || ElementAide.judgeType(sourceField, targetField,
                        this.ignoreGenericType, processorContext) || !isMatchType) {
                        this.processField(builder, sourceField, targetField, methodHolder, processorContext,
                            index, methodGeneratorOpt.orElse(null));
                    } else {
                        String message = "source Entity not has " + targetField.getName() +
                            " field GetMethod type is " + targetField.getType().getTypeName().toString();
                        if(judgeFieldCheck(targetField)) {
                            errorFieldList.add(message);
                        } else {
                            warnFieldList.add(message);
                        }
                    }
                } else {
                    String message = "source Entity not has " + targetField.getName() + " field";
                    if(judgeFieldCheck(targetField)) {
                        errorFieldList.add(message);
                    } else {
                        warnFieldList.add(message);
                    }
                }
            } else {
                String message = "source Entity not has " + targetField.getName() + " field";
                if(judgeFieldCheck(targetField)) {
                    errorFieldList.add(message);
                } else {
                    warnFieldList.add(message);
                }
            }
        }
        if(BlankAide.isNotBlank(warnFieldList)) {
            StringBuilder sb = new StringBuilder("generate " + this.elementMethod.getName() + " fail: \r\n");
            for (String message : warnFieldList) {
                sb.append(message).append("\r\n");
            }
            processorContext.getMessager().printMessage(Diagnostic.Kind.WARNING, sb.toString());
        }
        if(BlankAide.isNotBlank(errorFieldList)) {
            StringBuilder sb = new StringBuilder("generate " + this.elementMethod.getName() + " fail: \r\n");
            for (String message : errorFieldList) {
                sb.append(message).append("\r\n");
            }
            throw new ProcessorException(sb.toString());
        }
        builder.addStatement("this.$L($L,target)", "post" + StringUtils.capitalize(elementMethod.getName()), SOURCE_PARAM_NAME);
        builder.addStatement("return target");
    }

    private void processField(MethodSpec.Builder builder, RootElementField sourceField, RootElementField targetField,
        MethodHolder methodHolder, ProcessorContext processorContext, int fieldIndex, MethodGenerator typeChangeMethod) {
        String spaceParamName = "field" + fieldIndex;
        builder.addStatement("$T $L = $L.$L()",
            sourceField.getGetMethod().getResType().getTypeName(),
            spaceParamName,
            SOURCE_PARAM_NAME,
            sourceField.getGetMethod().getName()
            );
        Optional<MethodGenerator> defaultValueMethodOpt = methodHolder.matchDefaultValueMethod(sourceField,
            sourceField.getGetMethod().getResType(), ignoreGenericType);
        Optional<MethodGenerator> judgeEmptyMethodOpt = methodHolder.matchJudgeEmptyMethod(sourceField,
            sourceField.getGetMethod().getResType(), processorContext, ignoreGenericType
        );
        boolean ignoreEmpty = (this.ignoreEmpty || defaultValueMethodOpt.isPresent() ||
            judgeEmptyMethodOpt.isPresent())
            && !ElementAide.isUnboxType(sourceField.getGetMethod().getResType().getTypeName());
        boolean needEndControlFlow = false;
        // 判断是否有DefaultValueMethod, 如果有则肯定有赋值
        if(defaultValueMethodOpt.isPresent() && !ElementAide.isUnboxType(sourceField.getGetMethod().getResType().getTypeName())) {
            if(judgeEmptyMethodOpt.isPresent()) {
                builder.beginControlFlow("if($L($L))", judgeEmptyMethodOpt.get().getElementMethod().getName(),
                    spaceParamName);
            } else {
                builder.beginControlFlow("if($L == null)", spaceParamName);
            }
            builder.addStatement("$L = $L()", spaceParamName, defaultValueMethodOpt.get().getElementMethod().getName());
            builder.endControlFlow();
        } else if(ignoreEmpty) {
            if(judgeEmptyMethodOpt.isPresent()) {
                builder.beginControlFlow("if(!$L($L))", judgeEmptyMethodOpt.get().getElementMethod().getName(),
                    spaceParamName);
            } else {
                builder.beginControlFlow("if($L != null)", spaceParamName);
            }
            needEndControlFlow = true;
        }
        if(typeChangeMethod != null) {
            builder.addStatement("target.$L($L($L))", targetField.getSetMethod().getName(),
                typeChangeMethod.getElementMethod().getName(), spaceParamName);
        } else {
            // 判断是否有泛型，泛型策略
            builder.addStatement("target.$L(($T)$L)", targetField.getSetMethod().getName(),
                targetField.getType().getTypeName(), spaceParamName);
        }
        if(needEndControlFlow) {
            builder.endControlFlow();
        }
    }

    // 判断字段是否检查
    private boolean judgeFieldCheck(RootElementField targetField) {
        if(this.checkFieldAnnotation == null) {
            return false;
        }
        List<AnnotationValue> checkFieldAnnotation = this.checkFieldAnnotation.getCheckFieldAnnotation();
        List<AnnotationValue> checkFieldIgnoreAnnotation = this.checkFieldAnnotation.getCheckFieldIgnoreAnnotation();
        List<AnnotationValue> checkFieldName = this.checkFieldAnnotation.getCheckFieldName();
        if(BlankAide.isNotBlank(checkFieldName)) {
            for (AnnotationValue data : checkFieldName) {
                if(data.getValue().equals(targetField.getName())) {
                    return true;
                }
            }
        }
        if(BlankAide.isNotBlank(checkFieldAnnotation)) {
            for (AnnotationValue annotationClass : checkFieldAnnotation) {
                ElementAnnotation elementAnnotation = targetField.getAnnotationMap().get(TypeName.get((TypeMirror) annotationClass.getValue()));
                if(BlankAide.isNotBlank(elementAnnotation)) {
                    return true;
                }
            }
        }
        if(BlankAide.isNotBlank(checkFieldIgnoreAnnotation)) {
            for (AnnotationValue annotationClass : checkFieldIgnoreAnnotation) {
                ElementAnnotation elementAnnotation = targetField.getAnnotationMap().get(TypeName.get((TypeMirror) annotationClass.getValue()));
                if(BlankAide.isBlank(elementAnnotation)) {
                    return true;
                }
            }
        }
        return BlankAide.isBlank(checkFieldName) && BlankAide.isBlank(checkFieldAnnotation) && BlankAide.isBlank(checkFieldIgnoreAnnotation);
    }

}
