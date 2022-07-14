package com.cn.lp.converter.entity;

import com.cn.lp.converter.exception.ProcessorException;
import com.cn.lp.converter.processor.ProcessorContext;
import com.cn.lp.converter.utils.BlankAide;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import java.util.*;

/**
 * BeanConvert interface中source 和 target 实体的封装类
 */
public class RootElementClass {

    /** 类名  eg: ElementInterface*/
    protected String name;
    /** 包名 eg: com.cn.lp.converter.entity*/
    protected String packageName;
    /** 全名 eg: com.cn.lp.converter.entity.ElementInterface*/
    protected String fullName;
    /** 字节码元素 eg:java.util.HashMap*/
    protected TypeElement element;
    /** 泛型列表 */
    protected Map<TypeVariable, ElementType> argEntityMap = new HashMap<>();
    /** 对象类型 eg:java.util.HashMap<java.lang.String,java.lang.Integer>*/
    private TypeMirror type;
    /** 字段列表*/
    private Map<String, RootElementField> fieldMap = new HashMap<>();
    /** 方法列表 */
    private List<ElementMethod> methodList = new ArrayList<>();

    public static RootElementClass create(TypeMirror type, BeanConverterMapperAnnotation mapperAnnotation, ProcessorContext processorContext) {
        return create(type, Collections.emptyMap(), mapperAnnotation, processorContext);
    }

    public static RootElementClass create(TypeMirror type, Map<TypeVariable, ElementType> sourceArgEntityMap,
        BeanConverterMapperAnnotation mapperAnnotation, ProcessorContext processorContext) {
        RootElementClass rootElementClass = new RootElementClass();
        DeclaredType elementType;
        if(type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            if(wildcardType.getExtendsBound() != null) {
                elementType = (DeclaredType) wildcardType.getExtendsBound();
            } else if(wildcardType.getSuperBound() != null) {
                elementType = (DeclaredType) wildcardType.getSuperBound();
            } else {
                elementType = (DeclaredType) processorContext.getElementUtils().getTypeElement(TypeName.OBJECT.toString()).asType();
            }
        } else if(type instanceof DeclaredType) {
            elementType = (DeclaredType) type;
        } else {
            throw new ProcessorException("RootElementClass create " + type.getClass() + " not match");
        }
        TypeElement element = (TypeElement) elementType.asElement();
        Map<TypeVariable, ElementType> argEntityMap = new HashMap<>();
        List<? extends TypeMirror> typeArguments = elementType.getTypeArguments();
        List<? extends TypeParameterElement> typeParameters = element.getTypeParameters();
        if(BlankAide.isNotBlank(typeParameters)) {
            for(int index = 0; index < typeParameters.size(); index ++) {
                TypeMirror typeMirror;
                if(index >= typeArguments.size()) {
                    typeMirror = processorContext.getElementUtils().getTypeElement(TypeName.OBJECT.toString()).asType();
                } else {
                    typeMirror = typeArguments.get(index);
                }
                ElementType argElementType = ElementType.create(typeMirror, sourceArgEntityMap);
                argEntityMap.put((TypeVariable) typeParameters.get(index).asType(), argElementType);
            }
        }
        rootElementClass.type = type;
        rootElementClass.argEntityMap = argEntityMap;
        rootElementClass.name = element.getSimpleName().toString();
        rootElementClass.fullName = element.getQualifiedName().toString();
        rootElementClass.element = element;
        rootElementClass.packageName = element.getQualifiedName().toString().replace("." + rootElementClass.name, "");
        rootElementClass.initMethod(processorContext);
        rootElementClass.initField();
        if(!TypeName.OBJECT.toString().equals(element.getSuperclass().toString()) &&
            !"none".equals(element.getSuperclass().toString())) {
            RootElementClass superEntity = RootElementClass.create(element.getSuperclass(), argEntityMap, mapperAnnotation, processorContext);
            rootElementClass.mergeFieldList(superEntity.getFieldMap());
            rootElementClass.mergeMethodList(superEntity.getMethodList());
        }
        rootElementClass.filterField(mapperAnnotation);
        return rootElementClass;
    }

    private void mergeFieldList(Map<String, RootElementField> fields) {
        for (Map.Entry<String, RootElementField> entry : fields.entrySet()) {
            RootElementField oldField = this.fieldMap.get(entry.getKey());
            if(oldField != null) {
                oldField.mergeAnnotationList(entry.getValue().getAnnotationMap().values());
            } else {
                this.fieldMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void mergeMethodList(Collection<ElementMethod> methods) {
        Map<String, ElementMethod> methodMap = new HashMap<>();
        for (ElementMethod method : methods) {
            methodMap.put(method.getSign(), method);
        }
        for (ElementMethod method : methods) {
            ElementMethod oldMethod = methodMap.get(method.getSign());
            if(oldMethod != null) {
                this.methodList.add(method);
            }
        }
    }

    /** 初始化方法 */
    private void initMethod(ProcessorContext processorContext) {
        List<ElementMethod> list = new ArrayList<>();
        for (Element e :  this.element.getEnclosedElements()) {
            if (ElementKind.METHOD == e.getKind()) {
                ExecutableElement methodElement = (ExecutableElement) e;
                // 获取public 并且是 非static 方法
                if(methodElement.getModifiers().contains(Modifier.PUBLIC) &&
                    !methodElement.getModifiers().contains(Modifier.STATIC)
                ) {
                    list.add(ElementMethod.create(methodElement, this.argEntityMap,processorContext));
                }
            }
        }
        this.methodList = list;
    }

    private void filterField(BeanConverterMapperAnnotation mapperAnnotation) {
        Map<String, RootElementField> newFieldMap = new HashMap<>();
        for (RootElementField field : this.fieldMap.values()) {
            if(matchField(mapperAnnotation, field)) {
                newFieldMap.put(field.getName(), field);
            }
        }
        this.fieldMap = newFieldMap;
    }

    /** 初始化属性 */
    private void initField() {
        Map<String, ElementMethod> methodMap = new HashMap<>();
        for (ElementMethod method : this.methodList) {
            methodMap.put(method.getSign(), method);
        }
        Map<String, RootElementField> fieldMap = new HashMap<>();
        for (Element e :  this.element.getEnclosedElements()) {
            if (ElementKind.FIELD == e.getKind()) {
                if(!e.getModifiers().contains(Modifier.STATIC)
                ) {
                    RootElementField rootElementField = RootElementField.create(e, this.argEntityMap, methodMap);
                    fieldMap.put(rootElementField.getName(), rootElementField);
                }
            }
        }
        this.fieldMap = fieldMap;
    }

    private boolean matchField(BeanConverterMapperAnnotation mapperAnnotation, RootElementField field) {
        List<String> assignFieldNameList = mapperAnnotation.getAssignFieldName();
        if(BlankAide.isNotBlank(assignFieldNameList)) {
            for (String fieldName : assignFieldNameList) {
                if(field.getName().equals(fieldName)) {
                    return true;
                }
            }
            return false;
        }
        List<TypeMirror> assignAnnotationList = mapperAnnotation.getAssignAnnotation();
        if(BlankAide.isNotBlank(assignAnnotationList)) {
            for (TypeMirror assignAnnotation : assignAnnotationList) {
                ElementAnnotation elementAnnotation = field.getAnnotationMap().get(TypeName.get(assignAnnotation));
                if(BlankAide.isNotBlank(elementAnnotation)) {
                    return true;
                }
            }
            return false;
        }
        List<TypeMirror> assignIgnoreAnnotationList = mapperAnnotation.getAssignIgnoreAnnotation();
        if(BlankAide.isNotBlank(assignIgnoreAnnotationList)) {
            for (TypeMirror annotationValue : assignIgnoreAnnotationList) {
                ElementAnnotation elementAnnotation = field.getAnnotationMap().get(TypeName.get(annotationValue));
                if(BlankAide.isNotBlank(elementAnnotation)) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public Map<String, RootElementField> getFieldMap() {
        return fieldMap;
    }

    public TypeMirror getType() {
        return type;
    }

    public TypeElement getElement() {
        return element;
    }

    public List<ElementMethod> getMethodList() {
        return methodList;
    }
}
