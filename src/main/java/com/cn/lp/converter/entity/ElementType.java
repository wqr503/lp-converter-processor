package com.cn.lp.converter.entity;

import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.*;

import javax.lang.model.type.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 元素类型封装类
 */
public class ElementType {
    /** 原始type */
    private TypeMirror type;
    /** 类名 */
    private TypeName typeName;
    /** 类泛型 */
    private TypeName[] argTypeNames = new TypeName[0];

    /** 静态构造，子级构建父级(带有泛型) */
    public static ElementType create(TypeMirror type, Map<TypeVariable, ElementType> argMap) {
        Map<TypeVariable, TypeName> newArgMap = new HashMap<>();
        for (Map.Entry<TypeVariable, ElementType> entry : argMap.entrySet()) {
            newArgMap.put(entry.getKey(), entry.getValue().getTypeName());
        }
        return createByTypeName(type, newArgMap);
    }

    /** 静态构造，子级构建父级(带有泛型) */
    public static ElementType createByTypeName(TypeMirror type, Map<TypeVariable, TypeName> argMap) {
        ElementType elementType = new ElementType();
        elementType.type = type;
        if(type instanceof TypeVariable) {
            // 如果是泛型则从argMap找出子级中定义
            TypeName typeName = argMap.get(type);
            if(BlankAide.isNotBlank(typeName)) {
                elementType.typeName = typeName;
            } else {
                elementType.typeName = TypeName.get(type);
            }
        } else if(type instanceof ArrayType) {
            // 如果是数组
            ArrayType arrayType = (ArrayType) type;
            TypeMirror componentType = arrayType.getComponentType();
            if(componentType instanceof TypeVariable) {
                TypeName typeName = argMap.get(componentType);
                if(BlankAide.isNotBlank(typeName)) {
                    elementType.typeName = ArrayTypeName.of(typeName);
                } else {
                    elementType.typeName = ArrayTypeName.of(TypeName.get(componentType));
                }
            } else {
                elementType.typeName = TypeName.get(type);
                // 判断是否带泛型
                if(elementType.isHasArgParam()) {
                    // 泛型处理
                    convertTypeVariable(elementType, (DeclaredType) type, argMap);
                }
            }
        } else if(type instanceof WildcardType){
            // 判断是否 T extend Object 类型
            WildcardType wildcardType = (WildcardType) type;
            if(wildcardType.getExtendsBound() != null) {
                ElementType targetType = ElementType.createByTypeName(wildcardType.getExtendsBound(), argMap);
                elementType.typeName = WildcardTypeName.subtypeOf(targetType.getTypeName());
            } else if(wildcardType.getSuperBound() != null) {
                ElementType targetType = ElementType.createByTypeName(wildcardType.getSuperBound(), argMap);
                elementType.typeName = WildcardTypeName.supertypeOf(targetType.getTypeName());
            } else {
                elementType.typeName = TypeName.OBJECT;
            }
        }else {
            elementType.typeName = TypeName.get(type);
            if(elementType.isHasArgParam()) {
                convertTypeVariable(elementType, (DeclaredType) type, argMap);
            }
        }
        return elementType;
    }

    /** 判断是否含有泛型 */
    public boolean isHasArgParam() {
        if(type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;
            return BlankAide.isNotBlank(declaredType.getTypeArguments());
        }
        return false;
    }

    /** 泛型处理 */
    private static void convertTypeVariable(ElementType elementType, DeclaredType type, Map<TypeVariable, TypeName> argMap) {
        TypeName[] argTypes = new TypeName[type.getTypeArguments().size()];
        for(int index = 0; index < type.getTypeArguments().size(); index ++) {
            TypeMirror typeMirror = type.getTypeArguments().get(index);
            if(typeMirror instanceof TypeVariable) {
                TypeName typeName = argMap.get(typeMirror);
                if(BlankAide.isNotBlank(typeName)) {
                    argTypes[index] = typeName;
                } else {
                    argTypes[index] = TypeName.get(typeMirror);
                }
            } else {
                argTypes[index] = ElementType.createByTypeName(typeMirror, argMap).getTypeName();
            }
        }
        elementType.argTypeNames = argTypes;
        elementType.typeName = ParameterizedTypeName.get(ClassName.bestGuess(ElementAide.getSimpleTypeName(TypeName.get(type))), argTypes);
    }

    public TypeName getTypeName() {
        return typeName;
    }

    public TypeMirror getType() {
        return type;
    }

    public TypeName[] getArgTypeNames() {
        return argTypeNames;
    }

}
