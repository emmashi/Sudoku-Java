package cn.hnsoft.sudokuGame;

/**
 * Find solution of a given sudoku game
 */
public class Solution {
	private static final int boardSize = 9;
	
	private static final int boxSideSize = (int)Math.sqrt(boardSize);
	
	private int[][] matrix = new int[boardSize][boardSize];
	
	private int numSol = -1;
	
	// the only public function int this class, use this to find solution
	public int[][] getSolution(int[][] sudoku) {
		matrix = sudoku;
		numSol = 0;
        backTrace(0, 0);  
		
		return matrix;
	}
	
	private void backTrace(int i, int j) {
		
		// got a possible solution, print it out
		if (i == (boardSize - 1) && j == boardSize) {
			numSol += 1;
            printMatrix();  
            return;
		}
		
		// once reach the end of a line, begin with the next line
		if (j == boardSize) {
			i++; j = 0;
		}
		
		// if this position already had a number, go on with next position
		if (matrix[i][j] != 0) {
			backTrace(i, j + 1);
		}
		
		// it's an empty position, try every possible number
		else {
			for (int k = 1; k <= boardSize; k++) {
				if (valid(i, j, k)) {
					matrix[i][j] = k;
					backTrace(i, j + 1);
					matrix[i][j] = 0;
				}
			}
		}
	}
	
	private boolean valid(int row, int col, int value) {
		
		// row hit or col hit, invalid
		for (int i = 0; i < boardSize; i++) {
			if (matrix[i][col] == value || matrix[row][i] == value) {
				return false;
			}
		}
		
		// block hit, invalid
		int blockRow = row / boxSideSize;
		int blockCol = col / boxSideSize;	
		for (int  i = 0; i < boxSideSize; i++) {
			for (int j = 0; j < boxSideSize; j++) {
				if (matrix[blockRow * boxSideSize + i][blockCol * boxSideSize + j] == value) {
					return false;
				}
			}
		}
		
		return true; // no hit, valid
	}
	
	private void printMatrix() {
		System.out.println("Solution: ");  
		int i, j;
		for (i = 0; i < boardSize; i++) {
			for (j = 0; j < (boardSize - 1); j++) {
				if (j == 0) System.out.print("| ");
				System.out.print(matrix[i][j] + " | ");
			}
			System.out.println(matrix[i][j] + " |");
		}
	}

	public int numOfSolution() {
		return numSol;
	}
}
