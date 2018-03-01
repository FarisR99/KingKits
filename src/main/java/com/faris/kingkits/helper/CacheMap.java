package com.faris.kingkits.helper;

import java.lang.ref.SoftReference;
import java.util.*;

public class CacheMap<K, V> {

	private long expireTime = 1000L * 60L;
	private Map<K, CachedEntry<V>> map = new HashMap<>();

	public boolean containsKey(K key) {
		return this.map.containsKey(key) && this.get(key) != null;
	}

	public Set<Map.Entry<K, CachedEntry<V>>> entrySet() {
		return this.map.entrySet();
	}

	public V get(K key) {
		CachedEntry<V> entry = this.map.get(key);
		if (entry == null) return null;
		if (entry.isExpired()) {
			this.map.remove(key);
			return null;
		} else {
			return entry.getValue();
		}
	}

	public void put(K key, V value) {
		this.map.put(key, new CachedEntry<>(value, this.expireTime));
	}

	public void refresh() {
		for (Map.Entry<K, CachedEntry<V>> entry : new HashMap<>(this.map).entrySet()) {
			if (entry.getValue().isExpired()) {
				this.map.remove(entry.getKey());
			}
		}
	}

	public void remove(K key) {
		this.map.remove(key);
	}

	public static class CachedEntry<V> {
		private final SoftReference<V> value;
		private final long expires;

		public CachedEntry(V value, long expireTime) {
			this.value = new SoftReference<>(value);
			this.expires = expireTime + System.currentTimeMillis();
		}

		public V getValue() {
			return this.isExpired() ? null : this.value.get();
		}

		public boolean isExpired() {
			return this.value.get() == null || (this.expires != -1 && this.expires > System.currentTimeMillis());
		}
	}

}
