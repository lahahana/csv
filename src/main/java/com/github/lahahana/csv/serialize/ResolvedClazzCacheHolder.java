package com.github.lahahana.csv.serialize;

import java.util.WeakHashMap;

import com.github.lahahana.csv.base.CsvMetaNode;

class ResolvedClazzCacheHolder {

	private final WeakHashMap<Class<?>, CsvMetaNode<?>[]> cache = new WeakHashMap<Class<?>, CsvMetaNode<?>[]>();
	
	public boolean hasClazzBeenResolved(Class<?> clazz) {
		return cache.containsKey(clazz);
	}
	
	public CsvMetaNode<?>[] getResolvedClazzCache(Class<?> clazz) {
		return cache.get(clazz);
	}
}
