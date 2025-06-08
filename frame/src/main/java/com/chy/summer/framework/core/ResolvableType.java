
package com.chy.summer.framework.core;

import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ObjectUtils;
import com.chy.summer.framework.util.StringUtils;
import javax.annotation.Nullable;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.chy.summer.framework.core.SerializableTypeWrapper.TypeProvider;

/**
 * 用来解析 类上面的泛型的包装类型
 */
public class ResolvableType implements Serializable {

    /**
     * 这代表了一个空的类型
     */
    public static final ResolvableType NONE = new ResolvableType(EmptyType.INSTANCE, null, null, 0);

    /**
     * 这代表了一个 空的类型数组
     */
    private static final ResolvableType[] EMPTY_TYPES_ARRAY = new ResolvableType[0];

    private static final Map<ResolvableType, ResolvableType> cache =
            new ConcurrentHashMap<>(256);


    /**
     * 对应操作类的type
     */
    private final Type type;

    /**
     *  类型提供者
     */
    @Nullable
    private final TypeProvider typeProvider;

    /**
     * 变量解析器
     */
    @Nullable
    private final VariableResolver variableResolver;

    /**
     * 数组的组件类型
     */
    @Nullable
    private final ResolvableType componentType;

    @Nullable
    private final Integer hash;

    /**
     * 要操作的类型本尊
     */
    @Nullable
    private Class<?> resolved;

    /**
     *  要操作的类型的父类
     */
    @Nullable
    private volatile ResolvableType superType;

    /**
     *  要操作的类型的接口
     */
    @Nullable
    private volatile ResolvableType[] interfaces;

    /**
     *  要操作的类型的泛型
     */
    @Nullable
    private volatile ResolvableType[] generics;


    /**
     *  可以看做 一个 class 的包装类
     * @param type
     * @param typeProvider
     * @param variableResolver
     */
    private ResolvableType(
            Type type, @Nullable TypeProvider typeProvider, @Nullable VariableResolver variableResolver) {

        this.type = type;
        this.typeProvider = typeProvider;
        this.variableResolver = variableResolver;
        this.componentType = null;
        this.hash = calculateHashCode();
        this.resolved = null;
    }



    private ResolvableType(Type type, @Nullable TypeProvider typeProvider,
                           @Nullable VariableResolver variableResolver, @Nullable Integer hash) {

        this.type = type;
        this.typeProvider = typeProvider;
        this.variableResolver = variableResolver;
        this.componentType = null;
        this.hash = hash;
        this.resolved = resolveClass();
    }


    private ResolvableType(Type type, @Nullable TypeProvider typeProvider,
                           @Nullable VariableResolver variableResolver, @Nullable ResolvableType componentType) {

        this.type = type;
        this.typeProvider = typeProvider;
        this.variableResolver = variableResolver;
        this.componentType = componentType;
        this.hash = null;
        this.resolved = resolveClass();
    }


    private ResolvableType(@Nullable Class<?> clazz) {
        this.resolved = (clazz != null ? clazz : Object.class);
        this.type = this.resolved;
        this.typeProvider = null;
        this.variableResolver = null;
        this.componentType = null;
        this.hash = null;
    }


    /**
     *  返回类的Type类型,如果对应的Type 被SerializableType 包装过,就获取真实的Type
     */
    public Type getType() {
        return SerializableTypeWrapper.unwrap(this.type);
    }



    /**
     * 如果类型提供者 typeProvider 里面 提供了对应的类型,就返回
     * 如果没有就返回 this.type
     */
    public Object getSource() {
        Object source = (this.typeProvider != null ? this.typeProvider.getSource() : null);
        return (source != null ? source : this.type);
    }

    /**
     * 判断一个实例对象 是否是这个 class包装类 的实例对象
     */
    public boolean isInstance(@Nullable Object obj) {
        return (obj != null && isAssignableFrom(obj.getClass()));
    }

    /**
     * 判断一个class 和 这个ResolvableType 对象里面的class 是否有一些特殊关系,比如继承 什么
     */
    public boolean isAssignableFrom(Class<?> other) {
        return isAssignableFrom(forClass(other), null);
    }

    /**
     * 和上面那个方法类似
     */
    public boolean isAssignableFrom(ResolvableType other) {
        return isAssignableFrom(other, null);
    }

    /**
     * 上面的那几个 isAssignableFrom  不管是对比什么类型,都是全部把目标对象转成 ResolvableType,然后统一调用本方法
     */
    private boolean isAssignableFrom(ResolvableType target, @Nullable Map<Type, Type> matchedBefore) {
        Assert.notNull(target, "ResolvableType 对象不能为空");

        if (this == NONE || target == NONE) {
            return false;
        }

        // 如果是数组类型的判断
        if (isArray()) {
            if(!target.isArray()){
                //如果目标对象都不是数组,那肯定不匹配了
                return false;
            }
            //获取了数组里面的类型,递归调用再判断一次2个类型是否相同
            return getComponentType().isAssignableFrom(target.getComponentType());
        }

        //这个场景不多,暂时忽略
        if (matchedBefore != null && matchedBefore.get(this.type) == target.type) {
            return true;
        }

        //  如果要对比的和目标对象 是属于通配符类型,就是 ? extends A 这样的类型,那么走下面的对比逻辑
        if(wildcardIsAssignableFrom(target)){
            return true;
        }

        //下面开始正常类型的对比
        boolean exactMatch = (matchedBefore != null);
        //假设你有泛型
        boolean checkGenerics = true;
        Class<?> ourResolved = null;
        //如果是 泛型 T这样的变量类型
        if (this.type instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) this.type;
            // Try default variable resolution
            if (this.variableResolver != null) {
                ResolvableType resolved = this.variableResolver.resolveVariable(variable);
                if (resolved != null) {
                    ourResolved = resolved.resolve();
                }
            }
            if (ourResolved == null) {
                if (target.variableResolver != null) {
                    ResolvableType resolved = target.variableResolver.resolveVariable(variable);
                    if (resolved != null) {
                        ourResolved = resolved.resolve();
                        checkGenerics = false;
                    }
                }
            }
            if (ourResolved == null) {
                exactMatch = false;
            }
        }
        if (ourResolved == null) {
            ourResolved = resolve(Object.class);
        }
        Class<?> otherResolved = target.resolve(Object.class);

        //这里就真的 把2个要对比的 class 拿出来,看是否有关系,不通过就直接走人,通过了对比就进入下一环节
        if (exactMatch ? !ourResolved.equals(otherResolved) : !ClassUtils.isAssignable(ourResolved, otherResolved)) {
            return false;
        }

        //如果有泛型,泛型的类型也要对比
        if (checkGenerics) {
            // Recursively check each generic
            ResolvableType[] ourGenerics = getGenerics();
            ResolvableType[] typeGenerics = target.as(ourResolved).getGenerics();
            if (ourGenerics.length != typeGenerics.length) {
                return false;
            }
            if (matchedBefore == null) {
                matchedBefore = new IdentityHashMap<>(1);
            }
            matchedBefore.put(this.type, target.type);
            for (int i = 0; i < ourGenerics.length; i++) {
                if (!ourGenerics[i].isAssignableFrom(typeGenerics[i], matchedBefore)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 通配符类型的比较
     * @param target
     * @return
     */
    public boolean wildcardIsAssignableFrom(ResolvableType target){
        WildcardBounds ourBounds = WildcardBounds.get(this);
        WildcardBounds typeBounds = WildcardBounds.get(target);

        //下面2个都是从通配符的角度去对比了
        if (typeBounds != null) {
            return (ourBounds != null && ourBounds.isSameKind(typeBounds) &&
                    ourBounds.isAssignableFrom(typeBounds.getBounds()));
        }

        if (ourBounds != null) {
            return ourBounds.isAssignableFrom(target);
        }
        return false;
    }

    /**
     * 判断这个包装类是否是 数组
     */
    public boolean isArray() {
        if (this == NONE) {
            return false;
        }
        return ((this.type instanceof Class && ((Class<?>) this.type).isArray()) ||
                this.type instanceof GenericArrayType || resolveType().isArray());
    }


    public ResolvableType getComponentType() {
        if (this == NONE) {
            return NONE;
        }
        if (this.componentType != null) {
            return this.componentType;
        }
        if (this.type instanceof Class) {
            Class<?> componentType = ((Class<?>) this.type).getComponentType();
            return forType(componentType, this.variableResolver);
        }
        if (this.type instanceof GenericArrayType) {
            return forType(((GenericArrayType) this.type).getGenericComponentType(), this.variableResolver);
        }
        return resolveType().getComponentType();
    }




    public ResolvableType as(Class<?> type) {
        if (this == NONE) {
            return NONE;
        }
        //如果传入的类型就是 本尊的类型,那么就直接返回
        Class<?> resolved = resolve();
        if (resolved == null || resolved == type) {
            return this;
        }
        //获取本尊的所有接口,每一个接口都递归的方式判断一次直到找到
        for (ResolvableType interfaceType : getInterfaces()) {
            ResolvableType interfaceAsType = interfaceType.as(type);
            if (interfaceAsType != NONE) {
                return interfaceAsType;
            }
        }
        //接口也没有,那么就只能去父类找找了
        return getSuperType().as(type);
    }


    public ResolvableType getSuperType() {
        Class<?> resolved = resolve();
        if (resolved == null || resolved.getGenericSuperclass() == null) {
            return NONE;
        }
        ResolvableType superType = this.superType;
        if (superType == null) {
            superType = forType(SerializableTypeWrapper.forGenericSuperclass(resolved), asVariableResolver());
            this.superType = superType;
        }
        return superType;
    }


    public ResolvableType[] getInterfaces() {
        Class<?> resolved = resolve();
        if (resolved == null || resolved.getGenericInterfaces().length == 0) {
            return EMPTY_TYPES_ARRAY;
        }
        ResolvableType[] interfaces = this.interfaces;
        if (interfaces == null) {
            interfaces = forTypes(SerializableTypeWrapper.forGenericInterfaces(resolved), asVariableResolver());
            this.interfaces = interfaces;
        }
        return interfaces;
    }


    public boolean hasGenerics() {
        return (getGenerics().length > 0);
    }





    /**
     * 获取 泛型
     * @param indexes 这里用可变参数
     *                不传 : 获取第一个泛型
     *                传一个int : 获取指定位置的泛型
     *                传 1 0 1 : 额 用语言已经很难表达 A<B,C<D<E,F>>> 这里获取的是F
     *
     * @return
     */
    public ResolvableType getGeneric(@Nullable int... indexes) {
        ResolvableType[] generics = getGenerics();
        if (indexes == null || indexes.length == 0) {
            return (generics.length == 0 ? NONE : generics[0]);
        }
        ResolvableType generic = this;
        for (int index : indexes) {
            generics = generic.getGenerics();
            if (index < 0 || index >= generics.length) {
                return NONE;
            }
            generic = generics[index];
        }
        return generic;
    }


    public ResolvableType[] getGenerics() {
        if (this == NONE) {
            return EMPTY_TYPES_ARRAY;
        }
        ResolvableType[] generics = this.generics;
        if (generics == null) {
            if (this.type instanceof Class) {
                Class<?> typeClass = (Class<?>) this.type;
                generics = forTypes(SerializableTypeWrapper.forTypeParameters(typeClass), this.variableResolver);
            }
            else if (this.type instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) this.type).getActualTypeArguments();
                generics = new ResolvableType[actualTypeArguments.length];
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    generics[i] = forType(actualTypeArguments[i], this.variableResolver);
                }
            }
            else {
                generics = resolveType().getGenerics();
            }
            this.generics = generics;
        }
        return generics;
    }






    @Nullable
    public Class<?> resolve() {
        return this.resolved;
    }


    public Class<?> resolve(Class<?> fallback) {
        return (this.resolved != null ? this.resolved : fallback);
    }

    @Nullable
    private Class<?> resolveClass() {
        if (this.type == EmptyType.INSTANCE) {
            return null;
        }
        if (this.type instanceof Class) {
            return (Class<?>) this.type;
        }
        if (this.type instanceof GenericArrayType) {
            Class<?> resolvedComponent = getComponentType().resolve();
            return (resolvedComponent != null ? Array.newInstance(resolvedComponent, 0).getClass() : null);
        }
        return resolveType().resolve();
    }

    /**
     * 这里给 Type 做了一下泛型的解析
     *
     * 如果有泛型,会把泛型剔除,返回真实类型,A<B> 这样的会返回 A
     * 通配符类型会返回上下边界的类型  ? extends A 返回 A , ？super B 返回 B
     *
     *
     * 如果没有泛型,会直接返回none
     */
    ResolvableType resolveType() {

        //带了泛型的类型
        if (this.type instanceof ParameterizedType) {
            //这里把泛型给抹去了 A<B,C> 这里调用后只有 A,不管泛型有几层都没了
            Type rawType = ((ParameterizedType) this.type).getRawType();
            return forType(rawType, this.variableResolver);
        }

        //如果是通配符的类型 比如 ? extends A  ,  ？super classB
        if (this.type instanceof WildcardType) {
            //获取上边界类型  ? extends A 这里拿到的是 A
            Type resolved = resolveBounds(((WildcardType) this.type).getUpperBounds());
            if (resolved == null) {
                //没有就获取下边界类型 ？super B  获取的就是 B
                resolved = resolveBounds(((WildcardType) this.type).getLowerBounds());
            }
            return forType(resolved, this.variableResolver);
        }

        //如果是 T 这样的不明确的标识 就走下面
        if (this.type instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) this.type;
            // 尝试用默认的额解析器 处理一下,这里其实有一个扩展点 ,可以手动传入 variableResolver 来对泛型做一定处理
            if (this.variableResolver != null) {
                ResolvableType resolved = this.variableResolver.resolveVariable(variable);
                if (resolved != null) {
                    return resolved;
                }
            }
            // 获取泛型的边界,如果 比如 A<T> 这样的边界是 Object,那么 其实会返回一个 none出去
            return forType(resolveBounds(variable.getBounds()), this.variableResolver);
        }

        //不是泛型类,就直接返回none
        return NONE;
    }

    @Nullable
    private Type resolveBounds(Type[] bounds) {
        if (bounds.length == 0 || bounds[0] == Object.class) {
            return null;
        }
        return bounds[0];
    }

    @Nullable
    private ResolvableType resolveVariable(TypeVariable<?> variable) {
        if (this.type instanceof TypeVariable) {
            return resolveType().resolveVariable(variable);
        }
        if (this.type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) this.type;
            Class<?> resolved = resolve();
            if (resolved == null) {
                return null;
            }
            TypeVariable<?>[] variables = resolved.getTypeParameters();
            for (int i = 0; i < variables.length; i++) {
                if (ObjectUtils.nullSafeEquals(variables[i].getName(), variable.getName())) {
                    Type actualType = parameterizedType.getActualTypeArguments()[i];
                    return forType(actualType, this.variableResolver);
                }
            }
            Type ownerType = parameterizedType.getOwnerType();
            if (ownerType != null) {
                return forType(ownerType, this.variableResolver).resolveVariable(variable);
            }
        }
        if (this.variableResolver != null) {
            return this.variableResolver.resolveVariable(variable);
        }
        return null;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResolvableType)) {
            return false;
        }

        ResolvableType otherType = (ResolvableType) other;
        if (!ObjectUtils.nullSafeEquals(this.type, otherType.type)) {
            return false;
        }
        if (this.typeProvider != otherType.typeProvider &&
                (this.typeProvider == null || otherType.typeProvider == null ||
                        !ObjectUtils.nullSafeEquals(this.typeProvider.getType(), otherType.typeProvider.getType()))) {
            return false;
        }
        if (this.variableResolver != otherType.variableResolver &&
                (this.variableResolver == null || otherType.variableResolver == null ||
                        !ObjectUtils.nullSafeEquals(this.variableResolver.getSource(), otherType.variableResolver.getSource()))) {
            return false;
        }
        if (!ObjectUtils.nullSafeEquals(this.componentType, otherType.componentType)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (this.hash != null ? this.hash : calculateHashCode());
    }

    private int calculateHashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(this.type);
        if (this.typeProvider != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.typeProvider.getType());
        }
        if (this.variableResolver != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.variableResolver.getSource());
        }
        if (this.componentType != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.componentType);
        }
        return hashCode;
    }


    @Nullable
    VariableResolver asVariableResolver() {
        if (this == NONE) {
            return null;
        }
        return new DefaultVariableResolver();
    }


    private Object readResolve() {
        return (this.type == EmptyType.INSTANCE ? NONE : this);
    }


    @Override
    public String toString() {
        if (isArray()) {
            return getComponentType() + "[]";
        }
        if (this.resolved == null) {
            return "?";
        }
        if (this.type instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) this.type;
            if (this.variableResolver == null || this.variableResolver.resolveVariable(variable) == null) {
                // Don't bother with variable boundaries for toString()...
                // Can cause infinite recursions in case of self-references
                return "?";
            }
        }
        StringBuilder result = new StringBuilder(this.resolved.getName());
        if (hasGenerics()) {
            result.append('<');
            result.append(StringUtils.arrayToDelimitedString(getGenerics(), ", "));
            result.append('>');
        }
        return result.toString();
    }



    public static ResolvableType forClass(@Nullable Class<?> clazz) {
        return new ResolvableType(clazz);
    }







    private static ResolvableType[] forTypes(Type[] types, @Nullable VariableResolver owner) {
        ResolvableType[] result = new ResolvableType[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = forType(types[i], owner);
        }
        return result;
    }






    static ResolvableType forType(@Nullable Type type, @Nullable VariableResolver variableResolver) {
        return forType(type, null, variableResolver);
    }


    /**
     * 通过type来 创建 ResolvableType 对象
     * @param type
     * @param typeProvider
     * @param variableResolver
     * @return
     */
    static ResolvableType forType(
            @Nullable Type type, @Nullable TypeProvider typeProvider, @Nullable VariableResolver variableResolver) {

        if (type == null && typeProvider != null) {
            type = SerializableTypeWrapper.forTypeProvider(typeProvider);
        }
        if (type == null) {
            return NONE;
        }

        //是class类型的直接 创建ResolvableType 对象
        if (type instanceof Class) {
            return new ResolvableType(type, typeProvider, variableResolver, (ResolvableType) null);
        }

        // 这个构造器主要是用来生成hash值,然后当做key去获取缓存
        ResolvableType resultType = new ResolvableType(type, typeProvider, variableResolver);
        ResolvableType cachedType = cache.get(resultType);
        if (cachedType == null) {
            //缓存里真的没有,就直接 创造一个新的
            cachedType = new ResolvableType(type, typeProvider, variableResolver, resultType.hash);
            //放入缓存
            cache.put(cachedType, cachedType);
        }
        resultType.resolved = cachedType.resolved;
        return resultType;
    }


    public static void clearCache() {
        cache.clear();
        SerializableTypeWrapper.cache.clear();
    }



    interface VariableResolver extends Serializable {


        Object getSource();


        @Nullable
        ResolvableType resolveVariable(TypeVariable<?> variable);
    }


    @SuppressWarnings("serial")
    private class DefaultVariableResolver implements VariableResolver {

        @Override
        @Nullable
        public ResolvableType resolveVariable(TypeVariable<?> variable) {
            return ResolvableType.this.resolveVariable(variable);
        }

        @Override
        public Object getSource() {
            return ResolvableType.this;
        }
    }








    private static class WildcardBounds {

        private final Kind kind;

        private final ResolvableType[] bounds;


        public WildcardBounds(Kind kind, ResolvableType[] bounds) {
            this.kind = kind;
            this.bounds = bounds;
        }


        public boolean isSameKind(WildcardBounds bounds) {
            return this.kind == bounds.kind;
        }


        public boolean isAssignableFrom(ResolvableType... types) {
            for (ResolvableType bound : this.bounds) {
                for (ResolvableType type : types) {
                    if (!isAssignable(bound, type)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean isAssignable(ResolvableType source, ResolvableType from) {
            return (this.kind == Kind.UPPER ? source.isAssignableFrom(from) : from.isAssignableFrom(source));
        }

        /**
         * Return the underlying bounds.
         */
        public ResolvableType[] getBounds() {
            return this.bounds;
        }

        /**
         * 获取通配符的类型,没有就返回Null
         */
        @Nullable
        public static WildcardBounds get(ResolvableType type) {
            ResolvableType resolveToWildcard = type;

            //这里会一层层去找,直到找到 有通配符的地方,比如 A< B< ? extendes C > > 第一次while 拿到B<...> 第二次才拿到 ? extendes C
            //如果没有通配符的泛型,直接返回null了
            while (!(resolveToWildcard.getType() instanceof WildcardType)) {
                if (resolveToWildcard == NONE) {
                    return null;
                }
                resolveToWildcard = resolveToWildcard.resolveType();
            }

            //下面就是把 原生的 WildcardType 类型给封装到 自定义的 WildcardBounds 中返回出去
            WildcardType wildcardType = (WildcardType) resolveToWildcard.type;
            Kind boundsType = (wildcardType.getLowerBounds().length > 0 ? Kind.LOWER : Kind.UPPER);
            Type[] bounds = (boundsType == Kind.UPPER ? wildcardType.getUpperBounds() : wildcardType.getLowerBounds());
            ResolvableType[] resolvableBounds = new ResolvableType[bounds.length];
            for (int i = 0; i < bounds.length; i++) {
                resolvableBounds[i] = ResolvableType.forType(bounds[i], type.variableResolver);
            }
            return new WildcardBounds(boundsType, resolvableBounds);
        }

        /**
         * The various kinds of bounds.
         */
        enum Kind {UPPER, LOWER}
    }


    /**
     * Internal {@link Type} used to represent an empty value.
     */
    @SuppressWarnings("serial")
    static class EmptyType implements Type, Serializable {

        static final Type INSTANCE = new EmptyType();

        Object readResolve() {
            return INSTANCE;
        }
    }

}
