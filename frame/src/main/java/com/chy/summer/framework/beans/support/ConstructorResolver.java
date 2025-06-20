package com.chy.summer.framework.beans.support;

import com.chy.summer.framework.beans.BeanWrapper;
import com.chy.summer.framework.beans.BeanWrapperImpl;
import com.chy.summer.framework.beans.factory.ConstructorArgumentValues;
import com.chy.summer.framework.beans.factory.DependencyDescriptor;
import com.chy.summer.framework.beans.factory.InjectionPoint;
import com.chy.summer.framework.context.annotation.constant.Autowire;
import com.chy.summer.framework.core.MethodParameter;
import com.chy.summer.framework.core.ParameterNameDiscoverer;
import com.chy.summer.framework.exception.BeanCreationException;
import com.chy.summer.framework.exception.BeanDefinitionStoreException;
import com.chy.summer.framework.exception.BeansException;
import com.chy.summer.framework.util.AutowireUtils;
import com.chy.summer.framework.util.ClassUtils;
import com.chy.summer.framework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * 用于解析工厂方法,以及构造器方法的
 */
public class ConstructorResolver {


    private final AbstractAutowireCapableBeanFactory beanFactory;


    public ConstructorResolver(AbstractAutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 使用工厂方法来 创建对象 也就是 @Bean 注解标识的方法来创建
     *
     * @param beanName
     * @param mbd
     * @param explicitArgs
     * @return
     */
    public BeanWrapper instantiateUsingFactoryMethod(
            String beanName, RootBeanDefinition mbd, Object[] explicitArgs) {

        BeanWrapperImpl bw = new BeanWrapperImpl();
        //初始化一下 BeanWrapperImpl 这里暂时什么都没有做
        this.beanFactory.initBeanWrapper(bw);

        Object factoryBean = null;
        Class<?> factoryClass = null;
        boolean isStatic = true;

        //这里拿到的是 工厂bean的name, 比如
        // @Configuration
        // A{
        //      @Bean
        //      public B b();
        // }
        // 这里拿到的是 a 的名字, 而 beanName 拿到的是 b 的名字
        String factoryBeanName = mbd.getFactoryBeanName();
        if (factoryBeanName != null) {
            if (factoryBeanName.equals(beanName)) {
                throw new BeanDefinitionStoreException("工厂bean [%s]  不能 和 工厂中生产的bean同名", beanName);
            }

            //如果是单例对象,并且 beanName 已经被占用,则抛出异常
            if (mbd.isSingleton() && this.beanFactory.containsSingleton(beanName)) {
                throw new BeanDefinitionStoreException("beanName [%s] 已经被注册了 ", beanName);
            }

            //先把 工厂方法 的 持有者 的实例对象给拿出来
            factoryBean = this.beanFactory.getBean(factoryBeanName);
            factoryClass = factoryBean.getClass();
            isStatic = false;
        } else {
            //TODO 静态 工厂方法 暂时不考虑
            //也就是 @Bean 标注的是一个 static 方法
        }

        Method factoryMethodToUse = null;
        ArgumentsHolder argsHolderToUse = null;
        Object[] argsToUse = null;

        //TODO 然还有一个预先参数的解析,这个暂时不需要

        //如果是代理对象 剥除代理的壳子,拿到真实的对象类型
        factoryClass = ClassUtils.getUserClass(factoryClass);

        //获取到 factoryBean 里面所有的方法
        Method[] rawCandidates = getCandidateMethods(factoryClass, mbd);
        //存放的是在一个 factoryBean里面 和 beanName 相同的方法, 因为在一个类里面会有方法重名,所以可能会找到多个 Method 存进去
        List<Method> candidateList = new ArrayList<>();

        for (Method candidate : rawCandidates) {
            //遍历所有的方法,如果有方法是
            if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate)) {
                candidateList.add(candidate);
            }
        }

        //list 转成数组
        Method[] candidates = candidateList.toArray(new Method[0]);

        //工厂方法排序 排序顺序是 public优先  参数多的优先
        AutowireUtils.sortFactoryMethods(candidates);

        int minNrOfArgs = 0;
        if (mbd.hasConstructorArgumentValues()) {
            //TODO 如果有工厂方法有参数走这里面
        }

        RuntimeException exception = null;
        for (Method candidate : candidates) {
            Class<?>[] paramTypes = candidate.getParameterTypes();
            //有参数的情况下,去解析参数
            if (paramTypes.length >= minNrOfArgs) {
                ArgumentsHolder argsHolder;
                String[] paramNames = null;
                //去拿参数解析器
                ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
                if (pnd != null) {
                    //拿到这个方法里 的所有参数,并且转成 数组
                    paramNames = pnd.getParameterNames(candidate);
                }

                // 去ioc容器里拿到参数的对象 并且把他们全部封装到一个对象里
                //在spring中这里会去通过参数去计算偏差值来决定使用哪一个重载的方法,而这里简化这个操作,重载的优先级是
                //参数多的优先 , 如果参数多的有属性不能在ioc找到就 处理参数第二多的,依次类推
                try {
                    argsHolder = createArgumentArray(beanName, paramTypes, paramNames, candidate);
                    factoryMethodToUse = candidate;
                    argsToUse = argsHolder.arguments;
                    //成功找到了所有的参数,那么就直接选择他了,跳出循环
                    break;
                } catch (BeansException e) {
                    //这里如果异常了,说明参数在ioc中不存在,如果还有其他方法的重载就继续处理
                    exception = e;
                } catch (ClassNotFoundException e) {
                    exception = new RuntimeException(e);
                }
            }
        }

        //没有找到到底要用哪一个方法,这边直接抛出异常了
        //在spring里还给了一大段可能的原因,这边先忽略
        if (factoryMethodToUse == null) {
            //如果有异常记录就直接返回异常
            if (exception != null) {
                throw exception;
            }
            throw new BeanCreationException("在类 [%s] 中没有找到对应的 工厂方法 [%s]", factoryClass.getName(), mbd.getFactoryMethodName());

        } else if (void.class == factoryMethodToUse.getReturnType()) {
            throw new BeanCreationException("工厂方法 [%s] 的返回值不能 为 void", factoryMethodToUse.getName());
        }

        try {
            //使用 反射区执行对应的工厂方法
            Object beanInstance = this.beanFactory.getInstantiationStrategy()
                    .instantiate(mbd, beanName, this.beanFactory, factoryBean, factoryMethodToUse, argsToUse);
            bw.setBeanInstance(beanInstance);
            return bw;
        } catch (Throwable ex) {
            throw new BeanCreationException("使用 工厂方法生成实例 [%s] 失败 失败原因 : [%s]", beanName, ex.getMessage());
        }
    }



    /**
     * 使用构造器去实例化对象
     *
     * @param beanName
     * @param mbd
     * @param ctors
     * @param explicitArgs
     * @return
     */
    public BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mbd, Constructor<?>[] ctors, Object[] explicitArgs) {

        BeanWrapperImpl beanWrapper = new BeanWrapperImpl();
        Constructor<?> constructorToUse = null;
        ArgumentsHolder argsHolderToUse = null;
        Object[] argsToUse = null;

        if (explicitArgs != null) {
            argsToUse = explicitArgs;
        }

        ConstructorArgumentValues resolvedValues = new ConstructorArgumentValues();

        //排序走一波 也是参数多的放前面
        AutowireUtils.sortFactoryMethods(ctors);

        RuntimeException exception = null;

        for (Constructor<?> candidate : ctors) {
            Class<?>[] paramTypes = candidate.getParameterTypes();
            String paramNames[] = null;
            ArgumentsHolder argsHolder;
            if (resolvedValues != null) {
                try {
                    //处理构造器器里面的参数,并且去Ioc 容器去拿对应参数,如果拿不到就去执行其他参数的构造器
                    //如果所有的构造器都拿不到,那么就抛出异常
                    argsHolder = createArgumentArray(beanName, paramTypes, paramNames, getUserDeclaredConstructor(candidate));
                    constructorToUse = candidate;
                    argsHolderToUse = argsHolder;
                    argsToUse = argsHolderToUse.arguments;
                    //如果没抛出异常就算成功找到了,结束循环
                    break;
                } catch (BeansException | ClassNotFoundException e) {
                    exception = new RuntimeException(e);
                    //这里如果异常了,说明参数在ioc中不存在,如果还有其他方法的重载就继续处理
                    continue;
                }

            }

        }


        //没有找到到底要用哪一个方法,这边直接抛出异常了
        //在spring里还给了一大段可能的原因,这边先忽略
        if (constructorToUse == null) {
            //如果有异常记录就直接返回异常
            if (exception != null) {
                throw exception;
            }
            throw new BeanCreationException("在类 [%s] 中没有找到合适的构造方法 ", beanName);
        }


        if (explicitArgs == null) {
            argsHolderToUse.storeCache(mbd, constructorToUse);
        }

        //下面就是开始 实例化 bean 对象了
        try {
            final InstantiationStrategy strategy = beanFactory.getInstantiationStrategy();
            //使用反射用构造器 生成了对象
            Object beanInstance = strategy.instantiate(mbd, beanName, this.beanFactory, constructorToUse, argsToUse);

            beanWrapper.setBeanInstance(beanInstance);
            return beanWrapper;
        }
        catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "使用构造器实例化bean 失败", ex);
        }

    }


    /**
     * 把 工厂方法上的参数 给全部封装到 ArgumentsHolder 对象里
     * 同时还会更具方法的名字去 ioc 容器里获取对应的对象
     *
     * @param beanName
     * @param paramTypes
     * @param paramNames
     * @param executable
     * @return
     */
    private ArgumentsHolder createArgumentArray(String beanName, Class<?>[] paramTypes,
                                                String[] paramNames, Executable executable) throws ClassNotFoundException {


        ArgumentsHolder args = new ArgumentsHolder(paramTypes.length);
        Set<String> autowiredBeanNames = new LinkedHashSet<>(4);

        for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
            //拿到参数的类型
            Class<?> paramType = paramTypes[paramIndex];
            //用参数的类型去换取参数的 name
            String paramName = (paramNames != null ? paramNames[paramIndex] : "");
            // Try to find matching constructor argument value, either indexed or generic.
            ConstructorArgumentValues.ValueHolder valueHolder = null;
            //把 要执行的工厂方法/构造器 封装到 MethodParameter 对象里面
            MethodParameter methodParam = MethodParameter.forExecutable(executable, paramIndex);

            //去ioc 容器里把依赖的 参数对象给拉出来
            Object autowiredArgument = resolveAutowiredArgument(methodParam, beanName, autowiredBeanNames);
            args.rawArguments[paramIndex] = autowiredArgument;
            args.arguments[paramIndex] = autowiredArgument;
            args.resolveNecessary = true;

        }

        //把依赖关系给存入进去
        for (String autowiredBeanName : autowiredBeanNames) {
            this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
        }

        return args;
    }


    protected Object resolveAutowiredArgument(MethodParameter param, String beanName,
                                              Set<String> autowiredBeanNames) {

        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(param, true);
        return this.beanFactory.resolveDependency(dependencyDescriptor, beanName, autowiredBeanNames);
    }


    private Method[] getCandidateMethods(Class<?> factoryClass, RootBeanDefinition mbd) {
        //反射拿所有的方法
        return ReflectionUtils.getAllDeclaredMethods(factoryClass);
    }




    protected Constructor<?> getUserDeclaredConstructor(Constructor<?> constructor) {
        Class<?> declaringClass = constructor.getDeclaringClass();
        Class<?> userClass = ClassUtils.getUserClass(declaringClass);
        if (userClass != declaringClass) {
            try {
                return userClass.getDeclaredConstructor(constructor.getParameterTypes());
            } catch (NoSuchMethodException ex) {
            }
        }
        return constructor;
    }


    private static class ArgumentsHolder {

        public final Object[] rawArguments;

        public final Object[] arguments;

        public final Object[] preparedArguments;

        public boolean resolveNecessary = false;

        public ArgumentsHolder(int size) {
            this.rawArguments = new Object[size];
            this.arguments = new Object[size];
            this.preparedArguments = new Object[size];
        }

        public ArgumentsHolder(Object[] args) {
            this.rawArguments = args;
            this.arguments = args;
            this.preparedArguments = args;
        }

        public void storeCache(RootBeanDefinition mbd, Executable constructorOrFactoryMethod) {
            synchronized (mbd.constructorArgumentLock) {
                mbd.resolvedConstructorOrFactoryMethod = constructorOrFactoryMethod;
                mbd.constructorArgumentsResolved = true;
                if (this.resolveNecessary) {
                    mbd.preparedConstructorArguments = this.preparedArguments;
                } else {
                    mbd.resolvedConstructorArguments = this.arguments;
                }
            }
        }
    }


}
