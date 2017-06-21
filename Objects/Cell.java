package cn.hnsoft.sudokuGame.Objects;

import java.util.ArrayList;
import java.util.List;

public class Cell {
	
	int val = 0;
	int cell = 0;
	public int getCell() {
		return cell;
	}
	public void setCell(int cell) {
		this.cell = cell;
	}
	ArrayList<Integer> candidates = new ArrayList<Integer>();
	
	public int getVal() {
		return val;
	}
	public void setVal(int val) {
		this.val = val;
	}
	public ArrayList<Integer> getCandidates() {
		return candidates;
	}
	public void setCandidates(ArrayList<Integer> candidates) {
		this.candidates = candidates;
	}	
	
}
