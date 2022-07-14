package com.cn.lp.converter.processor;

import com.cn.lp.converter.utils.StringAide;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * lombok执行
 */
public class LombokProcessor extends AbstractProcessor {

    private Class<?> clazz;

    private Object object;

    private Messager messager;

    public static LombokProcessor create(Messager messager) {
        LombokProcessor lombokProcessor = new LombokProcessor();
        try {
            lombokProcessor.clazz = Class.forName("lombok.launch.AnnotationProcessorHider$AnnotationProcessor");
            lombokProcessor.object = lombokProcessor.clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            messager.printMessage(
                Diagnostic.Kind.WARNING,
                StringAide.format("加载lombok失败, 错误信息:{}", e));
        }
        lombokProcessor.messager = messager;
        return lombokProcessor;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        if(object != null) {
            try {
                Method method = clazz.getMethod("init", ProcessingEnvironment.class);
                method.invoke(object, processingEnv);
            }catch (Exception e) {
                messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    StringAide.format("加载lombok失败, 错误信息:{}", e));
            }
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(object != null) {
            try {
                Method method = clazz.getMethod("process", Set.class, RoundEnvironment.class);
                Object invoke = method.invoke(object, annotations, roundEnv);
                return (boolean) invoke;
            }catch (Exception e) {
                messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    StringAide.format("加载lombok失败, 错误信息:{}", e));
            }
        }
        return false;
    }


}
