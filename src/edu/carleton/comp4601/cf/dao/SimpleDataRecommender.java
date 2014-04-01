/*M///////////////////////////////////////////////////////////////////////////////////////
//
//  IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
//
//  By downloading, copying, installing or using the software you agree to this license.
//  If you do not agree to this license, do not download, install,
//  copy or use the software.
//
//
//                           License Agreement
//                For de.apaxo.bedcon.FacebookRecommender Bean
//
// Copyright (C) 2012, Apaxo GmbH, all rights reserved.
// Third party copyrights are property of their respective owners.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//   * Redistribution's of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//
//   * Redistribution's in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//
//   * The name of the copyright holders may not be used to endorse or promote products
//     derived from this software without specific prior written permission.
//
// This software is provided by the copyright holders and contributors "as is" and
// any express or implied warranties, including, but not limited to, the implied
// warranties of merchantability and fitness for a particular purpose are disclaimed.
// In no event shall the Apaxo GmbH or contributors be liable for any direct,
// indirect, incidental, special, exemplary, or consequential damages
// (including, but not limited to, procurement of substitute goods or services;
// loss of use, data, or profits; or business interruption) however caused
// and on any theory of liability, whether in contract, strict liability,
// or tort (including negligence or otherwise) arising in any way out of
// the use of this software, even if advised of the possibility of such damage.
//
//M*/
package edu.carleton.comp4601.cf.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.MemoryIDMigrator;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDPlusPlusFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

/**
 * This class shows some examples which are implemented
 * in java for showing how to use the different recommenders
 * in java.
 * It uses animals and foods as examples.
 * @author Manuel Blechschmidt <blechschmidt@apaxo.de>
 *
 */
public class SimpleDataRecommender {
	
	private MemoryIDMigrator id2thing = new MemoryIDMigrator();
//	private List<String> foods = new ArrayList<String>();
//	private List<String> animals = new ArrayList<String>();
	private DataModel model;
	private int[][] ratings;
	private String[] items;
	private String[] users;
	
	/*
	 * 5 5 
Alice User1 User2 User3 User4
Item1 Item2 Item3 Item4 Item5
5 3 4 4 -1
3 1 2 3 3
4 3 4 3 5
3 3 1 5 4 
1 5 5 2 1

	 */
	
	File file;
	
	
	public SimpleDataRecommender(File file) throws FileNotFoundException {
		this.file = file;
		input();
		initMemoryMigrator();
		initDataModel();
		initRecommender();
	}
	
	/**
	 * This function generates ids for
	 * the different things in the demp
	 */
	private void initMemoryMigrator() {
		for(String user : users) {
			id2thing.storeMapping(id2thing.toLongID(user), user);
			System.out.println(user+" = "+id2thing.toLongID(user));
		}
		for(String item : items) {
			id2thing.storeMapping(id2thing.toLongID(item), item);
			System.out.println(item+" = "+id2thing.toLongID(item));
		}
	}
	
	public void initDataModel() {
		FastByIDMap<PreferenceArray> preferenceMap = new FastByIDMap<PreferenceArray>();
		for(int i=0;i<users.length;i++) {
			List<Preference> userPreferences = new ArrayList<Preference>();
			long userId = id2thing.toLongID(users[i]);
			for(int j=0;j<items.length;j++) {
				if(ratings[i][j] != -1) {
//					System.out.println(userId+" | "+items[j] + " | "+ ratings[i][j]);
					userPreferences.add(new GenericPreference(userId, id2thing.toLongID(items[j]), ratings[i][j]));
				}
			}
			GenericUserPreferenceArray userArray = new GenericUserPreferenceArray(userPreferences);
			preferenceMap.put(userId, userArray);
		}
		model = new GenericDataModel(preferenceMap);
	}
	
	public void initRecommender() {
		try {
			
			PearsonCorrelationSimilarity pearsonSimilarity = new PearsonCorrelationSimilarity(model);
			
			System.out.println("Similarity between Alice and User1: "+pearsonSimilarity.userSimilarity(id2thing.toLongID("Alice"), id2thing.toLongID("User1")));
			System.out.println("Similarity between Alice and User2: "+pearsonSimilarity.userSimilarity(id2thing.toLongID("Alice"), id2thing.toLongID("User2")));
			System.out.println("Similarity between Alice and User3: "+pearsonSimilarity.userSimilarity(id2thing.toLongID("Alice"), id2thing.toLongID("User3")));
			
			GenericUserBasedRecommender recommender = new GenericUserBasedRecommender(model, new NearestNUserNeighborhood(3, pearsonSimilarity, model), pearsonSimilarity);
			for(RecommendedItem r : recommender.recommend(id2thing.toLongID("Alice"), 3)) {
				System.out.println("UserBased: Alice should like: "+id2thing.toStringID(r.getItemID())+" Rating: "+r.getValue());
			}
			SVDRecommender svdrecommender = new SVDRecommender(model, new SVDPlusPlusFactorizer(model, 4, 1000));
			for(RecommendedItem r : svdrecommender.recommend(id2thing.toLongID("User1"), 3)) {
				System.out.println("SVD: User1 should like: "+id2thing.toStringID(r.getItemID())+" Rating: "+r.getValue());
			}
		} catch (TasteException e) {
			e.printStackTrace();
		}
	}

	

	public boolean input() throws FileNotFoundException {
		boolean okay = true;
		
		Scanner s = new Scanner(file);
		int nUsers = s.nextInt();
		int nItems = s.nextInt();
		
		users = new String[nUsers];
		for (int i = 0; i < nUsers; i++)
			users[i] = s.next();
		items = new String[nItems];
		for (int j = 0; j < nItems; j++)
			items[j] = s.next();
		
		ratings = new int[nUsers][nItems];
		for (int i = 0; i < nUsers; i++) {
			for (int j = 0; j < nItems; j++) {
				ratings[i][j] = s.nextInt();
			}
		}
				
		s.close();
		return okay;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("======================== x text.txt x ============================");
		new SimpleDataRecommender(new File("test.txt"));
		System.out.println("======================== x text2.txt x ============================");
		new SimpleDataRecommender(new File("test2.txt"));
		System.out.println("======================== x text3.txt x ============================");
		new SimpleDataRecommender(new File("test3.txt"));
	}
}
