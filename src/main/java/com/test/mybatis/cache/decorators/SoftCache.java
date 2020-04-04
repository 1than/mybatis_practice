package com.test.mybatis.cache.decorators;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Deque;
import java.util.LinkedList;

import com.test.mybatis.cache.Cache;

/**
 * 
 *  java中有引用的四种分类 （强 -> 软 -> 弱 -> 虚）
 *  
 *  强引用：java中最普通遍的引用，Object obj = new Object() obj便是强引用
 *  如果一个对象被强引用引用，即使是Java虚拟机内存空间不足时，GC也绝不会回收该对象。
 *  当Java虚拟机内存不足时，就可能会导致内存溢出，即常见额OOM异常
 *  
 *  软引用：软引用是强度仅次于强引用的一种引用类型，使用SofReference来表示，
 *  当java虚拟机内存不足时，GC会回收那些被软引用引用的对象，从而避免内存溢出，在GC释放
 *  了那些被软引用指向的对象之后，虚拟机内存依然不足，才会抛出OOM异常。
 *  软引用适合引用那些可以通过其他方式恢复的对象，例如数据库缓存中的对象就可以从数据库中恢复，
 *  所以软引用可以用来实现缓存，该类所使用的缓存就是通过软引用实现的。
 *  
 *  引用队列：一些场景下，程序需要在一个对象的可达性发生变化时得到通知，引用队列就是用于收集这些信息的队列。
 *  在创建SoftReference对象时，可以为其关联一个引用队列，当SoftReference所引用的对象被GC回收时，Java虚拟机
 *  会将该对象添加到与之关联的引用队列，当需要检测这些通知时，就可以从引用队列获取这些SoftReference对象，不仅时
 *  SoftReference，弱引用和虚引用页可以关联相应的队列，
 *  
 *  弱引用：弱引用的强度比软引用的强度还要弱，弱引用使用WeakReference来表示，它可以引用一个对象，单并不阻止
 *  被引用的对象被GC回收，在JVM进行垃圾回收时，如果指向一个对象的所有引用都是弱引用，那么该对象会被回收。因此，弱
 *  引用所指向的对象的生命周期是两次GC之间的时间，只有被软引用指向的对象可以经历多次GC，直到内存出现紧张的情况才被回收。
 *  
 *  Java对象终止机制：在Object类里面有个finalize()方法，设计该方法的初衷是在一个对象真正被回收之前，执行一些清理工作，
 *  但由于GC的运行时间不是固定的，所以这些清理工作的实际运行时间也是无法预知的，而JVM不能保证finalize()方法一定会被调用。
 *  每个对象的finalize()方法最多被GC执行一次，对于再生对象不会再次调用该方法。而且使用finalize()方法还会导致严重的内存
 *  损耗和性能损失，由于finalize()方法存在的种种问题，已经被废弃，软引用便是其中的替代方案。
 *  
 *  虚引用(幽灵引用)：最弱的一种引用类型，当GC准备回收一个对象时，如果发现它还有虚引用，就会在回收对象之前，把该虚引用加入
 *  到与之关联的引用队列中，程序可以通过检查该引用队列里面的内容，跟踪对象是否已经被回收并进行一些清理工作，虚引用还可以用来
 *  实现比较精细的内存控制，例如应用程序可以确定一个对象要被回收之后，再申请内存创建新对象。
 *  
 *  
 *  SoftCache缓存项中的value是SoftEntry对象，SoftEntry继承了SoftReference，其中指向key的引用是强引用，而
 *  指向value的引用是软引用
 * 
 * @author ethan
 *
 */
public class SoftCache implements Cache {

	/**
	 * 
	 * 在softCache最近使用的一部分缓存项不会被GC回收，通过将其value添加到hardLinksToAvoidGarbageCollection中。
	 * hardLinksToAvoidGarbageCollection集合是LinkedList<Object>类型
	 * 
	 */
	private final Deque<Object> hardLinksToAvoidGarbageCollection;
	
	/**
	 * 
	 * 引用队列，用于记录已经被GC回收的缓存项对应的SoftEntry对象
	 * 
	 */
	private final ReferenceQueue<Object> queueOfGarbageCollectedEntries;
	
	/**
	 * 
	 * 缓存对象
	 * 
	 */
	private final Cache delegate;
	
	/**
	 * 
	 * 强连接的个数，默认是256
	 * 
	 */
	private int numberOfHardLinks;

	public SoftCache(Cache delegate) {
		this.delegate = delegate;
		this.numberOfHardLinks = 256;
		this.hardLinksToAvoidGarbageCollection = new LinkedList<>();
		this.queueOfGarbageCollectedEntries = new ReferenceQueue<>();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		removeGarbageCollectedItems();
		return delegate.getSize();
	}

	public void setSize(int size) {
		this.numberOfHardLinks = size;
	}

	@Override
	public void putObject(Object key, Object value) {
		removeGarbageCollectedItems();
		delegate.putObject(key, new SoftEntry(key, value, queueOfGarbageCollectedEntries));
	}

	@Override
	public Object getObject(Object key) {
		Object result = null;
		@SuppressWarnings("unchecked") // assumed delegate cache is totally managed by this cache
		SoftReference<Object> softReference = (SoftReference<Object>) delegate.getObject(key);
		if (softReference != null) {
			result = softReference.get();
			if (result == null) {
				delegate.removeObject(key);
			} else {
				// See #586 (and #335) modifications need more than a read lock
				synchronized (hardLinksToAvoidGarbageCollection) {
					hardLinksToAvoidGarbageCollection.addFirst(result);
					if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
						hardLinksToAvoidGarbageCollection.removeLast();
					}
				}
			}
		}
		return result;
	}

	@Override
	public Object removeObject(Object key) {
		removeGarbageCollectedItems();
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		synchronized (hardLinksToAvoidGarbageCollection) {
			hardLinksToAvoidGarbageCollection.clear();
		}
		removeGarbageCollectedItems();
		delegate.clear();
	}

	private void removeGarbageCollectedItems() {
		SoftEntry sv;
		while ((sv = (SoftEntry) queueOfGarbageCollectedEntries.poll()) != null) {
			delegate.removeObject(sv.key);
		}
	}

	private static class SoftEntry extends SoftReference<Object> {
		private final Object key;

		SoftEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
			super(value, garbageCollectionQueue);//指向value的引用是软引用，而且关联了引用队列
			this.key = key;//强引用
		}
	}
}
