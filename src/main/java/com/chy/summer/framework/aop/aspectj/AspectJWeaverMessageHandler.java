package com.chy.summer.framework.aop.aspectj;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessage.Kind;

/**
 * AspectJ的IMessageHandler接口的实现，
 * 该接口通过与常规消息相同的日志记录系统路来编织AspectJ消息。
 */
public class AspectJWeaverMessageHandler implements IMessageHandler {
	//TODO GYX 没有搞懂什么意思 将aspectJ的日志接入？
	private static final String AJ_ID = "[AspectJ] ";

//	private static final Log logger = LogFactory.getLog("AspectJ Weaver");


	@Override
	public boolean handleMessage(IMessage message) throws AbortException {
		Kind messageKind = message.getKind();
//		if (messageKind == IMessage.DEBUG) {
//			if (logger.isDebugEnabled()) {
//				logger.debug(makeMessageFor(message));
//				return true;
//			}
//		}
//		else if (messageKind == IMessage.INFO || messageKind == IMessage.WEAVEINFO) {
//			if (logger.isInfoEnabled()) {
//				logger.info(makeMessageFor(message));
//				return true;
//			}
//		}
//		else if (messageKind == IMessage.WARNING) {
//			if (logger.isWarnEnabled()) {
//				logger.warn(makeMessageFor(message));
//				return true;
//			}
//		}
//		else if (messageKind == IMessage.ERROR) {
//			if (logger.isErrorEnabled()) {
//				logger.error(makeMessageFor(message));
//				return true;
//			}
//		}
//		else if (messageKind == IMessage.ABORT) {
//			if (logger.isFatalEnabled()) {
//				logger.fatal(makeMessageFor(message));
//				return true;
//			}
//		}
		return false;
	}

	private String makeMessageFor(IMessage aMessage) {
		return AJ_ID + aMessage.getMessage();
	}

	@Override
	public boolean isIgnoring(Kind messageKind) {
		// We want to see everything, and allow configuration of log levels dynamically.
		return false;
	}

	@Override
	public void dontIgnore(Kind messageKind) {
		// We weren't ignoring anything anyway...
	}

	@Override
	public void ignore(Kind kind) {
		// We weren't ignoring anything anyway...
	}

}