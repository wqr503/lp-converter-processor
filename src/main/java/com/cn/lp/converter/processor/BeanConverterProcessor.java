package com.cn.lp.converter.processor;

import com.cn.lp.converter.BeanConverter;
import com.cn.lp.converter.MapConverter;
import com.cn.lp.converter.annotation.ConverterMapper;
import com.cn.lp.converter.entity.*;
import com.cn.lp.converter.exception.ProcessorException;
import com.cn.lp.converter.generator.*;
import com.cn.lp.converter.utils.BlankAide;
import com.cn.lp.converter.utils.ElementAide;
import com.cn.lp.converter.utils.StringAide;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * Created by qirong on 2022/1/31.
 */
@SupportedAnnotationTypes("com.cn.lp.converter.annotation.ConverterMapper")
@AutoService(Processor.class)
public class BeanConverterProcessor extends AbstractProcessor {

    private ProcessorContext processorContext;

    private LombokProcessor lombokProcessor;

    //这个方法用于初始化处理器，方法中有一个ProcessingEnvironment类型的参数，
    //ProcessingEnvironment是一个注解处理工具的集合。它包含了众多工具类。例如：
    //Filer可以用来编写新文件；
    //Messager可以用来打印错误信息；
    //Elements是一个可以处理Element的工具类。
    //Element已知的子接口有如下几种：
    //PackageElement 表示一个包程序元素。提供对有关包及其成员的信息的访问。
    //ExecutableElement 表示某个类或接口的方法、构造方法或初始化程序（静态或实例），包括注释类型元素。
    //TypeElement 表示一个类或接口程序元素。提供对有关类型及其成员的信息的访问。注意，枚举类型是一种类，而注解类型是一种接口。
    //VariableElement 表示一个字段、enum 常量、方法或构造方法参数、局部变量或异常参数。
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        if(System.getProperty("lombok.disable") == null) {
            lombokProcessor = LombokProcessor.create(processingEnv.getMessager());
            lombokProcessor.init(processingEnv);
        }
        processorContext = new ProcessorContext(
            processingEnv.getElementUtils(),
            processingEnv.getTypeUtils(),
            processingEnv.getMessager(),
            processingEnv.getFiler());
    }

    //终于，到了FactoryProcessor类中最后一个也是最重要的一个方法了。先看这个方法的返回值，是一个boolean类型，
    //返回值表示注解是否由当前Processor 处理。如果返回 true，则这些注解由此注解来处理，后续其它的 Processor 无需再处理它们；
    //如果返回 false，则这些注解未在此Processor中处理并，那么后续 Processor 可以继续处理它们。
    //在这个方法的方法体中，我们可以校验被注解的对象是否合法、可以编写处理注解的代码，以及自动生成需要的java文件等。
    //因此说这个方法是AbstractProcessor 中的最重要的一个方法。我们要处理的大部分逻辑都是在这个方法中完成。
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(lombokProcessor != null) {
            lombokProcessor.process(annotations, roundEnv);
        }
        // 忽略多轮编译
        if (!roundEnv.errorRaised() && !roundEnv.processingOver()) {
            for (TypeElement annotation : annotations) {
                if(ConverterMapper.class.getCanonicalName().equals(annotation.getQualifiedName().toString())) {
                    // 目标对象
                    for (Element rootElement : roundEnv.getElementsAnnotatedWith(annotation)) {
                        // 校验合法
                        if(rootElement.getKind() != ElementKind.INTERFACE) {
                            throw new ProcessorException(rootElement.toString() + " only interface can be annotation with BeanConverterMapper, current is " + rootElement.getKind());
                        }
                        TypeElement typeElement = (TypeElement) rootElement;
                        if(typeElement.getTypeParameters() != null && typeElement.getTypeParameters().size() > 0) {
                            throw new ProcessorException(rootElement.toString() + " interface must not has <?>");
                        }
                        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
                        List<RootElementInterface> convertInterfaceList = new ArrayList<>();
                        List<ElementInterface> interfaceList = new ArrayList<>();
                        if(BlankAide.isBlank(interfaces)) {
                            throw new ProcessorException(rootElement.toString() + " need interface BeanConverter or MapConverter");
                        }
                        // 生成当前接口
                        ElementInterface rootInterface = ElementInterface.createNotSupport((DeclaredType) rootElement.asType(), processorContext);
                        String beanConverterTypeName = ElementAide.getSimpleTypeName(TypeName.get(BeanConverter.class));
                        String mapConverterTypeNae = ElementAide.getSimpleTypeName(TypeName.get(MapConverter.class));
                        BeanConverterMapperAnnotation mapperAnnotation = (BeanConverterMapperAnnotation) rootInterface.getAnnotation(BeanConverterMapperAnnotation.ANNOTATION_TYPE_NAME);
                        for (TypeMirror interfaceType : interfaces) {
                            String interfaceTypeName = ElementAide.getSimpleTypeName(TypeName.get(interfaceType));
                            if(beanConverterTypeName.equals(interfaceTypeName)) {
                                convertInterfaceList.add(RootElementInterface.create((DeclaredType) interfaceType,
                                    mapperAnnotation, processorContext));
                            } else if(mapConverterTypeNae.equals(interfaceTypeName)) {
                                convertInterfaceList.add(RootElementInterface.create((DeclaredType) interfaceType,
                                    mapperAnnotation, processorContext));
                            }else {
                                // 生成除ConvertBean接口的其他接口
                                interfaceList.add(ElementInterface.create((DeclaredType) interfaceType, processorContext));
                            }
                        }
                        if(BlankAide.isBlank(convertInterfaceList)) {
                            throw new ProcessorException(rootElement.toString() + " need interface BeanConverter or MapConverter");
                        }
                        try {
                            // 生成BeanConverter接口
                            LinkedHashMap<String, MethodGenerator> methodMap = new LinkedHashMap<>();
                            // Converter接口方法，优先级最高
                            for (RootElementInterface convertInterface : convertInterfaceList) {
                                for (ElementMethod elementMethod : convertInterface.getMethodList()) {
                                    if(elementMethod.getName().startsWith("post")
                                        || elementMethod.getName().startsWith("pre")) {
                                        methodMap.put(elementMethod.getSign(), MethodGenerator.create(elementMethod));
                                    } else {
                                        ConverterMethodGenerator generator = ConverterMethodGenerator.create(convertInterface, elementMethod);
                                        methodMap.put(elementMethod.getSign(), generator);
                                    }
                                }
                            }
                            // 其他接口方法，如果重复，则抛弃
                            for (ElementInterface elementInterface : interfaceList) {
                                for (ElementMethod elementMethod : elementInterface.getMethodList()) {
                                    MethodGenerator oldMethod = methodMap.get(elementMethod.getSign());
                                    if(oldMethod != null) {
                                        if(elementMethod.isDefaultMethod()){
                                            oldMethod.setDefaultMethod(true);
                                        }
                                        oldMethod.mergeAnnotationList(elementMethod.getAnnotationMap().values());
                                    } else {
                                        methodMap.put(elementMethod.getSign(), MethodGenerator.create(elementMethod));
                                    }
                                }
                                rootInterface.mergeAnnotationList(elementInterface.getAnnotationMap().values());
                            }
                            // 当前接口方法
                            for (ElementMethod elementMethod : rootInterface.getMethodList()) {
                                MethodGenerator methodGenerator = methodMap.get(elementMethod.getSign());
                                if(methodGenerator != null) {
                                    if(elementMethod.isDefaultMethod()){
                                        methodGenerator.setDefaultMethod(true);
                                    }
                                    methodGenerator.mergeAnnotationList(elementMethod.getAnnotationMap().values());
                                } else {
                                    methodGenerator = MethodGenerator.create(elementMethod);
                                }
                                methodMap.put(elementMethod.getSign(), methodGenerator);
                            }
                            LinkedList<MethodGenerator> generateMethodList = new LinkedList<>();
                            LinkedList<MethodGenerator> methodList = new LinkedList<>();
                            for (MethodGenerator value : methodMap.values()) {
                                if(value instanceof ConverterMethodGenerator) {
                                    ((ConverterMethodGenerator) value).init(rootInterface);
                                }
                                if(!value.isDefaultMethod()) {
                                    generateMethodList.addFirst(value);
                                }
                                methodList.addFirst(value);
                            }
                            // BeanConverter生成
                            ConverterGenerator.build(rootInterface, generateMethodList, MethodHolder.build(methodList), processorContext);
                        } catch (Exception e) {
                            throw new ProcessorException(StringAide.format("生成class异常, 错误信息:{}", e));
                        }
                    }
                }
            }
        }
        return false;
    }

    //这个方法的返回值是一个Set集合，集合中指要处理的注解类型的名称(这里必须是完整的包名+类名，
    // 例如com.example.annotation.Factory)。由于在本例中只需要处理@Factory注解，因此Set集合中只需要添加@Factory的名称即可。
//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        Set<String> annotations = new LinkedHashSet<>();
//        annotations.add(Factory.class.getCanonicalName());
//        return annotations;
//    }

    //这个方法非常简单，只有一个返回值，用来指定当前正在使用的Java版本，
    //通常return SourceVersion.latestSupported()即可。
//    @Override
//    public SourceVersion getSupportedSourceVersion() {
//        return SourceVersion.latestSupported();
//    }

}

//package com.zhpan.mannotation.factory;  //    PackageElement
//
//public class Circle {  //  TypeElement
//
//    private int i; //   VariableElement
//    private Triangle triangle;  //  VariableElement
//
//    public Circle() {} //    ExecuteableElement
//
//    public void draw(   //  ExecuteableElement
//        String s)   //  VariableElement
//    {
//        System.out.println(s);
//    }
//
//    @Override
//    public void draw() {    //  ExecuteableElement
//        System.out.println("Draw a circle");
//    }
//}
