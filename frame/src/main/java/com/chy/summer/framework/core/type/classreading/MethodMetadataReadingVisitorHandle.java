package com.chy.summer.framework.core.type.classreading;

import aj.org.objectweb.asm.Opcodes;
import com.chy.summer.framework.core.annotation.AnnotationAttributeHolder;
import com.chy.summer.framework.core.annotation.AnnotationAttributes;
import com.chy.summer.framework.core.annotation.AnnotationUtils;
import com.chy.summer.framework.core.type.DefaultAnnotationBehavior;
import com.chy.summer.framework.core.type.MethodMetadata;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *  asm 对于方法的解析处理类,同时 这个类也实现 MethodMetadata 当做容器类
 *  一个本类的对象代表了一个方法
 */
public class MethodMetadataReadingVisitorHandle extends MethodVisitor implements MethodMetadata, DefaultAnnotationBehavior {


    private final String methodName;
    private final int access;
    private final String className;
    private final String returnTypeName;
    /** 这个类上的所有方法的对象放这里面, 这当然也是外面传进来的 */
    private final Set<MethodMetadata> methodMetadataSet;


    private Map<String, AnnotationAttributeHolder> ownAllAnnotated = new HashMap<>();
    private Set<String> ownAllAnnotatedType = new HashSet<>();



    public MethodMetadataReadingVisitorHandle(String methodName, int access, String className, String returnTypeName,
                                              Set<MethodMetadata> methodMetadataSet) {
        super(Opcodes.ASM5);
        this.methodName = methodName;
        this.access = access;
        this.className = className;
        this.returnTypeName = returnTypeName;
        this.methodMetadataSet = methodMetadataSet;
        this.methodMetadataSet.add(this);
    }

    /**
     * 方法上每有一个注解就会调用一次这个方法
     * @param desc
     * @param visible
     * @return
     */
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
        String className = Type.getType(desc).getClassName();
        this.ownAllAnnotatedType.add(className);
        return new MetadataAnnotationVisitorHandle(className, this.ownAllAnnotated, this.ownAllAnnotatedType);
    }



    //======================================下面是 MethodMetadata 接口的实现==============================================


    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getDeclaringClassName() {
        return className;
    }

    @Override
    public String getReturnTypeName() {
        return returnTypeName;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isOverridable() {
        return false;
    }


    @Override
    public Set<String> getOwnAllAnnotatedType() {
        return ownAllAnnotatedType;
    }

    @Override
    public Map<String, AnnotationAttributeHolder> getOwnAllAnnotated() {
        return ownAllAnnotated;
    }
}
