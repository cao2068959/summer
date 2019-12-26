package com.chy.summer.framework.core.type.classreading;

import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.core.type.AnnotationMetadata;
import com.chy.summer.framework.core.type.ClassMetadata;
import com.chy.summer.framework.core.type.MethodMetadata;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.StringUtils;
import com.sun.istack.internal.Nullable;
import jdk.internal.org.objectweb.asm.*;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * ASM 的访问器
 * 这里注解数据和类数据都放一起,用2个接口来区分隔离
 */
public class ClassMetadataReadingVisitor extends ClassVisitor implements AnnotationMetadata, ClassMetadata {
    private String className = "";

    private boolean isInterface;

    private boolean isAnnotation;

    private boolean isAbstract;

    private boolean isFinal;

    @Nullable
    private String enclosingClassName;

    private boolean independentInnerClass;

    @Nullable
    private String superClassName;

    private String[] interfaces = new String[0];

    private Set<String> memberClassNames = new LinkedHashSet<>(4);

    protected final Set<MethodMetadata> methodMetadataSet = new LinkedHashSet<>(4);

    /**
     * 记录了所有的注解的全路径
     */
    protected final Set<String> annotationSet = new LinkedHashSet<>(4);

    /**
     * 存放注解里面对应设置的属性值
     * key: 注解全路径 , value 注解的属性值
     */
    private final Map<String, AnnotationAttributes> annotationAttributes = new HashMap<>();

    /**
     * 存放 注解的父类有哪些
     */
    private final Map<String, Set<String>> metaAnnotationMap = new HashMap<>();


    public ClassMetadataReadingVisitor() {
        super(Opcodes.ASM5);
    }


    /**
     * 通过asm获取一些基本的 class信息
     */
    @Override
    public void visit(
            int version, int access, String name, String signature, @Nullable String supername, String[] interfaces) {
        //这里会把 className 转换一下  aa/bb/cc -> aa.bb.cc 的形式
        this.className = ClassUtils.convertResourcePathToClassName(name);
        //下面都是用的 bit 桶来存一些类的基本数据
        this.isInterface = ((access & Opcodes.ACC_INTERFACE) != 0);
        this.isAnnotation = ((access & Opcodes.ACC_ANNOTATION) != 0);
        this.isAbstract = ((access & Opcodes.ACC_ABSTRACT) != 0);
        this.isFinal = ((access & Opcodes.ACC_FINAL) != 0);

        //如果不是接口,就保存一下父类的信息
        if (supername != null && !this.isInterface) {
            this.superClassName = ClassUtils.convertResourcePathToClassName(supername);
        }

        //保存一下所有接口的信息
        this.interfaces = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            this.interfaces[i] = ClassUtils.convertResourcePathToClassName(interfaces[i]);
        }
    }

    /**
     * 获取外部类的方法
     */
    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        this.enclosingClassName = ClassUtils.convertResourcePathToClassName(owner);
    }

    /**
     * 获取内部类的方法
     */
    @Override
    public void visitInnerClass(String name, @Nullable String outerName, String innerName, int access) {
        if (outerName != null) {
            String fqName = ClassUtils.convertResourcePathToClassName(name);
            String fqOuterName = ClassUtils.convertResourcePathToClassName(outerName);
            if (this.className.equals(fqName)) {
                this.enclosingClassName = fqOuterName;
                this.independentInnerClass = ((access & Opcodes.ACC_STATIC) != 0);
            } else if (this.className.equals(fqOuterName)) {
                this.memberClassNames.add(fqName);
            }
        }
    }


    /**
     * 注解的解析处理
     */
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
        if (isAnnotation && desc.startsWith("Ljava/lang/annotation")) {
            //如果这个source本身就是annotion就跳过
            return null;
        }
        String className = Type.getType(desc).getClassName();
        this.annotationSet.add(className);
        return new MetadataAnnotationVisitorHandle(className, annotationAttributes, metaAnnotationMap);
    }


    /**
     * 这个类里面所有方法的解析
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ((access & Opcodes.ACC_BRIDGE) != 0) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
        //反正正真的返回值类型
        String returnType = Type.getReturnType(desc).getClassName();

        return new MethodMetadataReadingVisitorHandle(name, access, getClassName(),
                Type.getReturnType(desc).getClassName(), this.methodMetadataSet);
    }


    //================================下面是AnnotationMetadata接口的实现======================================

    @Override
    public boolean hasAnnotation(String annotationName) {
        return annotationSet.contains(annotationName);
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public boolean hasAnnotatedMethods(String annotationName) {
        for (MethodMetadata methodMetadata : this.methodMetadataSet) {
            if (methodMetadata.isAnnotated(annotationName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getAnnotationTypes() {
        return annotationSet;
    }

    /**
     * 获取类里面打了某个注解的 所有方法
     *
     * @param annotationName
     * @return
     */
    @Override
    public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
        Set<MethodMetadata> annotatedMethods = new LinkedHashSet<>(4);
        for (MethodMetadata methodMetadata : this.methodMetadataSet) {
            if (methodMetadata.isAnnotated(annotationName)) {
                annotatedMethods.add(methodMetadata);
            }
        }
        return annotatedMethods;
    }

    @Override
    public boolean isAnnotation() {
        return false;
    }

    @Override
    public boolean hasMetaAnnotation(String metaAnnotationName) {
        if(metaAnnotationMap.containsKey(metaAnnotationName)){
            return true;
        }

        return metaAnnotationMap.values().stream()
                .anyMatch(metaAnnotationSet -> metaAnnotationSet.contains(metaAnnotationName));
    }

    @Override
    public boolean isIndependent() {
        return (this.enclosingClassName == null || this.independentInnerClass);
    }

    @Override
    public boolean hasSuperClass() {
        return (this.superClassName != null);
    }

    @Override
    public boolean hasEnclosingClass() {
        return false;
    }

    @Override
    public String getEnclosingClassName() {
        return null;
    }


    @Override
    public String getSuperClassName() {
        return superClassName;
    }

    @Override
    public String[] getInterfaceNames() {
        return new String[0];
    }

    @Override
    public String[] getMemberClassNames() {
        return new String[0];
    }

    @Override
    public boolean isAbstract() {
        return this.isAbstract;
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(Class<? extends Annotation> type) {
        return annotationAttributes.get(type.getName());
    }

    @Override
    public Map<String,AnnotationAttributes> getAnnotationAttributesAll(Class<? extends Annotation> type){
       return AnnotationUtils.getAnnotationAttributesAll(type,metaAnnotationMap,annotationAttributes);
    }


    @Override
    public boolean isConcrete() {
        return !(this.isInterface || this.isAbstract);
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    //================================下面是暂时不需要的属性,都是空实现,需要的时候再去重写======================================

    @Override
    public void visitSource(String source, String debug) {
        // no-op
    }

    @Override
    public void visitAttribute(Attribute attr) {
        // no-op
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        // no-op
        return new EmptyFieldVisitor();
    }


    @Override
    public void visitEnd() {
        // no-op
    }


    private static class EmptyMethodVisitor extends MethodVisitor {

        public EmptyMethodVisitor() {
            super(Opcodes.ASM5);
        }
    }


    private static class EmptyFieldVisitor extends FieldVisitor {

        public EmptyFieldVisitor() {
            super(Opcodes.ASM5);
        }
    }

    @Override
    public boolean isAnnotated(String annotationName) {
        return false;
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(String annotationName) {
        return null;
    }

    @Override
    public AnnotationAttributes getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return null;
    }
}
