package com.chy.summer.framework.core;


import com.chy.summer.framework.util.ObjectUtils;
import com.chy.summer.framework.util.ReflectionUtils;
import javax.annotation.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


abstract class SerializableTypeWrapper {

    private static final Class<?>[] SUPPORTED_SERIALIZABLE_TYPES = {
            GenericArrayType.class, ParameterizedType.class, TypeVariable.class, WildcardType.class};

    static final Map<Type, Type> cache = new ConcurrentHashMap<>(256);



    @Nullable
    public static Type forField(Field field) {
        return forTypeProvider(new FieldTypeProvider(field));
    }


    @Nullable
    public static Type forMethodParameter(MethodParameter methodParameter) {
        return forTypeProvider(new MethodParameterTypeProvider(methodParameter));
    }

    /**
     * Return a {@link Serializable} variant of {@link Class#getGenericSuperclass()}.
     */
    @SuppressWarnings("serial")
    @Nullable
    public static Type forGenericSuperclass(final Class<?> type) {
        return forTypeProvider(type::getGenericSuperclass);
    }

    /**
     * Return a {@link Serializable} variant of {@link Class#getGenericInterfaces()}.
     */
    @SuppressWarnings("serial")
    public static Type[] forGenericInterfaces(final Class<?> type) {
        Type[] result = new Type[type.getGenericInterfaces().length];
        for (int i = 0; i < result.length; i++) {
            final int index = i;
            result[i] = forTypeProvider(() -> type.getGenericInterfaces()[index]);
        }
        return result;
    }

    /**
     * Return a {@link Serializable} variant of {@link Class#getTypeParameters()}.
     */
    @SuppressWarnings("serial")
    public static Type[] forTypeParameters(final Class<?> type) {
        Type[] result = new Type[type.getTypeParameters().length];
        for (int i = 0; i < result.length; i++) {
            final int index = i;
            result[i] = forTypeProvider(() -> type.getTypeParameters()[index]);
        }
        return result;
    }

    /**
     * Unwrap the given type, effectively returning the original non-serializable type.
     * @param type the type to unwrap
     * @return the original non-serializable type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Type> T unwrap(T type) {
        Type unwrapped = type;
        while (unwrapped instanceof SerializableTypeProxy) {
            unwrapped = ((SerializableTypeProxy) type).getTypeProvider().getType();
        }
        return (unwrapped != null ? (T) unwrapped : type);
    }

    /**
     * Return a {@link Serializable} {@link Type} backed by a {@link TypeProvider} .
     */
    @Nullable
    static Type forTypeProvider(TypeProvider provider) {
        Type providedType = provider.getType();
        if (providedType == null || providedType instanceof Serializable) {
            // No serializable type wrapping necessary (e.g. for java.lang.Class)
            return providedType;
        }

        // Obtain a serializable type proxy for the given provider...
        Type cached = cache.get(providedType);
        if (cached != null) {
            return cached;
        }
        for (Class<?> type : SUPPORTED_SERIALIZABLE_TYPES) {
            if (type.isInstance(providedType)) {
                ClassLoader classLoader = provider.getClass().getClassLoader();
                Class<?>[] interfaces = new Class<?>[] {type, SerializableTypeProxy.class, Serializable.class};
                InvocationHandler handler = new TypeProxyInvocationHandler(provider);
                cached = (Type) Proxy.newProxyInstance(classLoader, interfaces, handler);
                cache.put(providedType, cached);
                return cached;
            }
        }
        throw new IllegalArgumentException("Unsupported Type class: " + providedType.getClass().getName());
    }


    /**
     * Additional interface implemented by the type proxy.
     */
    interface SerializableTypeProxy {

        /**
         * Return the underlying type provider.
         */
        TypeProvider getTypeProvider();
    }


    /**
     * A {@link Serializable} interface providing access to a {@link Type}.
     */
    @SuppressWarnings("serial")
    interface TypeProvider extends Serializable {

        /**
         * Return the (possibly non {@link Serializable}) {@link Type}.
         */
        @Nullable
        Type getType();

        /**
         * Return the source of the type, or {@code null} if not known.
         * <p>The default implementations returns {@code null}.
         */
        @Nullable
        default Object getSource() {
            return null;
        }
    }


    /**
     * {@link Serializable} {@link InvocationHandler} used by the proxied {@link Type}.
     * Provides serialization support and enhances any methods that return {@code Type}
     * or {@code Type[]}.
     */
    @SuppressWarnings("serial")
    private static class TypeProxyInvocationHandler implements InvocationHandler, Serializable {

        private final TypeProvider provider;

        public TypeProxyInvocationHandler(TypeProvider provider) {
            this.provider = provider;
        }

        @Override
        @Nullable
        public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
            if (method.getName().equals("equals") && args != null) {
                Object other = args[0];
                // Unwrap proxies for speed
                if (other instanceof Type) {
                    other = unwrap((Type) other);
                }
                return ObjectUtils.nullSafeEquals(this.provider.getType(), other);
            }
            else if (method.getName().equals("hashCode")) {
                return ObjectUtils.nullSafeHashCode(this.provider.getType());
            }
            else if (method.getName().equals("getTypeProvider")) {
                return this.provider;
            }

            if (Type.class == method.getReturnType() && args == null) {
                return forTypeProvider(new MethodInvokeTypeProvider(this.provider, method, -1));
            }
            else if (Type[].class == method.getReturnType() && args == null) {
                Type[] result = new Type[((Type[]) method.invoke(this.provider.getType())).length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = forTypeProvider(new MethodInvokeTypeProvider(this.provider, method, i));
                }
                return result;
            }

            try {
                return method.invoke(this.provider.getType(), args);
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }


    /**
     * {@link TypeProvider} for {@link Type}s obtained from a {@link Field}.
     */
    @SuppressWarnings("serial")
    static class FieldTypeProvider implements TypeProvider {

        private final String fieldName;

        private final Class<?> declaringClass;

        private transient Field field;

        public FieldTypeProvider(Field field) {
            this.fieldName = field.getName();
            this.declaringClass = field.getDeclaringClass();
            this.field = field;
        }

        @Override
        public Type getType() {
            return this.field.getGenericType();
        }

        @Override
        public Object getSource() {
            return this.field;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();
            try {
                this.field = this.declaringClass.getDeclaredField(this.fieldName);
            }
            catch (Throwable ex) {
                throw new IllegalStateException("Could not find original class structure", ex);
            }
        }
    }


    /**
     * {@link TypeProvider} for {@link Type}s obtained from a {@link MethodParameter}.
     */
    @SuppressWarnings("serial")
    static class MethodParameterTypeProvider implements TypeProvider {

        @Nullable
        private final String methodName;

        private final Class<?>[] parameterTypes;

        private final Class<?> declaringClass;

        private final int parameterIndex;

        private transient MethodParameter methodParameter;

        public MethodParameterTypeProvider(MethodParameter methodParameter) {
            this.methodName = (methodParameter.getMethod() != null ? methodParameter.getMethod().getName() : null);
            this.parameterTypes = methodParameter.getExecutable().getParameterTypes();
            this.declaringClass = methodParameter.getDeclaringClass();
            this.parameterIndex = methodParameter.getParameterIndex();
            this.methodParameter = methodParameter;
        }

        @Override
        public Type getType() {
            return this.methodParameter.getGenericParameterType();
        }

        @Override
        public Object getSource() {
            return this.methodParameter;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();
            try {
                if (this.methodName != null) {
                    this.methodParameter = new MethodParameter(
                            this.declaringClass.getDeclaredMethod(this.methodName, this.parameterTypes), this.parameterIndex);
                }
                else {
                    this.methodParameter = new MethodParameter(
                            this.declaringClass.getDeclaredConstructor(this.parameterTypes), this.parameterIndex);
                }
            }
            catch (Throwable ex) {
                throw new IllegalStateException("Could not find original class structure", ex);
            }
        }
    }


    /**
     * {@link TypeProvider} for {@link Type}s obtained by invoking a no-arg method.
     */
    @SuppressWarnings("serial")
    static class MethodInvokeTypeProvider implements TypeProvider {

        private final TypeProvider provider;

        private final String methodName;

        private final Class<?> declaringClass;

        private final int index;

        private transient Method method;

        @Nullable
        private transient volatile Object result;

        public MethodInvokeTypeProvider(TypeProvider provider, Method method, int index) {
            this.provider = provider;
            this.methodName = method.getName();
            this.declaringClass = method.getDeclaringClass();
            this.index = index;
            this.method = method;
        }

        @Override
        @Nullable
        public Type getType() {
            Object result = this.result;
            if (result == null) {
                // Lazy invocation of the target method on the provided type
                result = ReflectionUtils.invokeMethod(this.method, this.provider.getType());
                // Cache the result for further calls to getType()
                this.result = result;
            }
            return (result instanceof Type[] ? ((Type[]) result)[this.index] : (Type) result);
        }

        @Override
        @Nullable
        public Object getSource() {
            return null;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();
            Method method = ReflectionUtils.findMethod(this.declaringClass, this.methodName);
            if (method == null) {
                throw new IllegalStateException("Cannot find method on deserialization: " + this.methodName);
            }
            if (method.getReturnType() != Type.class && method.getReturnType() != Type[].class) {
                throw new IllegalStateException(
                        "Invalid return type on deserialized method - needs to be Type or Type[]: " + method);
            }
            this.method = method;
        }
    }

}

