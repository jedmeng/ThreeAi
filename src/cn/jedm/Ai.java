package cn.jedm;


import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

class Ai {
	static int searchLevel = 6;
	private Grid grid;
	Direction bestDirection = null;
	public static Direction run(int[][] data) throws GameOverException {
		Ai ai = new Ai(new Grid(data));
		Logger.info("At first:\n");
		Logger.info(ai.getGrid().print());
		long startTime = new Date().getTime();
		ai.search(searchLevel, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
		Logger.info("cost: " + (new Date().getTime() - startTime) + "\n\n");
		if (ai.isGameOver()) {
			Logger.info("Game Over!\n\nYour score is : " + ai.getGrid().getScore());
			throw new GameOverException();
		}
		Logger.info("Move " + ai.bestDirection + "\n\n");
		return ai.bestDirection;
	}

	public Ai(Grid grid) {
		this.grid = grid;
	}

	public Ai(Ai ai) {
		grid = new Grid(ai.getGrid());
	}

	public double eval() {

		double smoothWeight         = 0.2,
				monotonicityWeight  = 1.0,
				emptyWeight         = 0.4,//2.7
				maxWeight           = 0.5,
				scoreWeight         = 1.0;

		Logger.debug(
				//"smooth: \t\t" + getSmoothness() + " * " + smoothWeight + "\n" +
				//"monotonicity: \t" + getMonotonicity() + " * " + monotonicityWeight + "\n" +
				"empty: \t\t\t" + getEmptyCellNum() + //" * " + emptyWeight + "\n" +
				"score: \t\t\t" + getScore() + //" * " + emptyWeight + "\n" +
				//"score2: \t\t\t" + getValueSum() + //" * " + emptyWeight + "\n" +
				//"max: \t\t\t" + getMaxValue() + " * " + maxWeight + "\n"
				"");


		return /*getSmoothness() * smoothWeight +
				getMonotonicity() * monotonicityWeight +
				getEmptyCellNum() * emptyWeight +
				getMaxValue() * maxWeight;*/
				getScore() + getEmptyCellNum();
	}

	public Grid getGrid() {
		return grid;
	}

	/**
	 * 获取孤岛数目
	 * 所有不可合并的单元认为一个孤岛
	 */
	private int getIslandNum() {
		final HashSet<Tile> visit = new HashSet<Tile>();
		int island = 0;

		DeepSearchAble object = new DeepSearchAble() {
			@Override
			public void deepSearch(Tile tile) {
				if (tile != null && !visit.contains(tile)) {
					visit.add(tile);
					for (Direction direction : Direction.values()) {
						Tile siblingTile = grid.getTile(tile.getCell().getLocation().neighbor(direction));
						deepSearch(siblingTile);
					}
				}
			}
		};

		for (Tile tile : grid.getTiles()) {
			if (!visit.contains(tile)) {
				island++;
				object.deepSearch(tile);
			}
		}

		return island;
	}

	/**
	 * 获取最大值对3的对数
	 */
	private double getMaxValue() {
		return Math.pow(getLogValue(grid.getMax().getValue() - 1), 1.4);
	}

	/**
	 * 获取最大值对3的对数
	 */
	private double getScore() {
		return Math.log(grid.getScore())/Math.log(2);
	}

	private double getValueSum() {
		return grid.getValueSum()/2;
	}

	/**
	 * 获取平滑程度
	 */
	private int getSmoothness() {
		int smoothness = 0;
		Direction[] directions = {Direction.RIGHT, Direction.DOWN};
		for (Tile tile : grid.getTiles()) {
			for (Direction direction : directions) {
				Tile neighbourTile = grid.getTile(tile.getCell().getLocation().neighbor(direction));

				if (neighbourTile == null) {
					smoothness -= 1;
				} else {
					smoothness -= Math.abs(getLogDiff(tile, neighbourTile));
				}

			}
		}
		return smoothness;
	}

	/**
	 * 获取单调性
	 */
	private int getMonotonicity() {
		int up, down, left, right;
		up = down = left = right = 0;

		for (int x=0; x<Grid.height; x++) {
			for (int y=0; y<Grid.width; y++) {
				Tile current = grid.getTile(Location.getLocation(x, y));

				if (y < Grid.width - 1) {
					Tile next = grid.getTile(Location.getLocation(x, y+1));
					int diff = getLogDiff(current, next);
					if (diff > 0) {
						right += diff;
					} else {
						left -= diff;
					}
				}

				if (x < Grid.height - 1) {
					Tile next = grid.getTile(Location.getLocation(x+1, y));
					int diff = getLogDiff(current, next);
					if (diff > 0) {
						up += diff;
					} else {
						down -= diff;
					}
				}
			}
		}

		return Math.max(up, down) + Math.max(left, right);
	}

	/**
	 * 获取剩余格数的对数
	 */
	private double getEmptyCellNum() {
		int num = Grid.size - grid.count();
		return Math.log(num + 1)/Math.log(12)*5;
	}

	private boolean isGameOver() {
		return !grid.isMovable();
	}

	private int getLogValue(Tile tile) {
		return getLogValue(tile == null ? 0 : tile.getValue());
	}

	private int getLogValue(int value) {
		if (value == 0) {
			return 0;
		}
		if (value == 1 || value == 2) {
			return 1;
		}
		return (int)(Math.log(value / 3) / Math.log(2) + 2);
	}

	public void move(Direction direction) {
		grid.move(direction);
	}

	private int getLogDiff(Tile a, Tile b) {
		int aValue = a == null ? 0 : a.getValue(),
			bValue = b == null ? 0 : b.getValue();
		return getLogDiff(aValue, bValue);
	}

	private int getLogDiff(int a, int b) {
		int diff = getLogValue(a) - getLogValue(b);
		if (diff == 0 && a == b && (a == 1 || a == 2)) {
			diff = -1;
		}
		return diff;
	}

	private double search(int depth, double alpha, double beta, boolean myTurn) throws GameOverException {

		if (myTurn) { // my turn 搜索极大值
			Logger.debug("My turn:\n\n");
			if (isGameOver()) {
				return eval();
			}
			for (Direction direction : grid.getMovableDirection()) {
				Ai ai = new Ai(this);
				ai.move(direction);
				//Logger.debug(ai.getGrid().print());
				double score = depth <= 0 ? ai.eval() : ai.search(depth - 1, alpha, beta, false);
				Logger.debug("The score is : " + score + "\n\n");
				if (score > alpha) {
					alpha = score;
					bestDirection = direction;
				}
				if (alpha > beta) {
					break;
				}
			}
			return alpha;
		} else { // computer's turn 搜索极小值
			Logger.debug("Computer's Turn:\n\n");
			// 初选
			int[] numbers;
			int next = grid.getNext();
			double rand = Math.random();
			int max = getLogValue(grid.getMax()) - 2;

			if (next == 0 && (rand <= 0.9 || max < 1)) {
				numbers = new int[]{1, 2, 3};
			} else if (next < 4) {
				numbers = new int[]{next};
			} else {
				// 随机生成>3的next
				int length = max > 3 ? 3 : max;
				numbers = new int[length];
				for (int i=0; i<length; i++) {
					rand = 1 - Math.pow(Math.random(), 2);
					numbers[i] = (int)Math.pow(2, Math.ceil(rand*max)) * 3;
				}
			}

			int maxScore = 0;
			LinkedList<Tile> candidates = new LinkedList<Tile>();
			Cell[] cells = grid.getAppearPossiblyCells();

			for (int number : numbers) {
				for (Cell cell : cells) {
					TileWithScore tile = new TileWithScore(cell, number);
					grid.addTile(tile, cell);
					int score = getIslandNum() - getSmoothness();
					tile.setScore(score);
					grid.removeTile(tile);
					candidates.add(tile);
					maxScore = score > maxScore ? score : maxScore;
				}
			}

			Logger.debug(candidates.toString() + "\n\n");

			// 精选
			for (Tile tile : candidates) {
				if (tile.getScore() == maxScore || candidates.size() <= 4) {
					Ai ai = new Ai(this);
					ai.getGrid().addTile(new Tile(tile), ai.getGrid().getCell(tile.getCell().getLocation()));
					Logger.debug("Add " + tile.getValue() + " at  " + tile.getCell().getLocation() + "\n\n");
					//Logger.debug(ai.getGrid().print());
					double score = ai.search(depth, alpha, beta, true);
					if (score < beta) {
						beta = score;
					}
					if (beta < alpha) {
						break;
					}
				}
			}
			return beta;
		}

	}

	private class TileWithScore extends Tile {
		private int score;

		public int getScore() {
			return score;
		}

		public void setScore(int score) {
			this.score = score;
		}

		public TileWithScore(Cell cell, int value) {
			super(cell, value);
		}

		public String toString() {
			return "s:" + score + " " + super.toString();
		}
	}
}

class GameOverException extends Exception {}
