package edu.carleton.comp4601.cf.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import Jama.Matrix;

public class SimpleDataAccessObject {
	
	File file;
	private int[][] ratings;
	private String[] items;
	private String[] users;
	private double[][] alice_user_based;
	private double[][] item_based;

	public SimpleDataAccessObject(File file) {
		this.file = file;
	}
	
	public int[][] getRatings() {
		return ratings;
	}

	public String[] getItems() {
		return items;
	}

	public String[] getUsers() {
		return users;
	}
	
	public double averageRating(int user){
		int average = 0, count = 0;

		for(int rating: ratings[user]){
			if(rating == -1)
				continue;
			count++;
			average  += rating;
		}
//		System.out.println("User("+user+") avg_rating="+average+"/"+count);
		return ((double) average/count);
	}
	
	public double averageItem(int item){
		int average = 0, count = 0;

		for(int i = 0; i < users.length; i++){
			if(ratings[i][item] == -1)
				continue;
			average += ratings[i][item];
			count++;
		}
		return ((double) average/count);
	}
	
	public double[] simUser(int user){
		double[] userBased_sim = new double [users.length];
		for(int i=0; i<users.length; i++){
			userBased_sim [i] = simUser(user, i);
		}
		return userBased_sim;
	}

	public double[] simItem(int item){
		double[] itemBased_sim = new double [items.length];
		for(int i=0; i<items.length; i++){
			itemBased_sim [i] = simItem(item, i);
		}
		return itemBased_sim;
	}

	public double simUser(int userA, int userB){
		if(userA == userB)
			return 1d;
			
		double sim = 0;
		double sumDot = 0;
		double absA = 0;
		double absB = 0;
		for(int i = 0; i < ratings[userA].length; i++){
			int ratingA = ratings[userA][i];
			int ratingB = ratings[userB][i];
//			System.out.println(ratingA + " vs " + ratingB);
			if (ratingA > 0 && ratingB > 0 ){
				double userA_avgRating = averageRating(userA);
				double userB_avgRating = averageRating(userB);

				sumDot += ((ratingA - userA_avgRating) * (ratingB - userB_avgRating));

				absA += Math.pow((ratingA - userA_avgRating), 2);
				absB += Math.pow((ratingB - userB_avgRating), 2);
			}
		}
		absA = Math.sqrt(absA);
		absB = Math.sqrt(absB);
		
		sim = sumDot / (absA * absB);
		
//		System.out.println("similarity of userA and B is " + sim);
		
		return sim;
	}
	
	public double simItem(int itemA, int itemB){
		if(itemA == itemB)
			return 1d;
		
		double sim = 0;
		double sumDot = 0;
		double absA = 0;
		double absB = 0;
		for(int i = 0; i < users.length; i++){
			double user_i_avg_rating = averageRating(i);
			int user_i_rating_itemA = ratings[i][itemA];
			int user_i_rating_itemB = ratings[i][itemB];
//			System.out.println(ratingA + " vs " + ratingB);
			if (user_i_rating_itemA > 0 && user_i_rating_itemB > 0 ){

				sumDot += ((user_i_rating_itemA - user_i_avg_rating) * (user_i_rating_itemB - user_i_avg_rating));

				absA += Math.pow((user_i_rating_itemA - user_i_avg_rating), 2);
				absB += Math.pow((user_i_rating_itemB - user_i_avg_rating), 2);
			}
		}
		absA = Math.sqrt(absA);
		absB = Math.sqrt(absB);
		
		sim = sumDot / (absA * absB);
		
//		System.out.println("similarity of Item A and B is " + sim);
		
		return sim;
	}
	
	public double userBasedPredict(int user, int item){
		if(ratings[user][item] > -1)
			return ratings[user][item];
				
		double user_avgRating = averageRating(user);
		double numerator = 0;
		double denominator = 0;
		
		for(int i=0; i< users.length; i++){
			double sim_user_useri = simUser(user, i);
			if(isNeighbor(user, i, sim_user_useri)){
				double user_i_item_rating = ratings[i][item];
				double user_i_avg_rating = averageRating(i);
				
				numerator += (sim_user_useri*(user_i_item_rating-user_i_avg_rating));
				denominator += sim_user_useri;
			}
		}
		
		if(denominator>0)
			return ((double)(user_avgRating+numerator/denominator));
		else
			return 1d;			
		
	}
	
	public double itemBasedPredict(int user, int item){
		if(ratings[user][item] > -1)
			return ratings[user][item];
		
//		System.out.println("item "+item +" is to be predicted for user "+user);
		
		double numerator = 0;
		double denominator = 0;
		
		for(int itemRated=0; itemRated< items.length; itemRated++){
			double itemSim = simItem(item, itemRated);

			if(ratings[user][itemRated] > -1 && itemSim>0){
				numerator +=  itemSim * ratings[user][itemRated];
				denominator += itemSim;
			}
		}
		
		return ((double) numerator/denominator);

	}

	private boolean isNeighbor(int usr1, int usr2){
		return isNeighbor(usr1, usr2, simUser(usr1, usr2));
	}
	
	private boolean isNeighbor(int usr1, int usr2, double sim){
		return usr1!=usr2 && sim > 0;
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
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("SimpleDataAccessObject\n\n");
		for (String u : users) {
			buf.append(u);
			buf.append(" ");
		}
		buf.append("\n");
		for (String i : items) {
			buf.append(i);
			buf.append(" ");
		}		
		buf.append("\n");
		for (int i = 0; i < users.length; i++) {
			for (int j = 0; j < items.length; j++) {
				if (ratings[i][j] == -1)
					buf.append("?");
				else
					buf.append(ratings[i][j]);
				buf.append(" ");
			}
			buf.append("\n");
		}
		return buf.toString();
	}

	public void printUsersSimilarties(){
		for (int i=0; i<users.length; i++){
			double[] values = simUser(i);
			printValues(values);
			System.out.println("\n");
		}
	}

	public void printUserBasedPredictions(){
		for (int i=0; i<users.length; i++){
			for(int item=0; item < items.length; item++){
				double prediction = userBasedPredict(i, item);
				System.out.printf("%2.3f ", prediction);
			}
			System.out.println("\n");
		}
	}
	
	public void printItemsSimilarties(){
		for (int i=0; i<items.length; i++){
			double[] values = simItem(i);
			printValues(values);
			System.out.println("\n");
		}
	}
	
	public void printItemBasedPredictions(){
		for (int i=0; i<users.length; i++){
			for(int item=0; item < ratings[0].length; item++){
				double prediction = itemBasedPredict(i, item);
				System.out.printf("%2.3f ", prediction);
			}
			System.out.println("\n");
		}
	}

	
	private void printValues(double[] values){
		for(int i=0; i<values.length; i++)
			System.out.printf("%2.3f ", values[i]);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		SimpleDataAccessObject sdao = new SimpleDataAccessObject(new File("test.txt"));
		sdao.input();
		System.out.println(sdao);

//		Matrix m = convertToMatix(sdao.ratings);
//		Matrix alice = m.getMatrix(0, 0, 0, m.getColumnDimension()-1);
//		Matrix usr1 = m.getMatrix(1, 1, 0, m.getColumnDimension()-1);
//		Matrix usr2 = m.getMatrix(2, 2, 0, m.getColumnDimension()-1);
//		Matrix usr3 = m.getMatrix(3, 3, 0, m.getColumnDimension()-1);
//		Matrix usr4 = m.getMatrix(4, 4, 0, m.getColumnDimension()-1);

//		System.out.print("[a]Similarity between user Alice and 1 is:");
//		System.out.println(calculateSimilarity(alice.getArray()[0], usr1.getArray()[0]));
		System.out.print("[b]Similarity between user Alice and 1 is:");
		System.out.println(sdao.simUser(0,1));
//		System.out.print("[a]Similarity between user Alice and 2 is:");
//		System.out.println(calculateSimilarity(alice.getArray()[0], usr2.getArray()[0]));
		System.out.print("[b]Similarity between user Alice and 2 is:");
		System.out.println(sdao.simUser(0,2));
//		System.out.print("[a]Similarity between user Alice and 3 is:");
//		System.out.println(calculateSimilarity(alice.getArray()[0], usr3.getArray()[0]));
		System.out.print("[b]Similarity between user Alice and 3 is:");
		System.out.println(sdao.simUser(0,3));
//		System.out.print("[a]Similarity between user Alice and 4 is:");
//		System.out.println(calculateSimilarity(alice.getArray()[0], usr4.getArray()[0]));
		System.out.print("[b]Similarity between user Alice and 4 is:");
		System.out.println(sdao.simUser(0,4));

		System.out.println("\n\n	-- User Based --\n\n");
		sdao.printUsersSimilarties();
		System.out.println("Predictions:\n");
		sdao.printUserBasedPredictions();
		
		
		System.out.println("\n\n	-- Item Based --\n\n");
		sdao.printItemsSimilarties();
		System.out.println("Predictions:\n");
		sdao.printItemBasedPredictions();
		
//		
//		System.out.print("Similarity between user 3 and 4 is:");
//		System.out.println(sdao.simUser(3,4));
//		
//		System.out.print("Similarity between item 3 and 4 is:");
//		System.out.println(sdao.simItem(3,4));
//
//		System.out.print("Predicted rating for user 0 at index 4 is:");
//		System.out.println(sdao.predict(0,4));
		System.out.println("=====================================");
		
		
		sdao = new SimpleDataAccessObject(new File("test2.txt"));
		sdao.input();
		System.out.println(sdao);

		System.out.print("Similarity between user 1 and 4 is:");
		System.out.println(sdao.simUser(1,4));
		
		System.out.print("Similarity between item 2 and 3 is:");
		System.out.println(sdao.simItem(2,3));

		System.out.print("Predicted rating for user 0 at index 0 is:");
		System.out.println(sdao.userBasedPredict(0,0));
		
		System.out.print("Predicted rating for user 1 at index 1 is:");
		System.out.println(sdao.userBasedPredict(1,1));
		
		System.out.print("Predicted rating for user 2 at index 2 is:");
		System.out.println(sdao.userBasedPredict(2,2));
		
		System.out.print("Predicted rating for user 3 at index 3 is:");
		System.out.println(sdao.userBasedPredict(3,3));
		
		System.out.print("Predicted rating for user 4 at index 4 is:");
		System.out.println(sdao.userBasedPredict(4,4));

		System.out.println("\n\n	-- User Based --\n\n");
		sdao.printUsersSimilarties();
		System.out.println("Predictions:\n");
		sdao.printUserBasedPredictions();
		
		
		System.out.println("\n\n	-- Item Based --\n\n");
		sdao.printItemsSimilarties();
		System.out.println("Predictions:\n");
		sdao.printItemBasedPredictions();

		
		
		System.out.println("=====================================");
		
		sdao = new SimpleDataAccessObject(new File("test3.txt"));
		sdao.input();
		System.out.println(sdao);


		
		System.out.print("Similarity between item 1 and 4 is:");
		System.out.println(sdao.simItem(1,4));

		System.out.print("Similarity between item 4 and 6 is:");
		System.out.println(sdao.simItem(4,6));

		System.out.print("Similarity between item 6 and 7 is:");
		System.out.println(sdao.simItem(6,7));

		System.out.print("Similarity between item 2 and 9 is:");
		System.out.println(sdao.simItem(2,9));

		System.out.print("Similarity between item 5 and 8 is:");
		System.out.println(sdao.simItem(5,8));
		

		
		System.out.print("Similarity between user 1 and 4 is:");
		System.out.println(sdao.simUser(1,4));
		
		System.out.print("Similarity between user 2 and 3 is:");
		System.out.println(sdao.simUser(2,3));
		
		System.out.print("Similarity between user 3 and 1 is:");
		System.out.println(sdao.simUser(3,1));

		
		
		System.out.print("Predicted rating for user 0 at index 0 is:");
		System.out.println(sdao.userBasedPredict(0,0));
		
		System.out.print("Predicted rating for user 1 at index 1 is:");
		System.out.println(sdao.userBasedPredict(1,1));
		
		System.out.print("Predicted rating for user 2 at index 2 is:");
		System.out.println(sdao.userBasedPredict(2,2));
		
		System.out.print("Predicted rating for user 3 at index 3 is:");
		System.out.println(sdao.userBasedPredict(3,3));
		
		System.out.print("Predicted rating for user 4 at index 4 is:");
		System.out.println(sdao.userBasedPredict(4,4));
		
		System.out.print("Predicted rating for user 0 at index 5 is:");
		System.out.println(sdao.userBasedPredict(0,5));
		
		System.out.print("Predicted rating for user 1 at index 6 is:");
		System.out.println(sdao.userBasedPredict(1,6));
		
		System.out.print("Predicted rating for user 3 at index 8 is:");
		System.out.println(sdao.userBasedPredict(3,8));
		
		System.out.print("Predicted rating for user 2 at index 9 is:");
		System.out.println(sdao.userBasedPredict(2,9));
		
		System.out.print("Predicted rating for user 4 at index 9 is:");
		System.out.println(sdao.userBasedPredict(4,9));
		
		System.out.println("\n\n	-- User Based --\n\n");
		sdao.printUsersSimilarties();
		System.out.println("Predictions:\n");
		sdao.printUserBasedPredictions();
		
		
		System.out.println("\n\n	-- Item Based --\n\n");
		sdao.printItemsSimilarties();
		System.out.println("Predictions:\n");
		sdao.printItemBasedPredictions();

		
		System.out.println("=====================================");

		
	}
	
	public static Matrix convertToMatix(int[][] m){
		double[][] ratings_doubles = new double[m.length][m[0].length];
		for(int i=0; i<m.length; i++) {
			for(int j=0; j<m[i].length; j++){
				ratings_doubles[i][j] = new Double (m[i][j]);
//				System.out.print(ratings_doubles[i][j]+" ");
			}
//			System.out.println();
		}
		return new Matrix(ratings_doubles);
	}
}


