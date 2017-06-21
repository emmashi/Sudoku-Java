package cn.hnsoft.sudokuGame;

public class testGame {
	public static void main(String[] args) {
		/*
	     * get the question 2D array
	     */  
		long a1 = System.currentTimeMillis();
		
		Question question = new Question();
		int level = 2;  // 1 or 2 or 3 or 4
		int[][] sudoku = question.getGame(level);
		
		long a2 = System.currentTimeMillis();
		System.out.println("Time generating guestion: " + (a2-a1) + "ms");
		
		/*
	     * get solution to the above question
	     */  
		Solution solution = new Solution();
		solution.getSolution(sudoku);
		System.out.println("solution number: " + solution.numOfSolution());
		
		long a3 = System.currentTimeMillis();
		System.out.println("Time solving sudoku: " + (a3-a2) + "ms");
	}
}
