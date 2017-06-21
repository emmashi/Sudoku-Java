package cn.hnsoft.sudokuGame;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.hnsoft.sudokuGame.Objects.AnalyzeData;
import cn.hnsoft.sudokuGame.Objects.Cell;
import cn.hnsoft.sudokuGame.Objects.EmptyCell;

public class DifficultyGrader {
	private int boardSize = 0;
	
	private int[][] matrix = new int[boardSize][boardSize];
	
	private int[] usedStrategies = null; // array of freq that strategy i has been used
	
	private static final String[] strategies = {"openSingles", "visualElimination", "singleCandidate", "nakedPair", 
									"pointingElimination", "hiddenPair", "nakedTriplet", "hiddenTriplet",
									"nakedQuad", "hiddenQuad"};
	
	private static final double[] strategyScore = {0.1, 8, 9, 50, 80, 90, 100, 140, 150, 280};
	
	//indexes of cells in each house - generated on the fly based on boardSize
	private ArrayList<ArrayList<ArrayList<Integer>>>houses = null; // hor.rows, ver.rows, boxes
	
	private ArrayList<Cell> board = new ArrayList<Cell>();
	
	private ArrayList<Integer> boardNumbers = new ArrayList<Integer>();
	
	public boolean boardError = false;
	
	public boolean boardFinished = false;
	
	private boolean onlyUpdatedCandidates = false;
	
	public DifficultyGrader(int size, int[][] matrix) {
		this.boardSize = size;
		this.matrix = matrix;
		usedStrategies = new int[10];
		for (int i = 0; i < 10; i++) {
			usedStrategies[i] = 0;
		}
		houses = new ArrayList<ArrayList<ArrayList<Integer>>>();
		
		// initialize boardNumbers
		for (int i=0; i < boardSize; i++){
			boardNumbers.add(i+1);
		}
		
		// initialize houses
		generateHouseIndexList();
		
		// initialize board ArrayList
		for (int i = 0; i < boardSize * boardSize; i++) {
			Cell cell = new Cell();
			cell.setVal(matrix[i / boardSize][i % boardSize]);
			cell.setCandidates(cell.getVal() == 0 ? (ArrayList<Integer>)boardNumbers.clone() : getZeroCandidatesList());
			board.add(cell);
		}
	}

	public AnalyzeData analyzeBoard() {
		int[] usedStrategiesClone = usedStrategies.clone();
		ArrayList<Cell> boardClone = (ArrayList<Cell>)board.clone();
		boolean canContinue = true;
		
		while(canContinue) {
			int startStrat = onlyUpdatedCandidates ? 2 : 0;;
			canContinue = solveFn(startStrat);
		}
		
		AnalyzeData data = new AnalyzeData();
		if (boardError) {
			data.setError(true);
		} else {
			data.setFinished(boardFinished);
			ArrayList<Integer> usedStratList = new ArrayList<Integer>(); 
			
			for (int i = 0; i < usedStrategies.length; i++) {
				usedStratList.add(usedStrategies[i]);
			}
			
			data.setUsedStrategies(usedStratList);
			
			if (boardFinished) {
				int temp_level = calcBoardDifficulty(usedStrategies);
				System.out.println("temp_level" + temp_level);
				data.setLevel(temp_level);
			}
		}
		
		resetBoardVariables();
		usedStrategies  = usedStrategiesClone;
		board = boardClone;
		return data;
	}

	
	private void resetBoardVariables() {
		boardFinished = false;
		boardError = false;
		onlyUpdatedCandidates = false;  
		for (int i = 0; i < 10; i++) {
			usedStrategies[i] = 0;
		}
	}
	
	/* solveFn
	 * --------------
	 *  applies strategy i (where i represents strategy, ordered by simplicity
	 *  -if strategy fails (too advanced a sudoku) AND an more advanced strategy exists:
	 *		calls itself with i++
	 *  returns canContinue true|false - only relevant for solveMode "all"
	 */
//	private int nrSolveLoops = 0;
//	private boolean effectedCells = false;
	
	private boolean solveFn(int i) {
		if (boardFinished) {
			return false; // !canContinue
		}
		
//		nrSolveLoops++;
		String strat = strategies[i];
		int returnVal = 0;
		if (strat == "openSingles") returnVal = openSingles();
		else if (strat == "visualElimination") returnVal = visualElimination();
		else if (strat == "singleCandidate") returnVal = singleCandidate();
		else if (strat == "nakedPair") returnVal = nakedPair();
		else if (strat == "pointingElimination") returnVal = pointingElimination();
		else if (strat == "hiddenPair") returnVal = hiddenPair();
		else if (strat == "nakedTriplet") returnVal = nakedTriplet();
		else if (strat == "hiddenTriplet") returnVal = hiddenTriplet();
		else if (strat == "nakedQuad") returnVal = nakedQuad();
		else if (strat == "hiddenQuad") returnVal = hiddenQuad();
		
		if (returnVal == -2) {
			if (i + 1 < 10) return solveFn(i+1);
			else return false; // no more strategies
		} else if (boardError || boardFinished){
			return false; 
		}
		
		//we got an answer, using strategy startStrat
		usedStrategies[i] += 1;
		return true; // canContinue
	}
	
	/* calcBoardDifficulty
	 * Return difficulty level: 1 / 2 / 3 / 4
	 * --------------
	 *  TYPE: solely based on strategies required to solve board (i.e. single count per strategy)
	 *  SCORE: distinguish between boards of same difficulty.. based on point system. Needs work.
	 * -----------------------------------------------------------------*/
	private int calcBoardDifficulty(int[] usedStrategies) {
		int boardDiff = 0;
		int numStratsUsed = 0;
		for (int i = 0; i < usedStrategies.length; i++) {
			if (usedStrategies[i] != 0) {
				numStratsUsed += 1;
			}
		}
		if(numStratsUsed < 3) // TODO: changed into 3
			boardDiff = 1; // DIFFICULTY_EASY
		else if(numStratsUsed < 4) // TODO: changed into 4
			boardDiff = 2; // DIFFICULTY_MEDIUM
		else
			boardDiff = 3; // DIFFICULTY_HARD

		int totalScore = 0;
		for(int i=0; i < strategies.length; i++){
			int freq = usedStrategies[i];
			
			if(freq == 0) continue; // 0, won't effect score
			
			double stratScore = strategyScore[i];
			totalScore += freq * stratScore;
		}
		
		if(totalScore > 750)
		// if(totalScore > 2200)
			boardDiff = 4; // DIFFICULTY_VERY_HARD

		return boardDiff;
	}
	
	private void generateHouseIndexList() {
        // reset houses
        houses.clear();
        ArrayList<ArrayList<Integer>> hrowHouse = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> vrowHouse = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> boxHouse = new ArrayList<ArrayList<Integer>>();
		int boxSideSize = (int)Math.sqrt(boardSize);

		for(int i=0; i < boardSize; i++){
			ArrayList<Integer> hrow = new ArrayList<Integer>(); //horisontal row
			ArrayList<Integer> vrow = new ArrayList<Integer>(); //vertical row
			ArrayList<Integer> box = new ArrayList<Integer>();
			
			for(int j=0; j < boardSize; j++){
					hrow.add(boardSize*i + j);
					vrow.add(boardSize*j + i);

					if(j < boxSideSize){
						for(int k=0; k < boxSideSize; k++){
							//0, 0,0, 27, 27,27, 54, 54, 54 for a standard sudoku
							int a = (int)Math.floor(i/boxSideSize) * boardSize * boxSideSize;
							//[0-2] for a standard sudoku
							int b = (int)(i%boxSideSize) * boxSideSize;
							int boxStartIndex = a + b; //0 3 6 27 30 33 54 57 60
							
							box.add(boxStartIndex + boardSize*j + k);
						}
					}
				}
			if (houses.size() == 0) {
				hrowHouse.add(hrow);
				vrowHouse.add(vrow);
				boxHouse.add(box);
				
				houses.add(hrowHouse);
				houses.add(vrowHouse);
				houses.add(boxHouse);
			} else {
				houses.get(0).add(hrow);
				houses.get(1).add(vrow);
				houses.get(2).add(box);
			}	
		}
	}
	
	
	/* openSingles  score: 0.1
	 * --------------
	 *  checks for houses with just one empty cell - fills it in board variable if so
	 * -- returns effectedCells - the updated cell(s), or false
	 * -----------------------------------------------------------------*/
	private int openSingles(){

		//for each type of house..(hor row / vert row / box)
		int hlength = houses.size();
		for(int i=0; i < hlength; i++){

			//for each such house
			int housesCompleted = 0; //if goes up to 9, sudoku is finished

			for(int j=0; j < boardSize; j++){
				ArrayList<EmptyCell> emptyCells = new ArrayList<EmptyCell>();

				// for each cell..
				for (int k=0; k < boardSize; k++){
//					System.out.println(houses.get(i).get(j));  // debug
//					System.out.println(houses.get(i).get(j).get(k));  // debug
					
					int boardIndex = (int)(houses.get(i).get(j).get(k));
					
//					System.out.println(board.get(boardIndex).getVal());  // debug
					if(board.get(boardIndex).getVal() == 0) {
						EmptyCell cell = new EmptyCell();
						cell.setHousesIdx_1(i);
						cell.setHousesIdx_2(j);
						cell.setBoardIdx(boardIndex);
						emptyCells.add(cell);
						if(emptyCells.size() > 1) {
							//log("more than one empty cell, house area :["+i+"]["+j+"]");
							break;
						}
					}
				}
				//one empty cell found
				if(emptyCells.size() == 1){
					EmptyCell emptyCell = emptyCells.get(0);
					//grab number to fill in in cell
					ArrayList<Integer> val = numbersLeft((houses.get(emptyCell.getHousesIdx_1()).get(emptyCell.getHousesIdx_2())));
					if(val.size() > 1) {
						boardError = true; //to force solve all loop to stop
						return -1; //error
					}

					setBoardCell(emptyCell.getBoardIdx(), val.get(0));

					return emptyCell.getBoardIdx();
				}
				//no empty cells..
				if(emptyCells.size() == 0) {
					housesCompleted++;
					if(housesCompleted == boardSize){
						boardFinished = true;
						return -1; //special case, done
					}
				}
			}
		}
		return -2;
	}
	
	/* visualElimination  score: 8
	 * --------------
	 * Looks for houses where a digit only appears in one slot
	 * -meaning we know the digit goes in that slot.
	 * -- returns effectedCells - the updated cell(s), or false
	 * -----------------------------------------------------------------*/
	private int visualElimination(){
		int hlength = houses.size();
		for(int i=0; i < hlength; i++){

			//for each such house
			for(int j=0; j < boardSize; j++){
				ArrayList<Integer> house = houses.get(i).get(j);
				ArrayList<Integer> digits = numbersLeft(house);

				//for each digit left for that house
				for (int k=0; k < digits.size(); k++){
					int digit = digits.get(k);
					ArrayList<Integer> possibleCells = new ArrayList<Integer>();

					//for each cell in house
					for(int l=0; l < boardSize; l++){
						int cell = house.get(l);
						Cell boardCell = board.get(cell);
						//if the digit only appears as a candidate in one slot, that's where it has to go
						if (boardCell.getCandidates().contains(digit)){
							possibleCells.add(cell);
							if(possibleCells.size() > 1)
								break; //no we can't tell anything in this case
						}
					}

					if(possibleCells.size() == 1){
						int cellIndex = possibleCells.get(0);

						setBoardCell(cellIndex, digit); //does not update UI

						onlyUpdatedCandidates = false;
						return cellIndex; //one step at the time
					}
				}

			}
		}
		return -2;
	}
	
	
	/* singleCandidate  score: 9
	 * --------------
	 * Looks for cells with only one candidate
	 * -- returns effectedCells - the updated cell(s), or false
	 * -----------------------------------------------------------------*/
	private int singleCandidate(){
		
		visualEliminationOfCandidates();
		
		//for each cell
		for(int i=0; i < board.size(); i++){
			Cell cell = board.get(i);
			ArrayList<Integer> candidates = cell.getCandidates();

			//for each candidate for that cell
			ArrayList<Integer> possibleCandidates = new ArrayList<Integer>();
			for (int j=0; j < candidates.size(); j++){
				if (candidates.get(j) != 0)
					possibleCandidates.add(candidates.get(j));
				if(possibleCandidates.size() >1)
					break; //can't find answer here
			}
			if(possibleCandidates.size() == 1){
				int digit = possibleCandidates.get(0);

				setBoardCell(i, digit); 

				return i; //one step at the time
			}
		}
		return -2;
	}
	
	
	/* nakedPair  score: 50
	 * --------------
	 * see nakedCandidateElimination for explanation
	 * -- returns effectedCells - the updated cell(s), or -2
	 * -----------------------------------------------------------------*/
	private int nakedPair(){
		return nakedCandidates(2);
	}
	
	/* pointingElimination  score: 80
	 * --------------
	 * if candidates of a type (digit) in a box only appar on one row, all other
	 * same type candidates can be removed from that row
	 ------------OR--------------
	 * same as above, but row instead of box, and vice versa.
	 * -- returns effectedCells - the updated cell(s), or false
	 * -----------------------------------------------------------------*/
	// TODO
	private int pointingElimination(){
		return -2;
	}
	
	/* hiddenPair  score: 90
	 */
	private int hiddenPair(){
		return hiddenLockedCandidates(2);
	}
	
	/* nakedTriplet  score: 100
	 */
	private int nakedTriplet(){
		return nakedCandidates(3);
	}
	
	/* hiddenTriplet  score: 140
	 */
	private int hiddenTriplet(){
		return hiddenLockedCandidates(3);
	}
	
	/* nakedQuad  score: 150
	 */
	private int nakedQuad(){
		return nakedCandidates(4);
	}
	
	/* hiddenQuad  score: 280
	 */
	private int hiddenQuad(){
		return hiddenLockedCandidates(4);
	}
	
	
	/* nakedCandidates
	 * --------------
	 * looks for n nr of cells in house, which together has exactly n unique candidates.
		this means these candidates will go into these cells, and can be removed elsewhere in house.
	 *
	 * -- returns effectedCells - the updated cell(s), or false
	 * -----------------------------------------------------------------*/
	// TODO
	private int nakedCandidates(int n) {
//		//for each type of house..(hor row / vert row / box)
//		int hlength = houses.size();
//		for(int i=0; i < hlength; i++){
//
//			//for each such house
//			for(int j=0; j < boardSize; j++){
//				
//				ArrayList<Integer> house = houses.get(i).get(j);
//				if(numbersLeft(house).size() <= n) continue;//can't eliminate any candidates
//					
//				ArrayList<Cell> combineInfo = new ArrayList<Cell>(); //{cell: x, candidates: []}, {} ..
//				//combinedCandidates,cellsWithCandidate;
//				ArrayList<Integer> minIndexes = new ArrayList<Integer>();
//				minIndexes.add(-1);
//
//				//checks every combo of n candidates in house, returns pattern, or false
//				ArrayList<Integer> result = checkCombinedCandidates(house, 0, combineInfo, minIndexes, n);
//				if(result != null)
//					return result;
//			}
//		}
		return -2; //pattern not found
	}
	
	/* hiddenLockedCandidates
	 * --------------
	 * looks for n nr of cells in house, which together has exactly n unique candidates.
		this means these candidates will go into these cells, and can be removed elsewhere in house.
	 *
	 * -- returns effectedCells - the updated cell(s), or false
	 * -----------------------------------------------------------------*/
	// TODO
	private int hiddenLockedCandidates(int i) {
		return -1;
	}
	
	// TODO
	private int checkCombinedCandidates(ArrayList<Integer> house, int startIndex, ArrayList<Cell> combineInfo, ArrayList<Integer> minIndexes, int n){
//		for(int i=Math.max(startIndex, minIndexes.get(startIndex)); i < boardSize-n+startIndex; i++){
//
//			//never check this cell again, in this loop
//			minIndexes.set(startIndex, i+1);
//			//or in a this loop deeper down in recursions
//			minIndexes.set(startIndex+1, i+1);
//
//			int cell = house.get(i);
//			ArrayList<Integer> cellCandidates = candidatesLeft(cell);
//
//			if(cellCandidates.size() == 0 || cellCandidates.size() > n)
//				continue;
//
//			//try adding this cell and it's cellCandidates,
//			//but first need to check that that doesn't make (unique) amount of
//			//candidates in combineInfo > n
//
//			//if this is the first item we add, we don't need this check (above one is enough)
//			if(combineInfo.size() > 0){
//				ArrayList<Integer> temp = (ArrayList<Integer>)cellCandidates.clone();
//				for(int a =0; a < combineInfo.size(); a++){
//					ArrayList<Integer> candidates = combineInfo.get(i).getCandidates();
//					for(int b=0; b < candidates.size(); b++){
//						if(!temp.contains(candidates.get(b)))
//							temp.add(candidates.get(b));
//					}
//				}
//				if(temp.size() > n){
//					continue; //combined candidates spread over > n cells, won't work
//				}
//
//			}
//
//			Cell someobj = new someObject();
//			someobj.set();
//			someobj.set();
//			combineInfo.add(somobj});
//
//
//			if(startIndex < n-1) {
//				//still need to go deeper into combo
//				var r = checkCombinedCandidates(house, startIndex+1, combineInfo, minIndex, n);
//				//when we come back, check if that's because we found answer.
//				//if so, return with it, otherwise, keep looking
//				if (r != null)
//					return r;
//			}
//
//			//check if we match our pattern
//			//if we have managed to combine n-1 cells,
//			//(we already know that combinedCandidates is > n)
//			//then we found a match!
//			if(combineInfo.size() == n){
//				//now we need to check whether this eliminates any candidates
//
//
//				//now we need to check whether this eliminates any candidates
//
//				ArrayList<Integer> cellsWithCandidates = new ArrayList<Integer>();
//				var combinedCandidates = []; //not unique either..
//				for(int x=0; x< combineInfo.size();x++){
//					cellsWithCandidates.add(combineInfo.get(x).cell);
//					combinedCandidates = combinedCandidates.concat(combineInfo[x].candidates);
//				}
//
//				//get all cells in house EXCEPT cellsWithCandidates
//				ArrayList<Integer> cellsEffected = new ArrayList<Integer>();
//				for (int y=0; y< boardSize; y++){
//					if(!cellsWithCandidates.contains(house.get(y))) {
//						cellsEffected.add(house.get(y));
//					}
//				}
//
//				//remove all candidates on house, except the on cells matched in pattern
//				ArrayList<Integer> cellsUpdated = removeCandidatesFromCells(cellsEffected, combinedCandidates);
//
//				//if it does remove candidates, we're succeded!
//				if(cellsUpdated.size() > 0){
//
////					onlyUpdatedCandidates = true;
//
//					//return cells we actually update, duplicates removed
//					return uniqueArray(cellsUpdated);
//				}
//		}
//			
//		if(startIndex > 0) {
//			//if we added a value to our combo check, but failed to find pattern, we now need drop that value and go back up in chain and continue to check..
//			if(combineInfo.size() > startIndex-1){
//				//log("nakedCans: need to pop last added values..");
//				combineInfo.remove(combineInfo.size() - 1);
//			}
//		}
//		return false;
		return -2;
	}
	
	
	/* visualEliminationOfCandidates 
	 * --------------
	 * ALWAYS returns -2
	 * -- special compared to other strats: doesn't step - updates whole board,
	 in one go. Since it also only updates candidates, we can skip straight to next strat, since we know that neither this one nor the one(s) before (that only look at actual numbers on board), will find anything new.
	 * -----------------------------------------------------------------*/
	private int visualEliminationOfCandidates(){
		int hlength = houses.size();
		for(int i=0; i < hlength; i++){

			//for each such house
			for(int j=0; j < boardSize; j++){
				ArrayList<Integer> house = houses.get(i).get(j);
				ArrayList<Integer> candidatesToRemove = numbersTaken(house);

				// for each cell..
				for (int k=0; k < boardSize; k++){
					int cell = house.get(k);
					removeCandidatesFromCell(cell, candidatesToRemove);
				}
			}
		}
		return -2;
	}
	
	
	private void removeCandidatesFromCell(int cell, ArrayList<Integer> candidates) {
		Cell boardCell = board.get(cell);
		ArrayList<Integer> c = boardCell.getCandidates();
		
		for(int i = 0; i < candidates.size(); i++){
			int temp = candidates.get(i);
			if (temp == 0) continue;
			//-1 because candidate '1' is at index 0 etc.
			else if(c.get(temp - 1) != 0) {
				c.set(temp - 1, 0); //writes to board variable
			}
		}
		
		// TODO: Are these two steps necessary??????
		boardCell.setCandidates(c);
		board.set(cell, boardCell);
	}
	
	private ArrayList<Integer> numbersTaken(ArrayList<Integer> house) {
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		
		for (int i = 0; i < house.size(); i++) {
			int val = board.get(house.get(i)).getVal();
			if (val != 0) {
				numbers.add(val);
			}
		}
		return numbers;
	}
	
	private ArrayList<Integer> numbersLeft(ArrayList<Integer> house) {
		ArrayList<Integer>numbers = (ArrayList<Integer>)boardNumbers.clone();
		
		for(int i=0; i < house.size(); i++){
			for(int j=0; j < numbers.size(); j++){
				//remove all numbers that are already being used
				if(numbers.get(j) == board.get(house.get(i)).getVal())
					numbers.remove(j);
			}
		}
		//return remaining numbers
		return numbers;
	};
	
	private void setBoardCell(int idx, int val) {
		board.get(idx).setVal(val); // TODO: can update board successfully??????
		if (val != 0) {
			board.get(idx).setCandidates(getZeroCandidatesList());
		}
	}
	
	private ArrayList<Integer> candidatesLeft(int cellIdx) {
		ArrayList<Integer> t = new ArrayList<Integer>();
		ArrayList<Integer> candidates = board.get(cellIdx).getCandidates();
		for (int i= 0; i < candidates.size(); i++) {
			if (candidates.get(i) != 0) {
				t.add(candidates.get(i));
			}
		}
		return t;
	}
	
	private ArrayList<Integer> getZeroCandidatesList() {
		ArrayList<Integer> zeroCandidatesList = new ArrayList<Integer>();
		for (int i = 0; i < boardSize; i++) {
			zeroCandidatesList.add(0);
		}
		return zeroCandidatesList;
	}

}
