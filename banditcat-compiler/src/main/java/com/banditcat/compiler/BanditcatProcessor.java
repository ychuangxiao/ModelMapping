package com.banditcat.compiler;

import com.banditcat.annotations.Filed;
import com.banditcat.annotations.FiledByClass;
import com.banditcat.annotations.FiledByCollection;
import com.banditcat.annotations.MappedClass;
import com.banditcat.annotations.Parse;
import com.banditcat.compiler.helper.AnnotationHelper;
import com.banditcat.compiler.helper.Constants;
import com.banditcat.compiler.model.FiledModel;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.sun.source.util.Trees;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;


/**
 * 文件名称：{@link BanditcatProcessor}
 * <br/>
 * 功能描述：注解处理类
 * <br/>
 * 创建作者：banditcat
 * <br/>
 * 创建时间：2018/1/19 10:27
 * <br/>
 * 修改作者：banditcat
 * <br/>
 * 修改时间：2018/1/19 10:27
 * <br/>
 * 修改备注：
 */
@AutoService(Process.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BanditcatProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private Trees trees;
    static Messager mMessager;


    private static final String ANIMATION_TYPE = "android.view.animation.Animation";

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
        mMessager = env.getMessager();
    }

    static void printMessage(String message) {
        mMessager.printMessage(Kind.NOTE, message);
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        mMessager.printMessage(kind, message, element);

    }

    private void logParsingError(Element element, Class<? extends Annotation> annotation,
            Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s binding.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();


        annotations.add(MappedClass.class);
        annotations.add(Filed.class);
        annotations.add(FiledByClass.class);
        annotations.add(FiledByCollection.class);
        annotations.add(Parse.class);

        return annotations;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private boolean nothingToDo(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return roundEnv.processingOver() || annotations.size() == 0;
    }

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param env
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

        mMessager.printMessage(Diagnostic.Kind.NOTE,
                "初始化2");
        if (nothingToDo(annotations, env)) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "no  process");

            return true;
        }


        Map<TypeElement, BindingSet> bindingMap = findAndParseTargets(env);

        mMessager.printMessage(Diagnostic.Kind.NOTE,
                "开始生成数据");
        for (Map.Entry<TypeElement, BindingSet> entry : bindingMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingSet binding = entry.getValue();

            JavaFile javaFile = binding.brewJava();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                error(typeElement, "Unable to write binding for type %s: %s", typeElement, e.getMessage());
            }
        }


        return false;
    }


    private Map<TypeElement, BindingSet> findAndParseTargets(RoundEnvironment env) {

        Map<TypeElement, BindingSet.Builder> builderMap = new LinkedHashMap<>();
        Set<TypeElement> erasedTargetNames = new LinkedHashSet<>();
        //1、先获取MappedClass
        //循环处理@MappedClass元素
        for (Element element : env.getElementsAnnotatedWith(MappedClass.class)) {

            try {
                parseMappedClassAnimation(element, builderMap, erasedTargetNames);
            } catch (Exception e) {
                logParsingError(element, MappedClass.class, e);
            }

        }

        //处理字段,在MappedClass注解对象下找到Filed注解
        for (Element element : erasedTargetNames) {


            findFiled(env, builderMap.get(element));
        }

        printMessage("我看你是不是结束了");


        Map<TypeElement, BindingSet> bindingMap = new LinkedHashMap<>();
        Iterator<Map.Entry<TypeElement, BindingSet.Builder>> entries = builderMap.entrySet().iterator();
        Map.Entry<TypeElement, BindingSet.Builder> entry;
        while (entries.hasNext()) {
            entry = entries.next();
            bindingMap.put(entry.getKey(), entry.getValue().build());
        }

        return bindingMap;
    }


    /**
     * 查找MappedClass
     *
     * @param element
     * @param builderMap
     * @param erasedTargetNames
     */
    private void parseMappedClassAnimation(Element element,
            Map<TypeElement, BindingSet.Builder> builderMap, Set<TypeElement> erasedTargetNames) {
        TypeElement enclosingElement = (TypeElement) element;
        getOrCreateBindingBuilder(builderMap, enclosingElement);
        erasedTargetNames.add(enclosingElement);
    }


    /**
     * 查找字段
     *
     * @param env
     * @param builder
     */
    private void findFiled(RoundEnvironment env,
            BindingSet.Builder builder) {


        FiledModel filedModel;

        for (Element element : env.getElementsAnnotatedWith(Filed.class)) {


            filedModel = new FiledModel();
            //获取注解信息
            AnnotationMirror annotationMirror = AnnotationHelper.getAnnotationMirror(element, Filed.class);
            AnnotationValue annotationValue = AnnotationHelper.getAnnotationValue(annotationMirror, Constants
                    .GENERATION_TOFIELD);

            printMessage("findFiled = " + element.getSimpleName().toString());
            printMessage("annotationValue = " + annotationValue.getValue().toString());

            filedModel.setName(element.getSimpleName().toString());
            filedModel.setToFiled(annotationValue.getValue().toString());


            //解析出转换注解信息-
            AnnotationMirror parserAnnotationMirror = AnnotationHelper.getAnnotationMirror(element, Parse.class);
            AnnotationValue parserAnnotationValue = AnnotationHelper.getAnnotationValue(parserAnnotationMirror,
                    Constants.GENERATION_TO_MODEL);



            filedModel.setParseToModelTypeName(ClassName.bestGuess(parserAnnotationValue.getValue().toString()));


            //解析转换信息-TO_ENTITY
            AnnotationMirror parserToEntityAnnotationMirror = AnnotationHelper.getAnnotationMirror(element, Parse
                    .class);
            AnnotationValue parserToEntityAnnotationValue = AnnotationHelper.getAnnotationValue
                    (parserToEntityAnnotationMirror,
                    Constants
                            .GENERATION_TO_ENTITY);

            filedModel.setParseToEntityTypeName(ClassName.bestGuess(parserToEntityAnnotationValue.getValue().toString()));


            builder.addFiledModelCollection(filedModel);
        }


    }

    private BindingSet.Builder getOrCreateBindingBuilder(
            Map<TypeElement, BindingSet.Builder> builderMap, TypeElement enclosingElement) {
        BindingSet.Builder builder = builderMap.get(enclosingElement);

        if (builder == null) {
            builder = BindingSet.newBuilder(enclosingElement);
            builderMap.put(enclosingElement, builder);
        }
        return builder;
    }
}
