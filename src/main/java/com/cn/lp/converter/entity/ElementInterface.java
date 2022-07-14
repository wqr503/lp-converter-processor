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
 * 接口元素封装类
 */
public class ElementInterface {

    /** 类名  eg: ElementInterface*/
    protected String name;
    /** 包名 eg: com.cn.lp.converter.entity*/
    protected String packageName;
    /** 全名 eg: com.cn.lp.converter.entity.ElementInterface*/
    protected String fullName;
    /** 字节码元素 */
    protected TypeElement element;
    /** 类型元素 */
    protected TypeMirror type;
    /** 方法列表 */
    protected Map<String, ElementMethod> methodMap = new HashMap<>();
    /** 注解列表 */
    protected Map<TypeName, ElementAnnotation> annotationMap = new HashMap<>();
    /** 泛型列表 */
    protected Map<TypeVariable, ElementType> argEntityMap = new HashMap<>();

    /** 静态构建方法(含子类) */
    public static ElementInterface create(DeclaredType type, ProcessorContext processorContext) {
        ElementInterface elementInterface = create(type, Collections.emptyMap(), processorContext);
        for (TypeMirror inter : elementInterface.element.getInterfaces()) {
            ElementInterface interfaceEntity = ElementInterface.create((DeclaredType) inter, elementInterface.argEntityMap, processorContext);
            elementInterface.mergeMethodList(interfaceEntity.getMethodList());
        }
        return elementInterface;
    }

    /** 静态构建方法(不包含父类) */
    public static ElementInterface createNotSupport(DeclaredType type, ProcessorContext processorContext) {
        return createInterface(type, Collections.emptyMap(), processorContext);
    }

    /** 静态构建方法(父类构建子类-含子类)  */
    public static ElementInterface create(DeclaredType type, Map<TypeVariable, ElementType> sourceArgEntityMap, ProcessorContext processorContext) {
        ElementInterface elementInterface = createInterface(type, sourceArgEntityMap, processorContext);
        for (TypeMirror inter : elementInterface.element.getInterfaces()) {
            ElementInterface interfaceEntity = ElementInterface.create((DeclaredType) inter, elementInterface.argEntityMap, processorContext);
            elementInterface.mergeMethodList(interfaceEntity.getMethodList());
        }
        return elementInterface;
    }

    /** 静态构建方法(父类构建子类) */
    private static ElementInterface createInterface(DeclaredType type, Map<TypeVariable, ElementType> sourceArgEntityMap, ProcessorContext processorContext) {
        ElementInterface elementInterface = new ElementInterface();
        TypeElement element = (TypeElement) type.asElement();
        Map<TypeVariable, ElementType> argEntityMap = new HashMap<>();
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
                ElementType elementType = ElementType.create(typeMirror, sourceArgEntityMap);
                argEntityMap.put((TypeVariable) typeParameters.get(index).asType(), elementType);
            }
        }
        elementInterface.argEntityMap = argEntityMap;
        Map<TypeName, ElementAnnotation> annotationMap = new HashMap<>();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            ElementAnnotation elementAnnotation = ElementAnnotation.build(annotationMirror);
            annotationMap.put(elementAnnotation.getTypeName(), elementAnnotation);
        }
        elementInterface.annotationMap = annotationMap;
        elementInterface.name = element.getSimpleName().toString();
        elementInterface.fullName = element.getQualifiedName().toString();
        elementInterface.element = element;
        elementInterface.type = type;
        elementInterface.packageName = element.getQualifiedName().toString().replace("." + elementInterface.name, "");
        elementInterface.initMethod(processorContext);
        return elementInterface;
    }

    /** 合并方法 - 如果已有方法则判断是否含有default否则不添加 */
    public void mergeMethodList(Collection<ElementMethod> methodList) {
        for (ElementMethod elementMethod : methodList) {
            ElementMethod oldMethod = methodMap.get(elementMethod.getSign());
            if(oldMethod != null) {
                if(elementMethod.isDefaultMethod()){
                    oldMethod.setDefaultMethod(true);
                }
                oldMethod.mergeAnnotationList(elementMethod.getAnnotationMap().values());
            } else {
                methodMap.put(elementMethod.getSign(), elementMethod);
            }
        }
    }

    /** 合并注解 - 如有已有注解则不添加，否则添加 */
    public void mergeAnnotationList(Collection<ElementAnnotation> annotationList) {
        for (ElementAnnotation elementAnnotation : annotationList) {
            ElementAnnotation oldAnnotation = annotationMap.get(elementAnnotation.getTypeName());
            if(BlankAide.isNotBlank(oldAnnotation)) {
                continue;
            }
            this.annotationMap.put(elementAnnotation.getTypeName(), elementAnnotation);
        }
    }

    /** 初始化方法 */
    private void initMethod(ProcessorContext processorContext) {
        Map<String, ElementMethod> methodMap = new HashMap<>();
        for (Element e :  this.element.getEnclosedElements()) {
            if (ElementKind.METHOD == e.getKind()) {
                ExecutableElement methodElement = (ExecutableElement) e;
                // 找出非static方法
                if(!methodElement.getModifiers().contains(Modifier.STATIC)) {
                    ElementMethod method = ElementMethod.create(methodElement, this.getArgEntityMap(), processorContext);
                    methodMap.put(method.getSign(), method);
                }
            }
        }
        this.methodMap = methodMap;
    }

    public ElementAnnotation getAnnotation(TypeName typeName) {
        return annotationMap.get(typeName);
    }

    public Collection<ElementMethod> getMethodList() {
        return methodMap.values();
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

    public Map<TypeName, ElementAnnotation> getAnnotationMap() {
        return annotationMap;
    }

    public Map<TypeVariable, ElementType> getArgEntityMap() {
        return argEntityMap;
    }

    public TypeMirror getType() {
        return this.type;
    }

}
