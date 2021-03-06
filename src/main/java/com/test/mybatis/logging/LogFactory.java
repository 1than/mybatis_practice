package com.test.mybatis.logging;

import java.lang.reflect.Constructor;



public final class LogFactory {
	
	/**
	   * Marker to be used by logging implementations that support markers
	   */
	  public static final String MARKER = "MYBATIS";

	  private static Constructor<? extends Log> logConstructor;

	  static {
	    tryImplementation(new Runnable() {
	      @Override
	      public void run() {
	        useSlf4jLogging();
	      }
	    });
	    tryImplementation(new Runnable() {
	      @Override
	      public void run() {
	        useCommonsLogging();
	      }
	    });
	    tryImplementation(new Runnable() {
	      @Override
	      public void run() {
	        useLog4J2Logging();
	      }
	    });
	    tryImplementation(new Runnable() {
	      @Override
	      public void run() {
	        useLog4JLogging();
	      }
	    });
	    tryImplementation(new Runnable() {
	      @Override
	      public void run() {
	        useJdkLogging();
	      }
	    });
	    tryImplementation(new Runnable() {
	      @Override
	      public void run() {
	        useNoLogging();
	      }
	    });
	  }

	  private LogFactory() {
	    // disable construction
	  }

	  public static Log getLog(Class<?> aClass) {
	    return getLog(aClass.getName());
	  }

	  public static Log getLog(String logger) {
	    try {
	      return logConstructor.newInstance(new Object[] { logger });
	    } catch (Throwable t) {
	      throw new LogException("Error creating logger for logger " + logger + ".  Cause: " + t, t);
	    }
	  }

	  public static synchronized void useCustomLogging(Class<? extends Log> clazz) {
	    setImplementation(clazz);
	  }

	  public static synchronized void useSlf4jLogging() {
	    setImplementation(com.test.mybatis.logging.slf4j.Slf4jImpl.class);
	  }

	  public static synchronized void useCommonsLogging() {
	    setImplementation(com.test.mybatis.logging.commons.JakartaCommonsLoggingImpl.class);
	  }

	  public static synchronized void useLog4JLogging() {
	    setImplementation(com.test.mybatis.logging.log4j.Log4jImpl.class);
	  }

	  public static synchronized void useLog4J2Logging() {
	    setImplementation(com.test.mybatis.logging.log4j2.Log4j2Impl.class);
	  }

	  public static synchronized void useJdkLogging() {
	    setImplementation(com.test.mybatis.logging.jdk14.Jdk14LoggingImpl.class);
	  }

	  public static synchronized void useStdOutLogging() {
	    setImplementation(com.test.mybatis.logging.stdout.StdOutImpl.class);
	  }

	  public static synchronized void useNoLogging() {
	    setImplementation(com.test.mybatis.logging.nologging.NoLoggingImpl.class);
	  }

	  private static void tryImplementation(Runnable runnable) {
	    if (logConstructor == null) {
	      try {
	        runnable.run();
	      } catch (Throwable t) {
	        // ignore
	      }
	    }
	  }

	  private static void setImplementation(Class<? extends Log> implClass) {
	    try {
	      Constructor<? extends Log> candidate = implClass.getConstructor(new Class[] { String.class });
	      Log log = candidate.newInstance(new Object[] { LogFactory.class.getName() });
	      if (log.isDebugEnabled()) {
	        log.debug("Logging initialized using '" + implClass + "' adapter.");
	      }
	      logConstructor = candidate;
	    } catch (Throwable t) {
	      throw new LogException("Error setting Log implementation.  Cause: " + t, t);
	    }
	  }
	  
	  
}
