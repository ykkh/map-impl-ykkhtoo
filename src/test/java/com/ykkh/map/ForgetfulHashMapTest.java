package com.ykkh.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.ykkh.customer.model.Customer;

public class ForgetfulHashMapTest {

	@Test
	public void testInsertFind() {

		/**
		 * Test insert, overwrite and find.
		 * 
		 * Create a Map with the capacity with 4 rooms. (Internally it will create 5
		 * because it needs a Prime number for hashing (double hashing) to work) Create
		 * 4 Customer objects but there are two customers with the same key in which
		 * it's email address. The earlier customer "Patrick Cole" will get overwritten.
		 * 
		 * So the expected map size would be 3. (capacity would be 4 as given) So
		 * "pcole@gmail.com" should return "Paul Cole"
		 * 
		 */

		TMap<String, Customer> customers = new ForgetfulHashMap<>(4);

		Customer c1 = new Customer("John Smith", "johnsmith@gmail.com");
		Customer c2 = new Customer("Patrick Cole", "pcole@gmail.com");
		Customer c3 = new Customer("Mary Brown", "marybrown@gmail.com");
		Customer c4 = new Customer("Paul Cole", "pcole@gmail.com"); // overwrite the value to c2

		customers.put(c1.getEmail(), c1);
		customers.put(c2.getEmail(), c2);
		customers.put(c3.getEmail(), c3);
		customers.put(c4.getEmail(), c4);

		assertEquals(3, customers.size());

		assertEquals("John Smith", customers.get("johnsmith@gmail.com").getName());
		assertEquals("Paul Cole", customers.get("pcole@gmail.com").getName());

	}

	@Test
	public void testForgetfulness() {
		/**
		 * Test that the value object with the least access will get removed when the
		 * capacity is full.
		 * 
		 * Put 3 customers with 3 different keys into the Map. Those objects were
		 * searched. Put another customers and the least access customer "John Smith"
		 * will be removed. And the new object will put into that room. Since the
		 * capacity is already full. Size wont' change.
		 * 
		 */

		Customer c1 = new Customer("John Smith", "johnsmith@gmail.com");
		Customer c2 = new Customer("Patrick Cole", "pcole@gmail.com");
		Customer c3 = new Customer("Mary Brown", "marybrown@gmail.com");

		TMap<String, Customer> customers = new ForgetfulHashMap<>(3);
		customers.put(c1.getEmail(), c1);
		customers.put(c2.getEmail(), c2);
		customers.put(c3.getEmail(), c3);

		customers.get("pcole@gmail.com");
		customers.get("pcole@gmail.com");
		customers.get("pcole@gmail.com");

		customers.get("johnsmith@gmail.com"); // least access

		customers.get("marybrown@gmail.com");
		customers.get("marybrown@gmail.com");
		customers.get("marybrown@gmail.com");

		assertNotNull(customers.get("johnsmith@gmail.com")); // test before putting 1 more object into the HashTable. Access Count will be increased

		Customer c4 = new Customer("Paul Cole", "paulcole@mail.co.uk");
		customers.put(c4.getEmail(), c4);// johnsmith@gmail.com should get replaced with the least access 2

		assertNull(customers.get("johnsmith@gmail.com"));

		assertEquals("Paul Cole", customers.get("paulcole@mail.co.uk").getName());
		assertEquals(3, customers.size()); // size not change

	}
	
	@Test
	public void testForgetfulness2() {
		/**
		 * Test with same access count.
		 * 
		 */

		Customer c1 = new Customer("John Smith", "johnsmith@gmail.com");
		Customer c2 = new Customer("Patrick Cole", "pcole@gmail.com");
		Customer c3 = new Customer("Mary Brown", "zamarybrown@gmail.com");

		TMap<String, Customer> customers = new ForgetfulHashMap<>(3);
		customers.put(c3.getEmail(), c3);
		customers.put(c1.getEmail(), c1);
		customers.put(c2.getEmail(), c2);

		Customer c4 = new Customer("Paul Cole", "paulcole@mail.co.uk");
		customers.put(c4.getEmail(), c4);// no gurantee which one will be replaced.


		assertEquals("Paul Cole", customers.get("paulcole@mail.co.uk").getName());
		assertEquals(3, customers.size()); // size not change

	}

	@Test
	public void testNullKey() {
		TMap<String, Integer> myMap = new ForgetfulHashMap<>(3);

		/**
		 * IllegalArgumentException should be thrown if the key or value is null
		 */

		assertThrows(IllegalArgumentException.class, () -> {
			myMap.put(null, 2);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			myMap.put("testNull", null);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			myMap.put(null, null);
		});
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testThreadSafy() throws InterruptedException {
		/**
		 * To test thread safty by calling get method and check the access count after
		 * that.
		 * 
		 * Running 5 threads with 1000 iteration each. The count should be 5000.
		 */
		TMap<String, Integer> myMap = new ForgetfulHashMap<>(3);

		myMap.put("threadSafe", 0);

		runManyThreads(myMap); // 5 threads with 1000 iteration each
		@SuppressWarnings("unchecked")
		int count = ((ForgetfulHashMap) myMap).getAccessCount("threadSafe");
		assertEquals(5000, count);
	}

	private void runManyThreads(TMap<String, Integer> map) throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(6);
		executorService.execute(new Runnable() {
			public void run() {
				for (int i = 0; i < 1000; i++) {
					map.get("threadSafe");
				}
			}
		});
		executorService.execute(new Runnable() {
			public void run() {
				for (int i = 0; i < 1000; i++) {
					map.get("threadSafe");
				}
			}
		});

		executorService.execute(new Runnable() {
			public void run() {
				for (int i = 0; i < 1000; i++) {
					map.get("threadSafe");
				}
			}
		});
		executorService.execute(new Runnable() {
			public void run() {
				for (int i = 0; i < 1000; i++) {
					map.get("threadSafe");
				}
			}
		});
		executorService.execute(new Runnable() {
			public void run() {
				for (int i = 0; i < 1000; i++) {
					map.get("threadSafe");
				}
			}
		});
		executorService.shutdown();
		executorService.awaitTermination(5, TimeUnit.SECONDS);

	}

}
