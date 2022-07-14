package com.cn.lp.converter.entity;

import com.cn.lp.converter.processor.ProcessorContext;
import com.cn.lp.converter.utils.BlankAide;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.*;

/**
 * BeanConvert interface的封装类
 */
public class RootElementInterface {
    /** 类名  eg: ElementInterface*/
    protected String name;
    /** 包名 eg: com.cn.lp.converter.entity*/
    protected String packageName;
    /** 全名 eg: com.cn.lp.converter.entity.ElementInterface*/
    protected String fullName;
    /** 字节码元素 eg:java.util.HashMap*/
    protected TypeElement element;
    /** 方法列表 */
    private List<ElementMethod> methodList = new ArrayList<>();
    /** 泛型列表 */
    private Map<TypeVariable, ElementType> argEntityMap = new HashMap<>();
    /** 实体列表 */
    private Map<TypeVariable, RootElementClass> entityMap = new HashMap<>();

    public static RootElementInterface create(DeclaredType type, BeanConverterMapperAnnotation mapperAnnotation, ProcessorContext processorContext) {
        RootElementInterface elementInterface = new RootElementInterface();
        TypeElement element = (TypeElement) type.asElement();
        Map<TypeVariable, ElementType> argEntityMap = new HashMap<>();
        Map<TypeVariable, RootElementClass> entityMap = new HashMap<>();
        List<? extends TypeMirror> typeArguments = type.getTypeArguments();
        List<? extends TypeParameterElement> typeParameters = element.getTypeParameters();
        if(BlankAide.isNotBlank(typeParameters)) {
            for(int index = 0; index < typeParameters.size(); index ++) {
                TypeMirror typeMirror;
                if(index >= typeArguments.size()) {
                    typeMirror = processorContext.getElementUtils().getTypeElement(TypeName.OBJECT.toString()).asType();
                } else {
                    typeMirror = typeArguments.get(index);
                }
                ElementType argElementType = ElementType.create(typeMirror, Collections.emptyMap());
                argEntityMap.put((TypeVariable) typeParameters.get(index).asType(), argElementType);
                entityMap.put((TypeVariable) typeParameters.get(index).asType(), RootElementClass.create(
                    typeMirror,
                    mapperAnnotation,
                    processorContext
                ));
            }
        }
        elementInterface.argEntityMap = argEntityMap;
        elementInterface.entityMap = entityMap;
        elementInterface.name = element.getSimpleName().toString();
        elementInterface.fullName = element.getQualifiedName().toString();
        elementInterface.element = element;
        elementInterface.packageName = element.getQualifiedName().toString().replace("." + elementInterface.name, "");
        elementInterface.initMethod(processorContext);
        return elementInterface;
    }

    private void initMethod(ProcessorContext processorContext) {
        List<ElementMethod> list = new ArrayList<>();
        for (Element e :  this.element.getEnclosedElements()) {
            if (ElementKind.METHOD == e.getKind()) {
                ExecutableElement methodElement = (ExecutableElement) e;
                if(!methodElement.getModifiers().contains(Modifier.STATIC)
                ) {
                    list.add(ElementMethod.create(methodElement, this.getArgEntityMap(), processorContext));
                }
            }
        }
        this.methodList = list;
    }

    public Map<TypeVariable, ElementType> getArgEntityMap() {
        return argEntityMap;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFullName() {
        return fullName;
    }

    public TypeElement getElement() {
        return element;
    }

    public List<ElementMethod> getMethodList() {
        return methodList;
    }

    public Map<TypeVariable, RootElementClass> getEntityMap() {
        return entityMap;
    }

}
