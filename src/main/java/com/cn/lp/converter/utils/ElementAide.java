package com.cn.lp.converter.utils;

import com.cn.lp.converter.entity.ElementClass;
import com.cn.lp.converter.entity.ElementType;
import com.cn.lp.converter.entity.RootElementField;
import com.cn.lp.converter.processor.ProcessorContext;
import com.google.common.collect.Lists;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import java.util.*;

/**
 * 元素帮助类
 */
public class ElementAide {

    public static final ClassName BOXED_VOID = ClassName.get("java.lang", "Void", new String[0]);
    public static final ClassName BOXED_BOOLEAN = ClassName.get("java.lang", "Boolean", new String[0]);
    public static final ClassName BOXED_BYTE = ClassName.get("java.lang", "Byte", new String[0]);
    public static final ClassName BOXED_SHORT = ClassName.get("java.lang", "Short", new String[0]);
    public static final ClassName BOXED_INT = ClassName.get("java.lang", "Integer", new String[0]);
    public static final ClassName BOXED_LONG = ClassName.get("java.lang", "Long", new String[0]);
    public static final ClassName BOXED_CHAR = ClassName.get("java.lang", "Character", new String[0]);
    public static final ClassName BOXED_FLOAT = ClassName.get("java.lang", "Float", new String[0]);
    public static final ClassName BOXED_DOUBLE = ClassName.get("java.lang", "Double", new String[0]);

    public static final List<TypeName> UNBOX_TYPE_LIST = Lists.newArrayList(
        TypeName.BYTE,
        TypeName.SHORT,
        TypeName.INT,
        TypeName.LONG,
        TypeName.FLOAT,
        TypeName.DOUBLE,
        TypeName.VOID,
        TypeName.BOOLEAN,
        TypeName.CHAR
    );

    /** 判断是否含有父类 */
    public static boolean judgeHasSuperClass(TypeElement typeElement) {
        TypeMirror superclass = typeElement.getSuperclass();
        if((BlankAide.isBlank(superclass) || TypeName.OBJECT.toString().equals(superclass.toString()) || "none".equals(superclass.toString()))) {
            return false;
        }
        return true;
    }

    /** 判断是否基础类型 */
    public static boolean isUnboxType(TypeName typeName) {
        return UNBOX_TYPE_LIST.contains(typeName);
    }

    /** 构建Set方法名　*/
    public static String buildSetMethodName(TypeMirror fieldType, String fieldName) {
        StringBuilder setMethodSB = new StringBuilder();
        setMethodSB.append("set").append(StringUtils.capitalize(fieldName)).append("-");
        if(fieldType instanceof TypeVariable || fieldType instanceof WildcardType) {
            setMethodSB.append("TypeVariable").append("-");
        } else if(fieldType instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) fieldType;
            TypeMirror componentType = arrayType.getComponentType();
            if(componentType instanceof TypeVariable) {
                setMethodSB.append("TypeVariable[]").append("-");
            } else {
                setMethodSB.append(ElementAide.getSimpleTypeName(TypeName.get(fieldType))).append("-");
            }
        } else {
            setMethodSB.append(ElementAide.getSimpleTypeName(TypeName.get(fieldType))).append("-");
        }
        return setMethodSB.toString();
    }

    /** 获取忽略泛型的类型 */
    public static String getSimpleTypeName(TypeName typeName) {
        return getSimpleTypeName(typeName.toString());
    }

    /** 获取忽略泛型的类型 */
    public static String getSimpleTypeName(String typeName) {
        String name = typeName.intern();
        String resName = name;
        int beginIndex = name.indexOf("<");
        if(beginIndex > 0 ) {
            resName = name.substring(0, beginIndex);
            int endIndex = name.lastIndexOf(">");
            if(endIndex > 0 && endIndex < name.length() - 1) {
                resName = resName + name.substring(endIndex + 1);
            }
        }
        return resName;
    }

    /** 获取注解值 */
    public static <T> T getAnnotationValue(AnnotationMirror annotationMirror, String valueName, T defaultValue) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            String name = entry.getKey().getSimpleName().toString();
            if(name.equals(valueName)) {
                return (T) entry.getValue().getValue();
            }
        }
        Element element = annotationMirror.getAnnotationType().asElement();
        for (Element enclosedElement : element.getEnclosedElements()) {
            if(ElementKind.METHOD.equals(enclosedElement.getKind())) {
                ExecutableElement methodElement = (ExecutableElement)enclosedElement;
                String name = enclosedElement.getSimpleName().toString();
                if(name.equals(valueName)) {
                    return (T) methodElement.getDefaultValue().getValue();
                }
            }
        }
        return defaultValue;
    }

    /** 获取父类列表 */
    public static Set<String> getSuperTypeNameList(ElementType field, boolean includeIgnoreGenericType,
        boolean includeSelf, ProcessorContext processorContext) {
        LinkedHashSet<String> superTypeNameList = new LinkedHashSet<>();
        if(!ElementAide.isUnboxType(field.getTypeName())) {
            ElementClass elementClass = ElementClass.create(field, processorContext);
            LinkedList<ElementClass> classList = new LinkedList<>();
            classList.add(elementClass);
            ElementClass nextClass = classList.pollFirst();
            while(nextClass != null) {
                if(nextClass != elementClass || includeSelf) {
                    superTypeNameList.add(nextClass.getTypeName().toString());
                }
                if(includeIgnoreGenericType) {
                    superTypeNameList.add(ElementAide.getSimpleTypeName(nextClass.getTypeName().toString()));
                }
                if(nextClass.isHasSuperClass()) {
                    classList.add(nextClass.getSuperClass());
                }
                classList.addAll(nextClass.getInterfaceList());
                nextClass = classList.pollFirst();
            }
        }
        return superTypeNameList;
    }

    public static boolean judgeType(RootElementField sourceField, RootElementField targetField, boolean ignoreGenericType,
        ProcessorContext processorContext) {
        ElementType sourceType = sourceField.getGetMethod().getResType();
        ElementType targetType = targetField.getType();
        // 判断Object
        if(TypeName.OBJECT.equals(targetType.getTypeName()) || TypeName.OBJECT.equals(sourceType.getTypeName())) {
            return true;
        }
        // 判断基础类型转Number
        if(ClassName.get(Number.class).equals(targetType.getTypeName())){
            if(TypeName.INT.equals(sourceType.getTypeName()) ||
                TypeName.SHORT.equals(sourceType.getTypeName()) ||
                TypeName.LONG.equals(sourceType.getTypeName()) ||
                TypeName.DOUBLE.equals(sourceType.getTypeName()) ||
                TypeName.BYTE.equals(sourceType.getTypeName()) ||
                TypeName.FLOAT.equals(sourceType.getTypeName()) ||
                ElementAide.BOXED_INT.equals(sourceType.getTypeName()) ||
                ElementAide.BOXED_BYTE.equals(sourceType.getTypeName()) ||
                ElementAide.BOXED_LONG.equals(sourceType.getTypeName()) ||
                ElementAide.BOXED_SHORT.equals(sourceType.getTypeName()) ||
                ElementAide.BOXED_DOUBLE.equals(sourceType.getTypeName()) ||
                ElementAide.BOXED_FLOAT.equals(sourceType.getTypeName())) {
                return true;
            }
        }
        // 判断封装类
        if(TypeName.INT.equals(targetType.getTypeName()) || ElementAide.BOXED_INT.equals(targetType.getTypeName())) {
            if(TypeName.INT.equals(sourceType.getTypeName()) || ElementAide.BOXED_INT.equals(sourceType.getTypeName())) {
                return true;
            }
        }
        if(TypeName.BOOLEAN.equals(targetType.getTypeName()) || ElementAide.BOXED_BOOLEAN.equals(targetType.getTypeName())) {
            if(TypeName.BOOLEAN.equals(sourceType.getTypeName()) || ElementAide.BOXED_BOOLEAN.equals(sourceType.getTypeName())) {
                return true;
            }
        }
        if(TypeName.BYTE.equals(targetType.getTypeName()) || ElementAide.BOXED_BYTE.equals(targetType.getTypeName())) {
            if( TypeName.BYTE.equals(sourceType.getTypeName()) || ElementAide.BOXED_BYTE.equals(sourceType.getTypeName())) {
                return true;
            }
        }
        if(TypeName.CHAR.equals(targetType.getTypeName()) || ElementAide.BOXED_CHAR.equals(targetType.getTypeName())) {
            if( TypeName.CHAR.equals(sourceType.getTypeName()) || ElementAide.BOXED_CHAR.equals(sourceType.getTypeName())) {
                return true;
            }
        }
        if(TypeName.DOUBLE.equals(targetType.getTypeName()) || ElementAide.BOXED_DOUBLE.equals(targetType.getTypeName())) {
            if( TypeName.DOUBLE.equals(sourceType.getTypeName()) || ElementAide.BOXED_DOUBLE.equals(sourceType.getTypeName())) {
                return true;
            }
        }
        if(TypeName.LONG.equals(targetType.getTypeName()) || ElementAide.BOXED_LONG.equals(targetType.getTypeName())) {
            if( TypeName.LONG.equals(sourceType.getTypeName()) || ElementAide.BOXED_LONG.equals(sourceType.getTypeName())) {
                return true;
            }
        }
        if(TypeName.FLOAT.equals(targetType.getTypeName()) || ElementAide.BOXED_FLOAT.equals(targetType.getTypeName())) {
            if( TypeName.FLOAT.equals(sourceType.getTypeName()) || ElementAide.BOXED_FLOAT.equals(sourceType.getTypeName())) {
                return true;
            }
        }
        if(TypeName.SHORT.equals(targetType.getTypeName()) || ElementAide.BOXED_SHORT.equals(targetType.getTypeName())) {
            if( TypeName.SHORT.equals(sourceType.getTypeName()) || ElementAide.BOXED_SHORT.equals(sourceType.getTypeName())) {
                return true;
            }
        }
        // 判断是否父类
        return isAssignable(sourceType, targetType, ignoreGenericType, processorContext);
    }

    /**
     * target 是否是source 的父类
     */
    public static boolean isAssignable(ElementType sourceField, ElementType targetField, boolean ignoreGenericType,
        ProcessorContext processorContext) {
        if(ElementAide.isUnboxType(sourceField.getTypeName()) || ElementAide.isUnboxType(targetField.getTypeName())) {
            return false;
        }
        Set<String> sourceSuperTypeNameList = ElementAide.getSuperTypeNameList(sourceField, ignoreGenericType, true, processorContext);
        TypeName targetTypeName = ignoreGenericType ? ClassName.bestGuess(ElementAide.getSimpleTypeName(targetField.getTypeName())) : targetField.getTypeName();
        return sourceSuperTypeNameList.contains(targetTypeName.toString());
    }
}
