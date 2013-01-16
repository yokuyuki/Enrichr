package edu.mssm.pharm.maayanlab.Enrichr;

public class Shortener {

	private static final String alphabet = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
	private static final int base = 58;
	
	public static String encode(long number) {
		StringBuilder linkId = new StringBuilder();
		
		do {
			int remainder = (int) (number % base);
			linkId.append(alphabet.charAt(remainder));
			number /= base;
		} while (number > 0);
		
		linkId.reverse();
		return linkId.toString();
	}
	
	public static long decode(String linkId) {
		long number = 0;
		
		for (int i = 0; i < linkId.length(); i++) {
			int position = alphabet.indexOf(linkId.charAt(i));
			number += position * (long) Math.pow(base, linkId.length() - i - 1);
		}
		
		System.out.println("");
		
		return number;
	}

}
