/**                                                                                                                                                                                
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.                                                                                                                             
 *                                                                                                                                                                                 
 * Licensed under the Apache License, Version 2.0 (the "License"); you                                                                                                             
 * may not use this file except in compliance with the License. You                                                                                                                
 * may obtain a copy of the License at                                                                                                                                             
 *                                                                                                                                                                                 
 * http://www.apache.org/licenses/LICENSE-2.0                                                                                                                                      
 *                                                                                                                                                                                 
 * Unless required by applicable law or agreed to in writing, software                                                                                                             
 * distributed under the License is distributed on an "AS IS" BASIS,                                                                                                               
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or                                                                                                                 
 * implied. See the License for the specific language governing                                                                                                                    
 * permissions and limitations under the License. See accompanying                                                                                                                 
 * LICENSE file.                                                                                                                                                                   
 */

package com.yahoo.ycsb.generator;

import java.util.HashMap;
import java.util.HashSet;

import com.sun.corba.se.spi.ior.iiop.MaxStreamFormatVersionComponent;
import com.yahoo.ycsb.Utils;

/**
 * A generator of a zipfian distribution. It produces a sequence of items, such that some items are more popular than others, according
 * to a zipfian distribution. When you construct an instance of this class, you specify the number of items in the set to draw from, either
 * by specifying an itemcount (so that the sequence is of items from 0 to itemcount-1) or by specifying a min and a max (so that the sequence is of 
 * items from min to max inclusive). After you construct the instance, you can change the number of items by calling nextInt(itemcount) or nextLong(itemcount).
 * 
 * Unlike @ZipfianGenerator, this class scatters the "popular" items across the itemspace. Use this, instead of @ZipfianGenerator, if you
 * don't want the head of the distribution (the popular items) clustered together.
 */
public class ScrambledZipfianGenerator extends IntegerGenerator 
{
	public static final double ZETAN=52.93805640344461;
	public static final long ITEM_COUNT=10000000000L;
	
	ZipfianGenerator gen;
	long _min,_max,_itemcount;
	
	/******************************* Constructors **************************************/

	/**
	 * Create a zipfian generator for the specified number of items.
	 * @param _items The number of items in the distribution.
	 */
	public ScrambledZipfianGenerator(long _items)
	{
		this(0,_items-1);
	}

	/**
	 * Create a zipfian generator for items between min and max.
	 * @param _min The smallest integer to generate in the sequence.
	 * @param _max The largest integer to generate in the sequence.
	 */
	public ScrambledZipfianGenerator(long _min, long _max)
	{
		this(_min,_max,ZipfianGenerator.ZIPFIAN_CONSTANT);
	}

	/**
	 * Create a zipfian generator for the specified number of items using the specified zipfian constant.
	 * 
	 * @param _items The number of items in the distribution.
	 * @param _zipfianconstant The zipfian constant to use.
	 */
	/*
// not supported, as the value of zeta depends on the zipfian constant, and we have only precomputed zeta for one zipfian constant
	public ScrambledZipfianGenerator(long _items, double _zipfianconstant)
	{
		this(0,_items-1,_zipfianconstant);
	}
*/
	
	/**
	 * Create a zipfian generator for items between min and max (inclusive) for the specified zipfian constant.
	 * @param min The smallest integer to generate in the sequence.
	 * @param max The largest integer to generate in the sequence.
	 * @param _zipfianconstant The zipfian constant to use.
	 */
	ScrambledZipfianGenerator(long min, long max, double _zipfianconstant)
	{
		//not public as we only support one value of zipfianconstant for which we have precomputed zeta
		_min=min;
		_max=max;
		_itemcount=_max-_min+1;
		gen=new ZipfianGenerator(0,ITEM_COUNT,_zipfianconstant,ZETAN);
	}
	
	/**************************************************************************************************/
	
	/**
	 * Return the next int in the sequence.
	 */
	@Override
	public int nextInt() {
		return (int)nextLong();
	}

	/**
	 * Return the next long in the sequence.
	 */
	public long nextLong()
	{
		long ret=gen.nextLong();
		ret=_min+Utils.FNVhash64(ret)%_itemcount;
		setLastInt((int)ret);
		return ret;
	}
	
	public static void main(String[] args)
	{
		//Code used to generate the distribution plot in SRDS-2013
		int max=400000;
		int siteSize=4 ;
		int range=max/siteSize;
		int ops_count=2;
		
		HashMap<Integer, Integer> siteRatio=new HashMap<Integer, Integer>();
		for (int i=0;i<siteSize;i++){
			siteRatio.put(i, 0);
		}
		
//		ScrambledZipfianGenerator gen=new ScrambledZipfianGenerator(max);
		
		UniformIntegerGenerator gen=new UniformIntegerGenerator(0,max-1);
		
		HashSet<Integer> keySets=null;
		
		int opts=0;
		for (int i=0; i<max; i++)
		{

			if (opts==0){
				keySets=new HashSet<Integer>();
			}
			
			
			int key=gen.nextInt();
			int siteId=0;
			for (int j=0;j<siteSize;j++){
				if (key>= (j* range) && key < ((j+1)*range)){
					siteId=j;
					
				}
			}
			
//			System.out.println(siteId +">>"+key);
			keySets.add(siteId);
			opts++;
			if (opts==ops_count){
				opts=0;
					int tmp=siteRatio.get(keySets.size()-1);
					siteRatio.put(keySets.size()-1, tmp+1);
			}
			
		}
		
		int tmp=0;
		for (int i=0;i<siteSize;i++){
			tmp=tmp + siteRatio.get(i);
			double output=(tmp*1.0)/(max/ops_count);
			System.out.println((i+1) + " "+output);		
		}
	}
}
