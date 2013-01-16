package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ShortenerTest extends TestCase {

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( ShortenerTest.class );
	}
	
	public void testEquivalency() {
		Random rng = new Random();
		long number = rng.nextLong();
		number >>>= 1;	// Shifts a 0 into the leftmost position making it always positive
		
		long result = Shortener.decode(Shortener.encode(number));
		assertEquals(number, result);
	}
	
	public void testEncode() {
		assertEquals("1", Shortener.encode(0L));
		assertEquals("2", Shortener.encode(1L));
		assertEquals("npL6MjP8Qfc", Shortener.encode(9223372036854775807L));
	}
	
	public void testDecode() {
		assertEquals(0L, Shortener.decode("1"));
		assertEquals(1L, Shortener.decode("2"));
		assertEquals(9223372036854775807L, Shortener.decode("npL6MjP8Qfc"));
	}
	
}
