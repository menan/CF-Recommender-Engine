package edu.carleton.comp4601.cf.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class SimpleDataAccessObject {
	
	File file;
	private int[][] ratings;
	private String[] items;
	private String[] users;
	
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
		int average = 0;
		for(int rating: ratings[user])
			average  += rating;
		
		return average/ratings[user].length;
	}
	public double averageItem(int item){
		int average = 0;

		for(int i = 0; i < users.length; i++){
			int rating = ratings[i][item];
			average  += rating;
		}
		return average/users.length;
	}

	public double simUser(int userA, int userB){
		double sim = 0;
		double sumDot = 0;
		double absA = 0;
		double absB = 0;
		for(int i = 0; i < ratings[userA].length; i++){
			int ratingA = ratings[userA][i];
			int ratingB = ratings[userB][i];
//			System.out.println(ratingA + " vs " + ratingB);
			if (ratingA > 0 && ratingB > 0 ){
				sumDot += ((ratingA - averageRating(userA)) * (ratingB - averageRating(userB)));

				absA += ((ratingA - averageRating(userA)) * (ratingA - averageRating(userA)));
				absB += ((ratingB - averageRating(userB)) * ((ratingB - averageRating(userB))));
				
			}
		}
		absA = Math.sqrt(absA);
		absB = Math.sqrt(absB);
		
		sim = sumDot / (absA * absB);
		
//		System.out.println("similarity of userA and B is " + sim);
		
		
		
		return sim;
	}
	public double simItem(int itemA, int itemB){
		double sim = 0;
		double sumDot = 0;
		double absA = 0;
		double absB = 0;
		for(int i = 0; i < users.length; i++){
			int ratingA = ratings[i][itemA];
			int ratingB = ratings[i][itemB];
//			System.out.println(ratingA + " vs " + ratingB);
			if (ratingA > 0 && ratingB > 0 ){
				sumDot += ((ratingA - averageItem(itemA)) * (ratingB - averageItem(itemB)));

				absA += ((ratingA - averageItem(itemA)) * (ratingA - averageItem(itemA)));
				absB += ((ratingB - averageItem(itemB)) * ((ratingB - averageItem(itemB))));
				
			}
		}
		absA = Math.sqrt(absA);
		absB = Math.sqrt(absB);
		
		sim = sumDot / (absA * absB);
		
//		System.out.println("similarity of Item A and B is " + sim);
		
		
		
		return sim;
	}
	
	public double predict(int user, int item){
		int sum = 0;
		double sumWeight = 0;

		for(int i = 0; i < ratings[user].length; i++){
			int rating = ratings[user][i];
			double weight = simItem(item, i);
			sum += (rating * weight);
			sumWeight += weight;
		}
		return sum/sumWeight;	
		
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

	public static void main(String[] args) throws FileNotFoundException {
		SimpleDataAccessObject sdao = new SimpleDataAccessObject(new File("test.txt"));
		sdao.input();
		System.out.println(sdao);

		System.out.print("Similarity between user 3 and 4 is:");
		System.out.println(sdao.simUser(3,4));
		
		System.out.print("Similarity between item 3 and 4 is:");
		System.out.println(sdao.simItem(3,4));

		System.out.print("Predicted rating for user 0 at index 4 is:");
		System.out.println(sdao.predict(0,4));
		System.out.println("=====================================");
		
		
		sdao = new SimpleDataAccessObject(new File("test2.txt"));
		sdao.input();
		System.out.println(sdao);

		System.out.print("Similarity between user 1 and 4 is:");
		System.out.println(sdao.simUser(1,4));
		
		System.out.print("Similarity between item 2 and 3 is:");
		System.out.println(sdao.simItem(2,3));

		System.out.print("Predicted rating for user 0 at index 0 is:");
		System.out.println(sdao.predict(0,0));
		
		System.out.print("Predicted rating for user 1 at index 1 is:");
		System.out.println(sdao.predict(1,1));
		
		System.out.print("Predicted rating for user 2 at index 2 is:");
		System.out.println(sdao.predict(2,2));
		
		System.out.print("Predicted rating for user 3 at index 3 is:");
		System.out.println(sdao.predict(3,3));
		
		System.out.print("Predicted rating for user 4 at index 4 is:");
		System.out.println(sdao.predict(4,4));

		
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
		System.out.println(sdao.predict(0,0));
		
		System.out.print("Predicted rating for user 1 at index 1 is:");
		System.out.println(sdao.predict(1,1));
		
		System.out.print("Predicted rating for user 2 at index 2 is:");
		System.out.println(sdao.predict(2,2));
		
		System.out.print("Predicted rating for user 3 at index 3 is:");
		System.out.println(sdao.predict(3,3));
		
		System.out.print("Predicted rating for user 4 at index 4 is:");
		System.out.println(sdao.predict(4,4));
		
		System.out.print("Predicted rating for user 0 at index 5 is:");
		System.out.println(sdao.predict(0,5));
		
		System.out.print("Predicted rating for user 1 at index 6 is:");
		System.out.println(sdao.predict(1,6));
		
		System.out.print("Predicted rating for user 3 at index 8 is:");
		System.out.println(sdao.predict(3,8));
		
		System.out.print("Predicted rating for user 2 at index 9 is:");
		System.out.println(sdao.predict(2,9));
		
		System.out.print("Predicted rating for user 4 at index 9 is:");
		System.out.println(sdao.predict(4,9));
		System.out.println("=====================================");
		
		
	}
}


