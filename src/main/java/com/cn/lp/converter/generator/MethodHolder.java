package com.cn.lp.converter.generator;

import com.cn.lp.converter.entity.*;
import com.cn.lp.converter.processor.ProcessorContext;
import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationValue;
import java.util.*;

/**
 * 方法持有者
 */
public class MethodHolder {

    private Map<String, MethodGenerator> typeChangeMethodFieldNameMap = new HashMap<>();

    private Map<String, MethodGenerator> judgeEmptyMethodFieldNameMap = new HashMap<>();

    private Map<String, MethodGenerator> defaultValueMethodFieldNameMap = new HashMap<>();

    private Map<String, MethodGenerator> judgeSameMethodFieldNameMap = new HashMap<>();

    private Map<String, JudgeSameMethodHolder> judgeSameMethodSignMap = new HashMap<>();

    private Map<String, DefaultValueMethodHolder> defaultValueMethodSignMap = new HashMap<>();

    private Map<String, TypeChangeMethodHolder> typeChangeMethodSignMap = new HashMap<>();

    private Map<String, JudgeEmptyMethodHolder> judgeEmptyMethodSignMap = new HashMap<>();

    private static void buildTypeChangeMethod(MethodHolder methodHolder, Collection<MethodGenerator> methodGeneratorList) {
        Map<String, MethodGenerator> fieldNameMap = new HashMap<>();
        Map<String, TypeChangeMethodHolder> signMap = new HashMap<>();
        for (MethodGenerator methodGenerator : methodGeneratorList) {
            ElementMethod elementMethod = methodGenerator.getElementMethod();
            if(methodGenerator.isTypeChangeMethod()) {
                TypeChangeMethodAnnotation typeChangeMethodAnnotation = (TypeChangeMethodAnnotation)
                    methodGenerator.getAnnotation(TypeChangeMethodAnnotation.ANNOTATION_TYPE_NAME);
                if(BlankAide.isNotBlank(typeChangeMethodAnnotation.getAssignFieldName())) {
                    for (AnnotationValue annotationValue : typeChangeMethodAnnotation.getAssignFieldName()) {
                        String fieldName = (String) annotationValue.getValue();
                        MethodGenerator oldMethodGenerator = fieldNameMap.get(fieldName);
                        if(oldMethodGenerator != null) {
                            if(typeChangeMethodAnnotation.isPrimary()) {
                                fieldNameMap.put(fieldName, methodGenerator);
                            }
                        } else {
                            fieldNameMap.put(fieldName, methodGenerator);
                        }
                    }
                } else {
                    String sign = typeChangeMethodAnnotation.createMethodSign(elementMethod);
                    TypeChangeMethodHolder oldMethodGenerator = signMap.get(sign);
                    if(oldMethodGenerator != null) {
                        if(typeChangeMethodAnnotation.isHasAssign()) {
                            oldMethodGenerator.addAssignMethod(methodGenerator);
                        } else  {
                            oldMethodGenerator.updateMethod(methodGenerator);
                        }
                    } else {
                        signMap.put(sign,TypeChangeMethodHolder.create(methodGenerator));
                    }
                }
            }
        }
        methodHolder.typeChangeMethodSignMap = signMap;
        methodHolder.typeChangeMethodFieldNameMap = fieldNameMap;
    }

    private static void buildJudgeEmptyMethod(MethodHolder methodHolder, Collection<MethodGenerator> methodGeneratorList) {
        Map<String, MethodGenerator> fieldNameMap = new HashMap<>();
        Map<String, JudgeEmptyMethodHolder> signMap = new HashMap<>();
        for (MethodGenerator methodGenerator : methodGeneratorList) {
            ElementMethod elementMethod = methodGenerator.getElementMethod();
            if(methodGenerator.isJudgeEmptyMethod()) {
                JudgeEmptyMethodAnnotation judgeEmptyMethodAnnotation = (JudgeEmptyMethodAnnotation)
                    methodGenerator.getAnnotation(JudgeEmptyMethodAnnotation.ANNOTATION_TYPE_NAME);
                if(BlankAide.isNotBlank(judgeEmptyMethodAnnotation.getAssignFieldName())) {
                    for (AnnotationValue annotationValue : judgeEmptyMethodAnnotation.getAssignFieldName()) {
                        String fieldName = (String) annotationValue.getValue();
                        MethodGenerator oldMethodGenerator = fieldNameMap.get(fieldName);
                        if(oldMethodGenerator != null) {
                            if(judgeEmptyMethodAnnotation.isPrimary()) {
                                fieldNameMap.put(fieldName, methodGenerator);
                            }
                        } else {
                            fieldNameMap.put(fieldName, methodGenerator);
                        }
                    }
                } else {
                    String sign = judgeEmptyMethodAnnotation.createMethodSign(elementMethod);
                    JudgeEmptyMethodHolder oldMethodGenerator = signMap.get(sign);
                    if(oldMethodGenerator != null) {
                        if(judgeEmptyMethodAnnotation.isHasAssign()) {
                            oldMethodGenerator.addAssignMethod(methodGenerator);
                        } else  {
                            oldMethodGenerator.updateMethod(methodGenerator);
                        }
                    } else {
                        signMap.put(sign,JudgeEmptyMethodHolder.create(methodGenerator));
                    }
                }
            }
        }
        methodHolder.judgeEmptyMethodSignMap = signMap;
        methodHolder.judgeEmptyMethodFieldNameMap = fieldNameMap;
    }

    private static void buildJudgeSameMethod(MethodHolder methodHolder, Collection<MethodGenerator> methodGeneratorList) {
        Map<String, MethodGenerator> fieldNameMap = new HashMap<>();
        Map<String, JudgeSameMethodHolder> signMap = new HashMap<>();
        for (MethodGenerator methodGenerator : methodGeneratorList) {
            ElementMethod elementMethod = methodGenerator.getElementMethod();
            if(methodGenerator.isJudgeSameMethod()) {
                JudgeSameMethodAnnotation methodAnnotation = (JudgeSameMethodAnnotation)
                    methodGenerator.getAnnotation(JudgeSameMethodAnnotation.ANNOTATION_TYPE_NAME);
                if(BlankAide.isNotBlank(methodAnnotation.getAssignFieldName())) {
                    for (AnnotationValue annotationValue : methodAnnotation.getAssignFieldName()) {
                        String fieldName = (String) annotationValue.getValue();
                        MethodGenerator oldMethodGenerator = fieldNameMap.get(fieldName);
                        if(oldMethodGenerator != null) {
                            if(methodAnnotation.isPrimary()) {
                                fieldNameMap.put(fieldName, methodGenerator);
                            }
                        } else {
                            fieldNameMap.put(fieldName, methodGenerator);
                        }
                    }
                } else {
                    String sign = methodAnnotation.createMethodSign(elementMethod);
                    JudgeSameMethodHolder oldMethodGenerator = signMap.get(sign);
                    if(oldMethodGenerator != null) {
                        if(methodAnnotation.isHasAssign()) {
                            oldMethodGenerator.addAssignMethod(methodGenerator);
                        } else  {
                            oldMethodGenerator.updateMethod(methodGenerator);
                        }
                    } else {
                        signMap.put(sign,JudgeSameMethodHolder.create(methodGenerator));
                    }
                }
            }
        }
        methodHolder.judgeSameMethodSignMap = signMap;
        methodHolder.judgeSameMethodFieldNameMap = fieldNameMap;
    }

    private static void buildDefaultValueMethod(MethodHolder methodHolder, Collection<MethodGenerator> methodGeneratorList) {
        Map<String, MethodGenerator> fieldNameMap = new HashMap<>();
        Map<String, DefaultValueMethodHolder> signMap = new HashMap<>();
        for (MethodGenerator methodGenerator : methodGeneratorList) {
            ElementMethod elementMethod = methodGenerator.getElementMethod();
            if(methodGenerator.isDefaultValueMethod()) {
                DefaultValueMethodAnnotation annotation = (DefaultValueMethodAnnotation)
                    methodGenerator.getAnnotation(DefaultValueMethodAnnotation.ANNOTATION_TYPE_NAME);
                if(BlankAide.isNotBlank(annotation.getAssignFieldName())) {
                    for (AnnotationValue annotationValue : annotation.getAssignFieldName()) {
                        String fieldName = (String) annotationValue.getValue();
                        MethodGenerator oldMethodGenerator = fieldNameMap.get(fieldName);
                        if(oldMethodGenerator != null) {
                            if(annotation.isPrimary()) {
                                fieldNameMap.put(fieldName, methodGenerator);
                            }
                        } else {
                            fieldNameMap.put(fieldName, methodGenerator);
                        }
                    }
                } else {
                    String sign = annotation.createMethodSign(elementMethod);
                    DefaultValueMethodHolder oldMethodGenerator = signMap.get(sign);
                    if(oldMethodGenerator != null) {
                        if(annotation.isHasAssign()) {
                            oldMethodGenerator.addAssignMethod(methodGenerator);
                        } else  {
                            oldMethodGenerator.updateMethod(methodGenerator);
                        }
                    } else {
                        signMap.put(sign,DefaultValueMethodHolder.create(methodGenerator));
                    }
                }
            }
        }
        methodHolder.defaultValueMethodSignMap = signMap;
        methodHolder.defaultValueMethodFieldNameMap = fieldNameMap;
    }

    public static MethodHolder build(Collection<MethodGenerator> methodGeneratorList) {
        MethodHolder methodHolder = new MethodHolder();
        // 找出TypeChangeMethod方法
        buildJudgeEmptyMethod(methodHolder, methodGeneratorList);
        buildTypeChangeMethod(methodHolder, methodGeneratorList);
        buildDefaultValueMethod(methodHolder, methodGeneratorList);
        buildJudgeSameMethod(methodHolder, methodGeneratorList);
        return methodHolder;
    }

    /**
     * 根据入参匹配JudgeSameMethod
     * ignoreGenericType - 忽略泛型
     */
    public Optional<MethodGenerator> matchJudgeSameMethod(RootElementField sourceField, ElementType sourceType,
        ElementType targetType, ProcessorContext processorContext, boolean ignoreGenericType) {
        MethodGenerator methodGenerator = judgeSameMethodFieldNameMap.get(sourceField.getName());
        if(BlankAide.isNotBlank(methodGenerator)) {
            return Optional.of(methodGenerator);
        }
        Set<String> sourceSuperTypeNameList = ElementAide.getSuperTypeNameList(sourceType, false,
            true, processorContext);
        Set<String> targetSuperTypeNameList = ElementAide.getSuperTypeNameList(targetType, false,
            true, processorContext);
        for (String sourceSuperType : sourceSuperTypeNameList) {
            for (String targetSuperType : targetSuperTypeNameList) {
                String sign = sourceSuperType + "-"
                    + targetSuperType + "-" + TypeName.BOOLEAN.toString();
                JudgeSameMethodHolder methodHolder = judgeSameMethodSignMap.get(sign);
                if(BlankAide.isNotBlank(methodHolder)) {
                    return methodHolder.getMatchFieldMethod(sourceField);
                }
                if(ignoreGenericType) {
                    sign = ElementAide.getSimpleTypeName(sourceSuperType) + "-"
                        + targetSuperType + "-" + TypeName.BOOLEAN.toString();
                    methodHolder = judgeSameMethodSignMap.get(sign);
                    if(BlankAide.isNotBlank(methodHolder)) {
                        return methodHolder.getMatchFieldMethod(sourceField);
                    }
                    sign = sourceSuperType + "-"
                        + ElementAide.getSimpleTypeName(targetSuperType) + "-" + TypeName.BOOLEAN.toString();
                    methodHolder = judgeSameMethodSignMap.get(sign);
                    if(BlankAide.isNotBlank(methodHolder)) {
                        return methodHolder.getMatchFieldMethod(sourceField);
                    }
                    sign = ElementAide.getSimpleTypeName(sourceSuperType) + "-"
                        + ElementAide.getSimpleTypeName(targetSuperType) + "-" + TypeName.BOOLEAN.toString();
                    methodHolder = judgeSameMethodSignMap.get(sign);
                    if(BlankAide.isNotBlank(methodHolder)) {
                        return methodHolder.getMatchFieldMethod(sourceField);
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 根据入参匹配JudgeEmptyMethod
     */
    public Optional<MethodGenerator> matchJudgeEmptyMethod(RootElementField sourceField, ElementType sourceType,
        ProcessorContext processorContext, boolean ignoreGenericType) {
        MethodGenerator methodGenerator = judgeEmptyMethodFieldNameMap.get(sourceField.getName());
        if(BlankAide.isNotBlank(methodGenerator)) {
            return Optional.of(methodGenerator);
        }
        Set<String> sourceSuperTypeNameList = ElementAide.getSuperTypeNameList(sourceType,
            false, true, processorContext);
        for (String sourceSuperType : sourceSuperTypeNameList) {
            String sign = sourceSuperType + "-" + TypeName.BOOLEAN.toString();
            JudgeEmptyMethodHolder methodHolder = judgeEmptyMethodSignMap.get(sign);
            if(BlankAide.isNotBlank(methodHolder)) {
                return methodHolder.getMatchFieldMethod(sourceField);
            }
            if(ignoreGenericType) {
                sign = ElementAide.getSimpleTypeName(sourceSuperType) + "-" + TypeName.BOOLEAN.toString();
                methodHolder = judgeEmptyMethodSignMap.get(sign);
                if(BlankAide.isNotBlank(methodHolder)) {
                    return methodHolder.getMatchFieldMethod(sourceField);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<MethodGenerator> matchTypeChangeMethod(RootElementField sourceField,
        TypeName sourceType, TypeName targetType, boolean ignoreGenericType) {
        MethodGenerator methodGenerator = typeChangeMethodFieldNameMap.get(sourceField.getName());
        if(BlankAide.isNotBlank(methodGenerator)) {
            return Optional.of(methodGenerator);
        }
        String sign = sourceType + "-" + targetType;
        TypeChangeMethodHolder methodHolder = typeChangeMethodSignMap.get(sign);
        if(BlankAide.isNotBlank(methodHolder)) {
            return methodHolder.getMatchFieldMethod(sourceField);
        }
        if(ignoreGenericType) {
            sign = ElementAide.getSimpleTypeName(sourceType.toString()) + "-" + targetType;
            methodHolder = typeChangeMethodSignMap.get(sign);
            if(BlankAide.isNotBlank(methodHolder)) {
                return methodHolder.getMatchFieldMethod(sourceField);
            }
            sign = sourceType.toString() + "-" + ElementAide.getSimpleTypeName(targetType.toString());
            methodHolder = typeChangeMethodSignMap.get(sign);
            if(BlankAide.isNotBlank(methodHolder)) {
                return methodHolder.getMatchFieldMethod(sourceField);
            }
            sign = ElementAide.getSimpleTypeName(sourceType.toString()) + "-" + ElementAide.getSimpleTypeName(targetType.toString());
            methodHolder = typeChangeMethodSignMap.get(sign);
            if(BlankAide.isNotBlank(methodHolder)) {
                return methodHolder.getMatchFieldMethod(sourceField);
            }
        }
        return Optional.empty();
    }

    public Optional<MethodGenerator> matchTypeChangeMethod(RootElementField sourceField,
        ElementType sourceType, TypeName targetType, ProcessorContext processorContext, boolean ignoreGenericType) {
        MethodGenerator methodGenerator = typeChangeMethodFieldNameMap.get(sourceField.getName());
        if(BlankAide.isNotBlank(methodGenerator)) {
            return Optional.of(methodGenerator);
        }
        Set<String> sourceSuperTypeNameList = ElementAide.getSuperTypeNameList(sourceType,
            false, true, processorContext);
        for (String sourceSuperType : sourceSuperTypeNameList) {
            String sign = sourceSuperType + "-" + targetType;
            TypeChangeMethodHolder methodHolder = typeChangeMethodSignMap.get(sign);
            if(BlankAide.isNotBlank(methodHolder)) {
                return methodHolder.getMatchFieldMethod(sourceField);
            }
            if(ignoreGenericType) {
                sign = ElementAide.getSimpleTypeName(sourceSuperType) + "-" + targetType;
                methodHolder = typeChangeMethodSignMap.get(sign);
                if(BlankAide.isNotBlank(methodHolder)) {
                    return methodHolder.getMatchFieldMethod(sourceField);
                }
                sign = ElementAide.getSimpleTypeName(sourceSuperType) + "-" + ElementAide.getSimpleTypeName(targetType.toString());
                methodHolder = typeChangeMethodSignMap.get(sign);
                if(BlankAide.isNotBlank(methodHolder)) {
                    return methodHolder.getMatchFieldMethod(sourceField);
                }
                sign = sourceSuperType + "-" + ElementAide.getSimpleTypeName(targetType.toString());
                methodHolder = typeChangeMethodSignMap.get(sign);
                if(BlankAide.isNotBlank(methodHolder)) {
                    return methodHolder.getMatchFieldMethod(sourceField);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<MethodGenerator> matchDefaultValueMethod(RootElementField field, ElementType fieldType,
        boolean ignoreGenericType) {
        MethodGenerator methodGenerator = defaultValueMethodFieldNameMap.get(field.getName());
        if(BlankAide.isNotBlank(methodGenerator)) {
            return Optional.of(methodGenerator);
        }
        String sign = "" + fieldType.getTypeName().toString();
        DefaultValueMethodHolder methodHolder = defaultValueMethodSignMap.get(sign);
        if(BlankAide.isNotBlank(methodHolder)) {
            return methodHolder.getMatchFieldMethod(field);
        }
        if(ignoreGenericType) {
            sign = "" + ElementAide.getSimpleTypeName(fieldType.getTypeName().toString());
            methodHolder = defaultValueMethodSignMap.get(sign);
            if(BlankAide.isNotBlank(methodHolder)) {
                return methodHolder.getMatchFieldMethod(field);
            }
        }
        return Optional.empty();
    }

}
