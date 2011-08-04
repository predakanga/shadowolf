package com.shadowolf.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utitlity class that allows for generic Reservoir Sampling.
 * 
 *
 * @see http://en.wikipedia.org/wiki/Reservoir_sampling
 */
public class ReservoirSampler {
	private ReservoirSampler() {} 
	
	@SuppressWarnings("unchecked")
	public static <T> T[] arraySample(T[] collection, int sampleSize) {
		T[] list;
		
		if(collection.length < sampleSize) {
			return collection;
		} else {
			//we have to cast here, java generic failure
			list = (T[]) new Object[sampleSize];
		}
		
		Random rand = new Random(System.nanoTime());
		
		for(int i = 0; i < collection.length; i++) {
			if(i < sampleSize) {
				list[i] = collection[i];
			} else {
				final int rnd = rand.nextInt(i);
				if(rnd < sampleSize) {
					list[rnd] = collection[i];
				}
			}
		}
		
		return list;
	}
		
	
	public static <T> List<T> listSample(T[] collection, int sampleSize) {
		List<T> list = new ArrayList<>(sampleSize);
		int count = 0;
		Random rand = new Random(System.nanoTime());
		for(T t : collection) {
			count++;
			if(count <= sampleSize) {
				list.add(t);
			} else {
				final int rnd = rand.nextInt(count);
				if(rnd < sampleSize) {
					list.set(rnd, t);
				}
			}
		}
		
		return list;
	}
	
	public static <T> List<T> listSample(Iterable<T> collection, int sampleSize) {
		List<T> list = new ArrayList<>(sampleSize);
		int count = 0;
		Random rand = new Random(System.nanoTime());
		for(T t : collection) {
			count++;
			if(count <= sampleSize) {
				list.add(t);
			} else {
				final int rnd = rand.nextInt(count);
				if(rnd < sampleSize) {
					list.set(rnd, t);
				}
			}
		}
		
		return list;
	}
}
