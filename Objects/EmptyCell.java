package cn.hnsoft.sudokuGame.Objects;

public class EmptyCell {
	int housesIdx_1 = 0;
	int housesIdx_2 = 0;
	int boardIdx = 0;
	public int getHousesIdx_1() {
		return housesIdx_1;
	}
	public void setHousesIdx_1(int housesIdx_1) {
		this.housesIdx_1 = housesIdx_1;
	}
	public int getHousesIdx_2() {
		return housesIdx_2;
	}
	public void setHousesIdx_2(int housesIdx_2) {
		this.housesIdx_2 = housesIdx_2;
	}
	public int getBoardIdx() {
		return boardIdx;
	}
	public void setBoardIdx(int boardIdx) {
		this.boardIdx = boardIdx;
	}
}
