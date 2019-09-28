//package com.chy.summer.framework.aop.framework;
//
//public class ProxyCreatorSupport extends AdvisedSupport {
//
//	private AopProxyFactory aopProxyFactory;
//
//	private List<AdvisedSupportListener> listeners = new LinkedList<>();
//
//	/** Set to true when the first AOP proxy has been created */
//	private boolean active = false;
//
//
//	/**
//	 * Create a new ProxyCreatorSupport instance.
//	 */
//	public ProxyCreatorSupport() {
//		this.aopProxyFactory = new DefaultAopProxyFactory();
//	}
//
//	/**
//	 * Create a new ProxyCreatorSupport instance.
//	 * @param aopProxyFactory the AopProxyFactory to use
//	 */
//	public ProxyCreatorSupport(AopProxyFactory aopProxyFactory) {
//		Assert.notNull(aopProxyFactory, "AopProxyFactory must not be null");
//		this.aopProxyFactory = aopProxyFactory;
//	}
//
//
//	/**
//	 * Customize the AopProxyFactory, allowing different strategies
//	 * to be dropped in without changing the core framework.
//	 * <p>Default is {@link DefaultAopProxyFactory}, using dynamic JDK
//	 * proxies or CGLIB proxies based on the requirements.
//	 */
//	public void setAopProxyFactory(AopProxyFactory aopProxyFactory) {
//		Assert.notNull(aopProxyFactory, "AopProxyFactory must not be null");
//		this.aopProxyFactory = aopProxyFactory;
//	}
//
//	/**
//	 * Return the AopProxyFactory that this ProxyConfig uses.
//	 */
//	public AopProxyFactory getAopProxyFactory() {
//		return this.aopProxyFactory;
//	}
//
//	/**
//	 * Add the given AdvisedSupportListener to this proxy configuration.
//	 * @param listener the listener to register
//	 */
//	public void addListener(AdvisedSupportListener listener) {
//		Assert.notNull(listener, "AdvisedSupportListener must not be null");
//		this.listeners.add(listener);
//	}
//
//	/**
//	 * Remove the given AdvisedSupportListener from this proxy configuration.
//	 * @param listener the listener to deregister
//	 */
//	public void removeListener(AdvisedSupportListener listener) {
//		Assert.notNull(listener, "AdvisedSupportListener must not be null");
//		this.listeners.remove(listener);
//	}
//
//
//	/**
//	 * Subclasses should call this to get a new AOP proxy. They should <b>not</b>
//	 * create an AOP proxy with {@code this} as an argument.
//	 */
//	protected final synchronized AopProxy createAopProxy() {
//		if (!this.active) {
//			activate();
//		}
//		return getAopProxyFactory().createAopProxy(this);
//	}
//
//	/**
//	 * Activate this proxy configuration.
//	 * @see AdvisedSupportListener#activated
//	 */
//	private void activate() {
//		this.active = true;
//		for (AdvisedSupportListener listener : this.listeners) {
//			listener.activated(this);
//		}
//	}
//
//	/**
//	 * Propagate advice change event to all AdvisedSupportListeners.
//	 * @see AdvisedSupportListener#adviceChanged
//	 */
//	@Override
//	protected void adviceChanged() {
//		super.adviceChanged();
//		synchronized (this) {
//			if (this.active) {
//				for (AdvisedSupportListener listener : this.listeners) {
//					listener.adviceChanged(this);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Subclasses can call this to check whether any AOP proxies have been created yet.
//	 */
//	protected final synchronized boolean isActive() {
//		return this.active;
//	}
//
//}