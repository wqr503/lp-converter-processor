package com.cn.lp.converter.generator;

import com.cn.lp.converter.entity.*;
import com.cn.lp.converter.exception.ProcessorException;
import com.cn.lp.converter.processor.ProcessorContext;
import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * convertTo方法构建器
 */
public class MergeMethodBuilder implements GeneratorMethodBuilder {

    private ElementMethod elementMethod;

    private RootElementClass targetEntity;

    private RootElementClass sourceEntity;

    private boolean ignoreGenericType;

    private CheckFieldSettingAnnotation checkFieldAnnotation;

    // 输出对象参数名
    public static final String TARGET_PARAM_NAME = "param1";

    // 修改字段集合
    public static final String CHANGE_FIELD_NAMES = "changeFieldNames";

    private boolean isMatchType;

    private BeanConverterMapperAnnotation beanConverterMapperAnnotation;

    public void init(ConverterMethodGenerator methodGenerator, ElementInterface rootInterface) {
        this.elementMethod = methodGenerator.getElementMethod();
        Map<TypeVariable, RootElementClass> entityMap = methodGenerator.getConvertInterface().getEntityMap();
        this.sourceEntity = entityMap.get(elementMethod.getParamList().get(0).getType().getType());
        this.targetEntity = entityMap.get(elementMethod.getParamList().get(1).getType().getType());
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
        this.isMatchType = beanConverterMapperAnnotation.isMatchType();
        if(beanConverterMappingAnnotation != null) {
            this.isMatchType = beanConverterMappingAnnotation.isMatchType();
        }
    }

    /**
     * // 如果存在DefaultValueMethod方法
     * if(param0 == null) {
     *     return MergeResult.build(param1, null);
     * }
     * MergeResult<$T> $L = new MergeResult<>();
     * $T field1 = param0.get$T();
     * // 如果存在DefaultValueMethod方法
     * if(JudgeEmptyMethod(field1)) {
     *     field1 = DefaultValueMethod()
     * }
     * // 如果存在JudgeSameMethod方法
     * if(!JudgeSameMethod(field1, param1.get())) {
     *     param1.set(field1);
     *     changeFieldNames.add($L);
     * }
     *
     * // 如果不存在DefaultValueMethod方法
     * if(param0 == null) {
     *     return MergeResult.build(param1, null);
     * }
     * MergeResult<$T> $L = new MergeResult<>();
     * $T field1 = param0.get$T();
     * // 如果存在JudgeSameMethod方法
     * if(!JudgeSameMethod(field1, param1.get())) {
     *     param1.set(field1);
     *     changeFieldNames.add($L);
     * }
     *
     */
    @Override
    public void createMethod(MethodSpec.Builder builder, MethodHolder methodHolder, ProcessorContext processorContext) {
        List<RootElementField> targetFieldList = new ArrayList<RootElementField>();
        for (RootElementField field : targetEntity.getFieldMap().values()) {
            if(BlankAide.isNotBlank(field.getSetMethod())) {
                targetFieldList.add(field);
            }
        }
        TypeName resultTypeName = ClassName.bestGuess(ElementAide.getSimpleTypeName(this.elementMethod.getResType().getTypeName()));
        List<String> errorFieldList = new ArrayList<>();
        List<String> warnFieldList = new ArrayList<>();
        builder.beginControlFlow("if($L == null)", SOURCE_PARAM_NAME);
        builder.addStatement("return $T.build($L, null)", resultTypeName, TARGET_PARAM_NAME);
        builder.endControlFlow();
        builder.addStatement("$T<$T> $L = new $T<>()", ClassName.get(Set.class), ClassName.get(String.class),
            CHANGE_FIELD_NAMES, ClassName.get(HashSet.class));
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
        builder.addStatement("return $T.build($L, changeFieldNames)", resultTypeName, TARGET_PARAM_NAME);
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
        boolean ignoreEmpty = (defaultValueMethodOpt.isPresent() && !ElementAide.isUnboxType(sourceField.getGetMethod().getResType().getTypeName()));
        // 判断是否有DefaultValueMethod, 如果有则肯定有赋值
        if(ignoreEmpty) {
            Optional<MethodGenerator> judgeEmptyMethodOpt = methodHolder.matchJudgeEmptyMethod(sourceField, sourceField.getGetMethod().getResType(),
                processorContext, ignoreGenericType
            );
            if(judgeEmptyMethodOpt.isPresent()) {
                builder.beginControlFlow("if($L($L))", judgeEmptyMethodOpt.get().getElementMethod().getName(),
                    spaceParamName);
            } else {
                builder.beginControlFlow("if($L == null)", spaceParamName);
            }
            builder.addStatement("$L = $L()", spaceParamName, defaultValueMethodOpt.get().getElementMethod().getName());
            builder.endControlFlow();
        }
        String targetSpaceParamName = "targetField" + fieldIndex;
        if(typeChangeMethod != null) {
            builder.addStatement("$T $L = $L($L)", typeChangeMethod.getElementMethod().getResType().getTypeName(),
                targetSpaceParamName, typeChangeMethod.getElementMethod().getName(), spaceParamName);
        } else {
            TypeName targetFieldTypeName = targetField.getType().getTypeName();
            // 判断是否有泛型，泛型策略
            if(this.ignoreGenericType
                && targetField.getType().isHasArgParam()) {
                targetFieldTypeName = ClassName.bestGuess(ElementAide.getSimpleTypeName(targetField.getType().getTypeName()));
            }
            builder.addStatement("$T $L = ($T)$L", targetFieldTypeName,
                targetSpaceParamName, targetFieldTypeName, spaceParamName);
        }
        Optional<MethodGenerator> judgeSameMethodGeneratorOpt = methodHolder.matchJudgeSameMethod(sourceField,
            targetField.getType(), targetField.getGetMethod().getResType(), processorContext, this.ignoreGenericType);
        if(judgeSameMethodGeneratorOpt.isPresent()) {
            builder.beginControlFlow("if(!$L($L,$L.$L()))", judgeSameMethodGeneratorOpt.get().getElementMethod().getName(),
                targetSpaceParamName, TARGET_PARAM_NAME, targetField.getGetMethod().getName());
        }
        builder.addStatement("$L.$L($L)", TARGET_PARAM_NAME, targetField.getSetMethod().getName(),targetSpaceParamName);
        builder.addStatement("$L.add(\"$L\")",CHANGE_FIELD_NAMES, targetField.getName());
        if(judgeSameMethodGeneratorOpt.isPresent()) {
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
