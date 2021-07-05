package com.ykkh.map;

/**
 * 
 * Hash table based implementation of the {@code TMap} interface. It's
 * synchronised. It's not a dynamic Hash Table. The capacity to be passed from
 * Constructor. When the map is full, the least access will be removed and the
 * new value will be put in that room.
 * 
 * 
 * @author Ye Kyaw Kyaw Htoo (ykkh)
 * 
 * 
 *
 * 
 * @param <K>
 * @param <V>
 */
public class ForgetfulHashMap<K, V> implements TMap<K, V> {

	private Entry<K, V>[] hashArray;
	private int capacity;
	private int size;
	private int primeCapacity;

	private int smallPrime; // for hash method 2. To determine according to the capacity.

	@SuppressWarnings("unchecked")
	public ForgetfulHashMap(int capacity) {
		if (isPrime(capacity)) {
			this.capacity = capacity;
			this.primeCapacity = capacity;
			hashArray = new Entry[capacity];
		} else {
			this.capacity = capacity;
			this.primeCapacity = getNextPrime(capacity);
			hashArray = new Entry[primeCapacity];
		}
		smallPrime = getSmallPrime();
	}

	/**
	 * Associate the specified {@code key} to the specified {@code value} in this
	 * hashmap. Neither the key nor the value can be {@code null}.
	 * <p>
	 * 
	 * When the map is full, the least access will be removed and the new value will
	 * be put in that room.
	 * <p>
	 * 
	 * When the key already exist, the previous value will be overwritten.
	 *
	 * The value can be retrieved by calling the {@code get} method with a key that
	 * is equal to the original key.
	 *
	 * @param key   the hashtable key
	 * @param value the value
	 * @return the previous value of the specified key in this hashtable, or
	 *         {@code null} if it did not have one
	 */
	public synchronized void put(K key, V value) {

		if (key == null || value == null) {
			throw new IllegalArgumentException("Key can't be null");
		}

		Entry<K, V> e = new Entry<K, V>(key, value);
		int keyHash = hashMethod1(key);
		int ss = hashMethod2(key);

		a: if (size < capacity) { // Not Full
			while (hashArray[keyHash] != null) { // hash collision is occurring if it's not null
				Entry<K, V> en = hashArray[keyHash];
				if (en.getKey().equals(key)) { // overwrite value with the same key
					hashArray[keyHash] = e;
					break a; // no need to change size nor count
				}
				keyHash = keyHash + ss;
				keyHash = keyHash % primeCapacity;
			}
			hashArray[keyHash] = e;
			size++;
		} else { // isFull
			/**
			 * if there are multiple least-used entries, the first one that found in the
			 * iteration will be removed.
			 */
			int min = hashArray[0].accessCount;
			boolean accessCountFlag = true;
			h: for (int i = 1; i < hashArray.length; i++) {

				if (min == 0) {
					// Access Count 0 would be the least, if it's 0, doesn't need to iterate more.
					// this condition is for index Zero
					hashArray[0] = e;
					accessCountFlag = false; // to skip the second iteration
					break h;
				} else if (hashArray[i].accessCount == 0) {
					// Access Count 0 would be the least, if it's 0, doesn't need to iterate more.
					hashArray[i] = e;
					accessCountFlag = false; // to skip the second iteration
					break h;
				} else {
					if (hashArray[i].accessCount < min) {
						min = hashArray[i].accessCount;
					}
				}
			}
			if (accessCountFlag) {
				z: for (int i = 1; i < hashArray.length; i++) {
					if (hashArray[i].accessCount == min) {
						hashArray[i] = e;
						break z;
					}
				}
			}
		}
	}

	/**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     * 
     * Increase the access count of that particular entry 
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     */
	public synchronized V get(K key) {
		
		if(key==null) {
			throw new IllegalArgumentException("Key can't be null");
		}
		
		int keyHash = hashMethod1(key);
		int ss = hashMethod2(key);

		if (hashArray[keyHash] == null) {
			return null;
		}
		int count = 0;

		while (hashArray[keyHash] != null && count < size) {
			count++;
			Entry<K, V> e = hashArray[keyHash];
			if (e.getKey().equals(key)) {
				e.accessCount++;
				return (V) e.getValue();
			}

			keyHash = keyHash + ss;
			keyHash = keyHash % primeCapacity;
		}

		return null;
	}

	public synchronized int size() {
		return size;
	}

	/**
	 * This public method only exist in this ForgetfulHashMap implementation since
	 * the access count logic is tie to this very implementation.
	 * 
	 * 
	 * @param key
	 * @return total access count for particular entry
	 */
	public synchronized int getAccessCount(K key) {
		int keyHash = hashMethod1(key);
		int ss = hashMethod2(key);

		if (hashArray[keyHash] == null) {
			return 0;
		}
		int count = 0;
		while (hashArray[keyHash] != null && count < size) {
			count++;
			Entry<K, V> e = hashArray[keyHash];
			if (e.getKey().equals(key)) {
				return e.accessCount;
			}

			keyHash = keyHash + ss;
			keyHash = keyHash % primeCapacity;
		}

		return 0;
	}

	/** Private Methods Start */

	private int hashMethod1(Object key) {
		int keyHash = key.hashCode();

		keyHash = keyHash % primeCapacity;

		if (keyHash < 0) {
			keyHash += primeCapacity;
		}

		return keyHash;
	}

	private int hashMethod2(Object key) {
		int keyHash = key.hashCode();

		keyHash = keyHash % primeCapacity;

		if (keyHash < 0) {
			keyHash += primeCapacity;
		}

		return smallPrime - keyHash % smallPrime;
	}

	private int getSmallPrime() {
		for (int i = capacity - 1; i >= 2; i--) {
			if (isPrime(i))
				return i;
		}

		return 1;
	}

	private int getNextPrime(int capacity) {
		for (int i = capacity; true; i++) {
			if (isPrime(i))
				return i;
		}
	}

	private boolean isPrime(int capacity) {
		for (int i = 2; i * i < capacity; i++) {
			if (capacity % i == 0) {
				return false;
			}
		}
		return true;
	}

	/** Private Methods End */

	static class Entry<K, V> {
		private K key;
		private V value;
		private int accessCount;

		public int getAccessCount() {
			return accessCount;
		}

		public void setAccessCount(int accessCount) {
			this.accessCount = accessCount;
		}

		public K getKey() {
			return key;
		}

		public void setKey(K key) {
			this.key = key;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}

		public Entry(K key, V value) {
			super();
			this.key = key;
			this.value = value;
		}

	}
}
