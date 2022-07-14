package com.cn.lp.converter.generator;

import com.cn.lp.converter.ConverterHolder;
import com.cn.lp.converter.entity.*;
import com.cn.lp.converter.processor.ProcessorContext;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.*;

/**
 * BeanConvert生成器
 */
public class ConverterGenerator {

    public static void build(ElementInterface rootInterface, Collection<MethodGenerator> methodGeneratorList,
        MethodHolder methodHolder, ProcessorContext processorContext) throws IOException {
        List<MethodSpec> generateMethodList = new ArrayList<>();
        for (MethodGenerator methodGenerator : methodGeneratorList) {
            generateMethodList.add(methodGenerator.buildSpec(methodHolder, processorContext));
        }
        String className = rootInterface.getName() + ConverterHolder.BEAN_SUFFIX;
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(TypeName.get(rootInterface.getType()))
            .addMethods(generateMethodList)
            .addInitializerBlock(CodeBlock.builder()
                .addStatement("$T.registerConverter($T.class, this)", ClassName.get(ConverterHolder.class), rootInterface.getType())
                .build())
            .addStaticBlock(CodeBlock.builder()
                .addStatement("$L instance = new $L()", className, className)
                .build());
        BeanConverterMapperAnnotation annotation = (BeanConverterMapperAnnotation) rootInterface.getAnnotation(BeanConverterMapperAnnotation.ANNOTATION_TYPE_NAME);
        if(annotation.isImplSpringInterface()) {
            builder.addAnnotation(ClassName.bestGuess("org.springframework.stereotype.Component"));
        }
        TypeSpec beanConverter = builder.build();
        JavaFile javaFile = JavaFile.builder(rootInterface.getPackageName(), beanConverter)
            .build();
        javaFile.writeTo(processorContext.getFiler());
    }

}
