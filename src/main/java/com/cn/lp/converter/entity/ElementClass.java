package com.cn.lp.converter.entity;

import com.cn.lp.converter.processor.ProcessorContext;
import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类元素封装
 */
public class ElementClass {

    /** 泛型列表 */
    private Map<TypeVariable, TypeName> argEntityMap = new HashMap<>();
    /** 类名 */
    private TypeName typeName;
    /** 父类 */
    private ElementClass superClass;
    /** 接口列表 */
    private List<ElementClass> interfaceList = new ArrayList<>();

    /** 静态构建 */
    public static ElementClass create(ElementType elementType, ProcessorContext processorContext) {
        ElementClass elementClass = new ElementClass();
        elementClass.typeName = elementType.getTypeName();
        if(ElementAide.isUnboxType(elementType.getTypeName())) {
            return elementClass;
        }
        TypeElement typeElement = processorContext.getElementUtils().getTypeElement(ElementAide.getSimpleTypeName(elementType.getTypeName()));
        Map<TypeVariable, TypeName> argEntityMap = new HashMap<>();
        List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();
        for(int index = 0; index < typeParameters.size(); index ++) {
            TypeName typeName;
            if(index >= elementType.getArgTypeNames().length) {
                typeName = ClassName.get(Object.class);
            } else {
                typeName = elementType.getArgTypeNames()[index];
            }
            argEntityMap.put((TypeVariable) typeParameters.get(index).asType(), typeName);
        }
        elementClass.argEntityMap = argEntityMap;
        if(ElementAide.judgeHasSuperClass(typeElement)) {
            elementClass.superClass = ElementClass.create(ElementType.createByTypeName(typeElement.getSuperclass(), argEntityMap), processorContext);
        }
        for (TypeMirror typeInterface : typeElement.getInterfaces()) {
            elementClass.interfaceList.add(ElementClass.create(ElementType.createByTypeName(typeInterface, argEntityMap), processorContext));
        }
        return elementClass;
    }

    public Map<TypeVariable, TypeName> getArgEntityMap() {
        return argEntityMap;
    }

    public TypeName getTypeName() {
        return typeName;
    }

    public ElementClass getSuperClass() {
        return superClass;
    }

    public List<ElementClass> getInterfaceList() {
        return interfaceList;
    }

    public boolean isHasSuperClass() {
        return BlankAide.isNotBlank(this.superClass);
    }
}
