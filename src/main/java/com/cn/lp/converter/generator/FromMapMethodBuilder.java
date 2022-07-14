package com.cn.lp.converter.generator;

import com.cn.lp.converter.entity.*;
import com.cn.lp.converter.processor.ProcessorContext;
import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * FromMap方法生成器
 */
public class FromMapMethodBuilder implements GeneratorMethodBuilder {

    private ElementMethod elementMethod;

    private RootElementClass targetEntity;

    private boolean ignoreGenericType;

    private boolean ignoreEmpty;

    public static final String SOURCE_MAP_PARAM_NAME = "sourceMap";

    public void init(ConverterMethodGenerator methodGenerator, ElementInterface rootInterface) {
        this.elementMethod = methodGenerator.getElementMethod();
        this.targetEntity = methodGenerator.getConvertInterface().getEntityMap().get(elementMethod.getResType().getType());
        BeanConverterMapperAnnotation beanConverterMapperAnnotation = (BeanConverterMapperAnnotation)
            rootInterface.getAnnotation(BeanConverterMapperAnnotation.ANNOTATION_TYPE_NAME);
        this.ignoreGenericType = beanConverterMapperAnnotation.isIgnoreGenericType();
        BeanConverterMappingAnnotation beanConverterMappingAnnotation = (BeanConverterMappingAnnotation)
            methodGenerator.getAnnotation(BeanConverterMappingAnnotation.ANNOTATION_TYPE_NAME);
        if(beanConverterMappingAnnotation != null) {
            this.ignoreGenericType = beanConverterMappingAnnotation.isIgnoreGenericType();
        }
        this.ignoreEmpty = beanConverterMapperAnnotation.isIgnoreEmpty();
        if(beanConverterMappingAnnotation != null) {
            this.ignoreEmpty = beanConverterMappingAnnotation.isIgnoreEmpty();
        }
    }

    /**
     * // 如果存在DefaultValueMethod
     * if(param0 == null) {
     *    return null;
     * }
     * Map<String, Object> sourceMap = new HashMap<String, Object>(param0);
     * TestConvertMap.TextEntityA target = new TestConvertMap.TextEntityA();
     * // 如果存在TypeChangeMethod方法
     * $T field1 = TypeChangeMethod(sourceMap.get("$L"));
     * // 如果存在JudgeEmptyMethod
     * if(judgeEmpty(($T)field1) {
     *     field1 = DefaultValueMethod();
     * }
     * target.set(($T)field1);
     *
     * // 不存在DefaultValueMethod
     * if(param0 == null) {
     *    return null;
     * }
     * Map<String, Object> sourceMap = new HashMap<String, Object>(param0);
     * TestConvertMap.TextEntityA target = new TestConvertMap.TextEntityA();
     * // 如果存在TypeChangeMethod方法
     * $T field1 = TypeChangeMethod(sourceMap.get("$L"));
     * // 如果存在JudgeEmptyMethod
     * if(!judgeEmpty(($T)field1) {
     *     target.set(($T)field1);
     * }
     *
     */
    @Override
    public void createMethod(MethodSpec.Builder builder, MethodHolder methodHolder, ProcessorContext processorContext) {
        TypeName targetTypeName = TypeName.get(targetEntity.getType());
        builder.beginControlFlow("if($L == null)", SOURCE_PARAM_NAME);
        builder.addStatement("return null");
        builder.endControlFlow();
        builder.addStatement("$T<$T, $T> $L = new $T<$T, $T>($L)",ClassName.get(Map.class),ClassName.get(String.class),
            ClassName.get(Object.class), SOURCE_MAP_PARAM_NAME, ClassName.get(HashMap.class), ClassName.get(String.class),
            ClassName.get(Object.class),SOURCE_PARAM_NAME
        );
        builder.addStatement("$T target = new $T()", targetTypeName, targetTypeName);
        builder.addStatement("this.$L($L,target)", "pre" + StringUtils.capitalize(elementMethod.getName()), SOURCE_MAP_PARAM_NAME);
        int targetFieldIndex = 0;
        for (RootElementField field : targetEntity.getFieldMap().values()) {
            targetFieldIndex =  targetFieldIndex + 1;
            if(BlankAide.isNotBlank(field.getSetMethod())) {
                if(BlankAide.isNotBlank(field.getSetMethod())) {
                    String spaceParamName = "field" + targetFieldIndex;
                    Optional<MethodGenerator> matchTypeChangeMethodOpt = methodHolder.matchTypeChangeMethod(field,
                        ClassName.get(Object.class), field.getType().getTypeName(), ignoreGenericType);
                    if(matchTypeChangeMethodOpt.isPresent()) {
                        builder.addStatement(
                            "$T $L = $L($L.get(\"$L\"))",
                            matchTypeChangeMethodOpt.get().getElementMethod().getResType().getTypeName(),
                            spaceParamName,
                            matchTypeChangeMethodOpt.get().getElementMethod().getName(),
                            SOURCE_MAP_PARAM_NAME,
                            field.getName()
                        );
                    } else {
                        builder.addStatement(
                            "$T $L = $L.get(\"$L\")",
                            ClassName.get(Object.class),
                            spaceParamName,
                            SOURCE_MAP_PARAM_NAME,
                            field.getName()
                        );
                    }
                    boolean needEndControlFlow = false;
                    Optional<MethodGenerator> defaultValueMethodOpt = methodHolder.matchDefaultValueMethod(field,
                        field.getType(), ignoreGenericType);
                    Optional<MethodGenerator> matchJudgeEmptyMethodOpt = methodHolder.matchJudgeEmptyMethod(field,
                        field.getType(), processorContext, ignoreGenericType);
                    boolean ignoreEmpty = this.ignoreEmpty || defaultValueMethodOpt.isPresent()
                        || ElementAide.isUnboxType(field.getType().getTypeName()) ||
                        matchJudgeEmptyMethodOpt.isPresent();
                    // 判断是否有DefaultValueMethod，如果有则肯定有赋值
                    if(defaultValueMethodOpt.isPresent()) {
                        // 如果是基础类型，则没有JudgeEmptyMethod
                        if(ElementAide.isUnboxType(field.getType().getTypeName())) {
                            builder.beginControlFlow(
                                "if($L == null)",
                                spaceParamName
                            );
                        } else {
                            if(matchJudgeEmptyMethodOpt.isPresent()) {
                                builder.beginControlFlow(
                                    "if($L(($T)$L))",
                                    matchJudgeEmptyMethodOpt.get().getElementMethod().getName(),
                                    field.getType().getTypeName(),
                                    spaceParamName
                                );
                            } else {
                                builder.beginControlFlow(
                                    "if($L == null)",
                                    spaceParamName
                                );
                            }
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
                                "if(!$L(($T)$L))",
                                matchJudgeEmptyMethodOpt.get().getElementMethod().getName(),
                                field.getType().getTypeName(),
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
                    builder.addStatement(
                        "target.$L(($T)$L)",
                        field.getSetMethod().getName(),
                        field.getType().getTypeName(),
                        spaceParamName
                    );
                    if(needEndControlFlow) {
                        builder.endControlFlow();
                    }
                }
            }
        }
        builder.addStatement("return target");
    }

}
