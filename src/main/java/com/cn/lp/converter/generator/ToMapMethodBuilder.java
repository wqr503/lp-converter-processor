package com.cn.lp.converter.generator;

import com.cn.lp.converter.entity.*;
import com.cn.lp.converter.processor.ProcessorContext;
import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.type.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ToMap方法构建器
 */
public class ToMapMethodBuilder implements GeneratorMethodBuilder {

    private ElementMethod elementMethod;

    private RootElementClass sourceEntity;

    private boolean ignoreEmpty;

    private boolean ignoreGenericType;

    public void init(ConverterMethodGenerator methodGenerator, ElementInterface rootInterface) {
        this.elementMethod = methodGenerator.getElementMethod();
        Map<TypeVariable, RootElementClass> entityMap = methodGenerator.getConvertInterface().getEntityMap();
        this.sourceEntity = entityMap.get(elementMethod.getParamList().get(0).getType().getType());
        BeanConverterMapperAnnotation beanConverterMapperAnnotation = (BeanConverterMapperAnnotation)
            rootInterface.getAnnotation(BeanConverterMapperAnnotation.ANNOTATION_TYPE_NAME);
        this.ignoreEmpty = beanConverterMapperAnnotation.isIgnoreEmpty();
        BeanConverterMappingAnnotation beanConverterMappingAnnotation = (BeanConverterMappingAnnotation)
            methodGenerator.getAnnotation(BeanConverterMappingAnnotation.ANNOTATION_TYPE_NAME);
        if(beanConverterMappingAnnotation != null) {
            this.ignoreEmpty = beanConverterMappingAnnotation.isIgnoreEmpty();
        }
        this.ignoreGenericType = beanConverterMapperAnnotation.isIgnoreGenericType();
        if(beanConverterMappingAnnotation != null) {
            this.ignoreGenericType = beanConverterMappingAnnotation.isIgnoreGenericType();
        }
    }

    /**
     * // 如果存在DefaultValueMethod
     * if(param0 == null) {
     *     return new HashMap();
     * }
     * Map<String,Object> target = new HashMap();
     * $T field1 = param0.get();
     * // 如果存在TypeChangeMethod方法
     * if(JudgeEmptyMethod(field1)) {
     *      field1 = DefaultValueMethod();
     * }
     * // 如果存在TypeChangeMethod
     * Object targetField1 = TypeChangeMethod(field1)
     * target.put($L, targetField1);
     *
     *
     * // 不存在DefaultValueMethod
     * if(param0 == null) {
     *     return new HashMap();
     * }
     * Map<String,Object> target = new HashMap();
     * $T field1 = param0.get();
     * // 如果存在TypeChangeMethod方法
     * if(!JudgeEmptyMethod(field1)) {
     *      // 如果存在TypeChangeMethod
     *      target.put($L, TypeChangeMethod(field1));
     * }
     */
    @Override
    public void createMethod(MethodSpec.Builder builder, MethodHolder methodHolder, ProcessorContext processorContext) {
        TypeName targetTypeName = this.elementMethod.getResType().getTypeName();
        builder.beginControlFlow("if($L == null)", SOURCE_PARAM_NAME);
        builder.addStatement("return new $T()", ParameterizedTypeName.get(ClassName.get(HashMap.class), this.elementMethod.getResType().getArgTypeNames()));
        builder.endControlFlow();
        builder.addStatement("$T target = new $T()", targetTypeName,
            ParameterizedTypeName.get(ClassName.get(HashMap.class), this.elementMethod.getResType().getArgTypeNames()));
        int targetFieldIndex = 0;
        for (RootElementField field : sourceEntity.getFieldMap().values()) {
            targetFieldIndex =  targetFieldIndex + 1;
            if(BlankAide.isNotBlank(field.getGetMethod())) {
                String spaceParamName = "field" + targetFieldIndex;
                builder.addStatement(
                    "$T $L = $L.$L()",
                    field.getGetMethod().getResType().getTypeName(),
                    spaceParamName,
                    SOURCE_PARAM_NAME,
                    field.getGetMethod().getName()
                );
                boolean needEndControlFlow = false;
                Optional<MethodGenerator> defaultValueMethodOpt = methodHolder.matchDefaultValueMethod(field,
                    field.getGetMethod().getResType(), ignoreGenericType);
                Optional<MethodGenerator> matchJudgeEmptyMethodOpt = methodHolder.matchJudgeEmptyMethod(field,
                    field.getGetMethod().getResType(), processorContext, ignoreGenericType);
                boolean ignoreEmpty = (this.ignoreEmpty || defaultValueMethodOpt.isPresent() ||
                    matchJudgeEmptyMethodOpt.isPresent())
                    && !ElementAide.isUnboxType(field.getGetMethod().getResType().getTypeName());
                // 判断是否有DefaultValueMethod, 如果有则肯定有赋值
                if(defaultValueMethodOpt.isPresent() && !ElementAide.isUnboxType(field.getGetMethod().getResType().getTypeName())) {
                    if(matchJudgeEmptyMethodOpt.isPresent()) {
                        builder.beginControlFlow(
                            "if($L($L))",
                            matchJudgeEmptyMethodOpt.get().getElementMethod().getName(),
                            spaceParamName
                        );
                    } else {
                        builder.beginControlFlow(
                            "if($L == null)",
                            spaceParamName
                        );
                    }
                    builder.addStatement(
                        "$L = $L()",
                        spaceParamName,
                        defaultValueMethodOpt.get().getElementMethod().getName()
                    );
                    builder.endControlFlow();
                } else if(ignoreEmpty) {
                    // 判断是由有ignoreEmpty判定，非空才有赋值
                    if(matchJudgeEmptyMethodOpt.isPresent()) {
                        builder.beginControlFlow(
                            "if(!$L($L))",
                            matchJudgeEmptyMethodOpt.get().getElementMethod().getName(),
                            spaceParamName
                        );
                    } else {
                        builder.beginControlFlow(
                            "if($L != null)",
                            spaceParamName
                        );
                    }
                    needEndControlFlow = true;
                }
                Optional<MethodGenerator> matchTypeChangeMethodOpt = methodHolder.matchTypeChangeMethod(field,
                    field.getGetMethod().getResType(), ClassName.get(Object.class), processorContext, ignoreGenericType);
                if(matchTypeChangeMethodOpt.isPresent()) {
                    builder.addStatement(
                        "target.put(\"$L\",$L($L))",
                        field.getName(),
                        matchTypeChangeMethodOpt.get().getElementMethod().getName(),
                        spaceParamName
                    );
                } else {
                    builder.addStatement(
                        "target.put(\"$L\",$L)",
                        field.getName(),
                        spaceParamName
                    );
                }
                if(needEndControlFlow) {
                    builder.endControlFlow();
                }
            }
        }
        builder.addStatement("this.$L($L,target)", "post" + StringUtils.capitalize(elementMethod.getName()), SOURCE_PARAM_NAME);
        builder.addStatement("return target");
    }

}
