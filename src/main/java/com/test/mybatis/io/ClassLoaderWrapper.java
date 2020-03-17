package com.test.mybatis.io;

import java.io.InputStream;
import java.net.URL;

/**
 * 
 * 把多个classLoader封装在一个classLoaderWrapper里，就像操作一个classLoader一样
 * 
 * 
 * 获取ClassLoader 三种方式
 * 1.ClassLoader.getSystemClassLoader();              使用系统ClassLoader，即系统的入口点所使用的ClassLoader。
 * 2.this.getClass().getClassLoader();                使用当前类的ClassLoader
 * 3.Thread.currentThread().getContextClassLoader();  使用当前线程的ClassLoader
 * 
 * @author ethan
 *
 */
public class ClassLoaderWrapper {
	
	ClassLoader defaultClassLoader;
	
	ClassLoader systemClassLoader;
	
	ClassLoaderWrapper() {
		try {
			//java中的类加载器 bootstrapClassLoader -> extentionClassLoader -> applicationClassLoader
			//返回ApplicationClassLoader,其只加载classpath下的class文件。
			//一般javaSE项目的classpath为bin/目录，因此只要编译后的class文件在classpath下就可以。此时ApplicationClassLoader就可以加载动态生成的类。
			//但在javaEE环境下，我们的项目里的类是通过WebAppClassLoader类来加载的，此时我们获取了ApplicationClassLoader，因此自然找不到class文件。
			systemClassLoader = ClassLoader.getSystemClassLoader(); 
		} catch (SecurityException ignored) {
			
		}
	}
	
	/**
	 * 从当前的类路径中获取一个resource作为URL
	 * 
	 * @param resource 资源路径，一般是一个地址
	 * @return resource or null
	 */
	public URL getResourceAsURL(String resource) {
		return getResourceAsURL(resource, getClassLoaders(null));
	}
	
	/**
	 * 根据指定的类加载器，从类路径中获取一个资源
	 * 
	 * 
	 * @param resource    资源路径，一般是一个路径
	 * @param classLoader 尝试从类加载器数组中获取一个类加载器
	 * @return the stream or null
	 */
	public URL getResourceAsURL(String resource, ClassLoader classLoader) {
		return getResourceAsURL(resource, getClassLoaders(classLoader));
	}

	/**
	 * 从类路径中获取一个资源
	 * 
	 * @param resource 资源路径
	 * @return   resource or null
	 */
	public InputStream getResourceAsStream(String resource) {
		return getResourceAsStream(resource, getClassLoaders(null));
	}
	
	/**
	 * 用指定的类加载器，从类路径下获取一个资源
	 * 
	 * @param resource     资源路径
	 * @param classLoader  从类加载器数组中获取一个加载器
	 * @return             the stream or null
	 */
	public InputStream getResourceAsStream(String resource, ClassLoader classLoader) {
		return getResourceAsStream(resource, getClassLoaders(classLoader));
	}
	
	/**
	 * 从类路径中获取一个类
	 * 
	 * @param name  寻找的目标类
	 * @return      class
	 * @throws ClassNotFoundException
	 */
	public Class<?> classForName(String name) throws ClassNotFoundException {
		return classForName(name, getClassLoaders(null));
	}
	
	/**
	 * 用指定的类加载器，从类路径中获取一个类
	 * 
	 * @param name         寻找的目标类
	 * @param classLoader  从类加载器数组中获取第一个类加载器
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
		return classForName(name, getClassLoaders(classLoader));
	}
	
	/**
	 * 从当前类路径中获取一个资源作为URL
	 * 
	 * @param resource
	 * @param classLoader
	 * @return
	 */
	URL getResourceAsURL(String resource, ClassLoader[] classLoader) {
		
		URL url;
		for (ClassLoader cl: classLoader) {
			if (null != cl) {
				url = cl.getResource(resource);
				
				if (null == url) {
					url = cl.getResource("/" + resource);
				}
				
				if (null != url) {
					return url;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 尝试获取一个resource 从类加载器数组中
	 * 
	 * @param resource
	 * @param classLoader
	 * @return
	 */
	InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
		
		for(ClassLoader cl: classLoader) {
			if (null != cl) {
				InputStream returnValue = cl.getResourceAsStream(resource);
				
				if (null == returnValue) {
					returnValue = cl.getResourceAsStream("/" + resource);
				}
				
				if (null != returnValue) {
					return returnValue;
				}
			}
			
		}
		
		return null;
		
	}
	
	
	/**
	 * 尝试从类加载器数组中拿到类加载器获取类
	 * 
	 * @param name
	 * @param classLoader
	 * @return
	 * @throws ClassNotFoundException
	 */
	Class<?> classForName(String name, ClassLoader[] classLoader) throws ClassNotFoundException {
		
		for (ClassLoader cl: classLoader) {
			if (null != cl) {
				try {
					Class<?> c = Class.forName(name);
					if (null != c) {
						return c;
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		throw new ClassNotFoundException("Cannot find class: " + name);
		
	}
	
	
	ClassLoader[] getClassLoaders(ClassLoader classLoader) {
		return new ClassLoader[] {
				classLoader,
				defaultClassLoader,
				systemClassLoader, //javase环境下的类路径
				Thread.currentThread().getContextClassLoader(), //javaee环境下类路径
				getClass().getClassLoader()
		};
	}
}
