package cn.hnsoft.sudokuGame;

import java.util.ArrayList;

//public class Question {
//	public int[][] getGame() {
////		int[][] sudoku = {  
////                {8, 0, 0, 0, 0, 0, 0, 0, 0},  
////                {0, 0, 3, 6, 0, 0, 0, 0, 0},  
////                {0, 7, 0, 0, 9, 0, 2, 0, 0},  
////                {0, 5, 0, 0, 0, 7, 0, 0, 0},  
////                {0, 0, 0, 0, 4, 5, 7, 0, 0},  
////                {0, 0, 0, 1, 0, 0, 0, 3, 0},  
////                {0, 0, 1, 0, 0, 0, 0, 6, 8},  
////                {0, 0, 8, 5, 0, 0, 0, 1, 0},  
////                {0, 9, 0, 0, 0, 0, 4, 0, 0}};  
//		int[][] sudoku = {  
//              {0, 8, 2, 0, 9, 0, 0, 0, 0},  
//              {0, 0, 0, 0, 0, 0, 0, 0, 5},  
//              {0, 1, 0, 0, 0, 0, 0, 0, 0},  
//              {0, 0, 0, 0, 0, 0, 3, 2, 0},  
//              {7, 0, 0, 5, 0, 6, 0, 0, 0},  
//              {0, 0, 0, 7, 0, 0, 0, 0, 0},  
//              {0, 3, 0, 9, 0, 0, 0, 0, 0},  
//              {0, 0, 0, 0, 2, 0, 0, 8, 0},  
//              {5, 0, 0, 0, 0, 0, 0, 0, 6}};  
//		printArray(sudoku);
//        return sudoku;
//	}
//	
//	private void printArray(int[][] sudoku) {
//		int i, j;
//		System.out.println("Question: ");
//		for (i = 0; i < 9; i++) {
//			for (j = 0; j < 8; j++) {
//				if (j == 0) System.out.print("| ");
//				if (sudoku[i][j] == 0 ) System.out.print( "  | ");
//				else System.out.print(sudoku[i][j] + " | ");
//			}
//			if (sudoku[i][j] == 0 ) System.out.println( "  |");
//			else System.out.println(sudoku[i][j] + " |");
//		}
//	}
//}

import java.util.Random;

import cn.hnsoft.sudokuGame.Objects.AnalyzeData;  

public class Question {  
  
    private Random random = new Random();  
      
    /**运行此程序300次，currentTimes最大值是217，最小值11，平均约等于50 
     * 阈值设置为220， 能满足大部分程序，二维矩阵不会置为0，重新再产生值。 
     */  
    private static final int MAX_CALL_RANDOM_ARRAY_TIMES = 220;  
    
    private static final int boardSize = 9;
    private static final int boxSideSize = (int)Math.sqrt(boardSize);
  
    /**记录当前buildRandomArray()方法调用的次数*/  
    private int currentTimes = 0;  
    
    public int[][] getGame(int level) {
    	int[][] validGrid = generatePuzzleMatrix();
    	System.out.println("valid grid: ");
    	printMatrix(validGrid);
    	
    	int[][] sudokuGame = new int[boardSize][boardSize];
    	boolean boardTooEasy = true;
    	int[][] copyValidGrid = validGrid.clone();
    	
    	while (boardTooEasy) {
    		sudokuGame = digCells(validGrid, level);
    		printMatrix(sudokuGame); // debug
    		DifficultyGrader grader = new DifficultyGrader(boardSize, validGrid);
    		AnalyzeData data = grader.analyzeBoard();
			if(hardEnough(data, level))
				boardTooEasy = false;
			else
				validGrid = copyValidGrid;
    	}
    	
    	System.out.println("question: ");
    	printMatrix(sudokuGame);
    	
    	return sudokuGame;
    }
    
    /*
     * function digHoleByLevel()
     * dig hole in a valid grid to generate question
     */
    private int[][] digCells(int[][] validGrid, int level) {
    	ArrayList<Integer> cells = new ArrayList<Integer>();
		int given = boardSize*boardSize;
		int minGiven = 25;  // TODO: does not support lower than 25!! why??
		if(level == 1){
			minGiven = 50; // TODO: change into 50
		} else if(level == 2){
			minGiven = 30;  // TODO: change into 30
		}
		
		for (int i=0; i < boardSize*boardSize; i++){
			cells.add(i);
		}
    	
    	int randomPosition = 0;
    	int temp = 0, i=0, j=0;
    	int[][] flags = new int[boardSize][boardSize];
    	resetValuesToZeros(flags);
    	Solution sol = new Solution();
    	
    	while (cells.size() > 0 && given > minGiven) {
    		// randomly pick a position to dig
    		randomPosition = random.nextInt(boardSize * boardSize - 1) + 1;
    		i = (randomPosition - 1) / boardSize;
    		j = (randomPosition - 1) % boardSize;
    		
    		// if the position had been visited, pick another one
    		while (flags[i][j] == 1) {
    			randomPosition = random.nextInt(boardSize * boardSize - 1) + 1;
    			i = (randomPosition - 1) / boardSize;
        		j = (randomPosition - 1) % boardSize;
    		}
    		
    		// record current value and then dig a hole
    		temp = validGrid[i][j];
    		validGrid[i][j] = 0;
    	    flags[i][j] = 1;
        	
        	sol.getSolution(validGrid);
        	
        	if (sol.numOfSolution()  != 1) {
        		// if digging this hole cause more than one solution, don't dig this hole
        		validGrid[i][j] = temp;
        	} else {
        		DifficultyGrader grader = new DifficultyGrader(boardSize, validGrid);
        		AnalyzeData data = grader.analyzeBoard();
        		if (data.isFinished() && easyEnough(data, level)){
        			given--;
        		} else {
        			if (grader.boardFinished) System.out.println("aaa");  // debug
        			else System.out.println("bbb");// debug
        			if (easyEnough(data, level)) System.out.println("ccc"); // debug
        			else System.out.println("ddd");// debug
        			validGrid[i][j] = temp;
        		}
        	}
    	}
    	
    	return validGrid;
    }
  
    private int[][] generatePuzzleMatrix() {  

        int[][] randomMatrix = new int[boardSize][boardSize];  
  
        for (int row = 0; row < boardSize; row++) {  
            if (row == 0) {  
            	// the first row, just build a random array and use it
                currentTimes = 0;  
                randomMatrix[row] = buildRandomArray();  
            } else {  
                int[] tempRandomArray = buildRandomArray();  
  
                for (int col = 0; col < boardSize; col++) {  
                    if (currentTimes < MAX_CALL_RANDOM_ARRAY_TIMES) {  
                        if (!isCandidateNmbFound(randomMatrix, tempRandomArray,  
                                row, col)) {  
                              
                            /* 
                             * 将该行的数据置为0，并重新为其准备一维随机数数组 
                             */  
                            resetValuesInRowToZero(randomMatrix,row);  
                            row -= 1;  
                            col = boardSize - 1;  
                            tempRandomArray = buildRandomArray();  
                        }  
                    } else {  
                        /* 
                         * 将二维矩阵中的数值置为0， 
                         * row赋值为-1 col赋值为boardSize - 1， 下一个执行的就是row =0 col=0， 
                         *  
                         * 重头开始 
                         */  
                        row = -1;  
                        col = boardSize - 1;  
                        resetValuesToZeros(randomMatrix);  
                        currentTimes = 0;  
                    }  
                }  
            }  
        }  
//        printArray(randomMatrix);
        return randomMatrix;  
    }  
      
    private void resetValuesInRowToZero(int[][] matrix, int row)  
    {  
        for (int j = 0; j < boardSize; j++) {  
            matrix[row][j] = 0;  
        }  
          
    }  
  
    private void resetValuesToZeros(int[][] matrix) {  
        for (int row = 0; row < boardSize; row++) {  
            for (int col = 0; col < boardSize; col++) {  
                matrix[row][col] = 0;  
            }  
        }  
    }  
  
    private boolean isCandidateNmbFound(int[][] randomMatrix,  
            int[] randomArray, int row, int col) {  
        for (int i = 0; i < randomArray.length; i++) {  
            /** 
             * 试着给randomMatrix[row][col] 赋值,并判断是否合理 
             */  
  
            randomMatrix[row][col] = randomArray[i];  
            if (noConflict(randomMatrix, row, col)) {  
                return true;  
            }  
        }  
        return false;  
    }  
  
    private boolean noConflict(int[][] candidateMatrix, int row, int col) {  
        return noConflictInRow(candidateMatrix, row, col)  
                && noConflictInColumn(candidateMatrix, row, col)  
                && noConflictInBlock(candidateMatrix, row, col);  
    }  
  
    private boolean noConflictInRow(int[][] candidateMatrix, int row, int col) {  
        int currentValue = candidateMatrix[row][col];  
  
        for (int colNum = 0; colNum < col; colNum++) {  
            if (currentValue == candidateMatrix[row][colNum]) {  
                return false;  
            }  
        }  
  
        return true;  
    }  
  
    private boolean noConflictInColumn(int[][] candidateMatrix, int row, int col) {  
  
        int currentValue = candidateMatrix[row][col];  
  
        for (int rowNum = 0; rowNum < row; rowNum++) {  
            if (currentValue == candidateMatrix[rowNum][col]) {  
                return false;  
            }  
        }  
  
        return true;  
    }  
  
    private boolean noConflictInBlock(int[][] candidateMatrix, int row, int col) {  
  
        int baseRow = row / boxSideSize * boxSideSize;  
        int baseCol = col / boxSideSize * boxSideSize;  
  
        for (int rowNum = 0; rowNum < (boardSize - 1); rowNum++) {  
            if (candidateMatrix[baseRow + rowNum / boxSideSize][baseCol + rowNum % boxSideSize] == 0) {  
                continue;  
            }  
            for (int colNum = rowNum + 1; colNum < boardSize; colNum++) {  
                if (candidateMatrix[baseRow + rowNum / boxSideSize][baseCol + rowNum % boxSideSize] == candidateMatrix[baseRow  
                        + colNum / boxSideSize][baseCol + colNum % boxSideSize]) {  
                    return false;  
                }  
            }  
        }  
        return true;  
  
    }  
  
    /** 
     * 返回一个有1到9九个数随机排列的一维数组, 
     */  
    private int[] buildRandomArray() {  
        currentTimes++;  
        int[] array = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };  
        int randomInt = 0;  
        /* 
         * 随机产生一个1到8的随机数，使得该下标的数值与下标为0的数值交换， 
         *  
         *  处理20次，能够获取一个有1到9九个数随机排列的一维数组, 
         */  
        for (int i = 0; i < 20; i++) {  
            randomInt = random.nextInt(8) + 1;  
            int temp = array[0];  
            array[0] = array[randomInt];  
            array[randomInt] = temp;  
        }  
  
        return array;  
    }  
  
    /** 
     * @return the currentTimes 
     */  
    public int getCurrentTimes() {  
        return currentTimes;  
    }  
  
    /** 
     * @param currentTimes the currentTimes to set 
     */  
    public void setCurrentTimes(int currentTimes) {  
        this.currentTimes = currentTimes;  
    }  
    
	private void printMatrix(int[][] sudoku) {

		int i, j;
		for (i = 0; i < boardSize; i++) {
			for (j = 0; j < (boardSize - 1); j++) {
				if (j == 0) System.out.print("| ");
				if (sudoku[i][j] == 0 ) System.out.print( "  | ");
				else System.out.print(sudoku[i][j] + " | ");
			}
			if (sudoku[i][j] == 0 ) System.out.println( "  |");
			else System.out.println(sudoku[i][j] + " |");
		}
	}
	
	private boolean easyEnough(AnalyzeData data, int level) {
		if(data.getLevel() == 1) return true;
		if(data.getLevel() ==  2) return level != 1;
		if(data.getLevel() ==  3) return level != 1  && level != 2;
		if(data.getLevel() ==  4) return level != 1 && level != 2 && level != 3;
		return false;
	}
	
	private boolean hardEnough(AnalyzeData data, int level) {
		if(level == 1) return true;
		if(level == 2) return data.getLevel() != 1;
		if(level == 3) return data.getLevel() != 1 && data.getLevel() != 2;
		if(level == 4) return data.getLevel() != 1 && data.getLevel() != 2 && data.getLevel() != 3;
		return false;
	}
}
