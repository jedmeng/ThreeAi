package cn.jedm;


import java.util.*;

class Grid {

	public static int width = 4;
	public static int height = 4;
	public static int size = width * height;

	private int copyLevel = 0;
	private int next;
	private Tile max;
	private Direction lastMoveDirection;
	private HashSet<Tile> tiles = new HashSet<Tile>(size);
	private LinkedList<Tile> lastMovedTiles = new LinkedList<Tile>();
	private HashMap<Location, Cell> cells = new HashMap<Location, Cell>(size);

	public Grid(int[][] data) {
		for (int i=0; i<height; i++) {
			for (int j=0; j<width; j++) {
				int value = data[i][j];
				Location location = Location.getLocation(i, j);
				Cell cell = new Cell(this, location);
				Tile tile;
				if (value > 0) {
					tile = new Tile(value);
					tiles.add(tile);
					cell.setTile(tile);
					if (max == null || tile.getValue() > max.getValue()) {
						max = tile;
					}
				}
				cells.put(location, cell);
			}
		}
		next = data[height][0];
	}

	public Grid(Grid grid) {
		for (Location location : grid.getCells().keySet()) {
			Cell cell = grid.getCells().get(location);
			Tile tile = cell.getTile();
			Cell newCell = new Cell(cell);
			newCell.setGrid(this);
			cells.put(location, newCell);

			if (tile != null) {
				Tile newTile = new Tile(tile);
				newCell.setTile(newTile);
				tiles.add(newTile);

				if (tile == grid.getMax()) {
					max = newTile;
				}
			}
		}
		int copyLevel = grid.copyLevel + 1;
		next = copyLevel == 1 ? grid.getNext() : 0;
	}

	public void move(Direction direction) {
		Logger.debug("\nMove " + direction + "\n");
		lastMoveDirection = direction;
		lastMovedTiles.clear();
		boolean lower = direction == Direction.UP || direction == Direction.LEFT;
		if (direction == Direction.UP || direction == Direction.DOWN) {
			for (int y=0; y<Grid.width; y++) {
				int x = lower ? Grid.height-1 : 0;
				Tile tile;
				Location location;
				do {
					location = Location.getLocation(lower ? x-- : x++, y);
					tile = getTile(location);
				} while (tile == null && location.isInGrid());
				if (location.isInGrid() && tile.move(direction)) {
					lastMovedTiles.add(tile);
				}
			}
		} else {
			for (int x=0; x<Grid.height; x++) {
				int y = lower ? Grid.width-1 : 0;
				Tile tile;
				Location location;
				do {
					location = Location.getLocation(x, lower ? y-- : y++);
					tile = getTile(location);
				} while (tile == null && location.isInGrid());
				if (location.isInGrid() && tile.move(direction)) {
					lastMovedTiles.add(tile);
				}
			}
		}
	}

	public Direction[] getMovableDirection() {
		ArrayList<Direction> movableDirection = new ArrayList<Direction>(Direction.values().length);
		for (Direction direction : Direction.values()) {
			for (Tile tile : tiles) {
				if (tile.isMovable(new Direction[]{direction})) {
					movableDirection.add(direction);
					break;
				}
			}
		}

		Direction[] a = new Direction[movableDirection.size()];
		return movableDirection.toArray(a);
	}

	public int count() {
		return tiles.size();
	}

	public Tile getMax() {
		return max;
	}

	public void setMax(Tile max) {
		this.max = max;
	}

	public int getNext() {
		return next;
	}

	public void clearNext() {
		next = 0;
	}

	public HashMap<Location, Cell> getCells() {
		return cells;
	}

	public Cell getCell(Location location) {
		return location.isInGrid() ? cells.get(location) : null;
	}

	public Iterable<Tile> getTiles() {
		return tiles;
	}

	public Tile getTile(Location location) {
		Cell cell = getCell(location);
		return cell == null ? null : cell.getTile();
	}

	public void addTile(Tile tile, Cell cell) {
		cell.setTile(tile);
		tiles.add(tile);
		tile.setGrid(this);
	}

	public void removeTile(Tile tile) {
		tiles.remove(tile);
		tile.getCell().clear();
		tile.setGrid(null);
	}

	public int getScore() {
		int sum = 0;
		for (Tile tile : tiles) {
			sum += tile.getScore();
		}
		return sum;
	}

	public int getValueSum() {
		int sum = 0;
		for (Tile tile : tiles) {
			sum += tile.getValue();
		}
		return sum;
	}

	public Direction getLastMoveDirection() {
		return lastMoveDirection;
	}

	public boolean isMovable() {
		if (tiles.size() < size) {
			return true;
		}
		Direction[] directions = {Direction.DOWN, Direction.RIGHT};
		for (Tile tile : tiles) {
			if (tile.isMovable(directions)) {
				return true;
			}
		}
		return false;
	}

	public String print() {
		StringBuilder stringBuilder = new StringBuilder();
		String blanks = "         ".substring(0, Integer.toString(max.getValue()).length() + 1);
		for (int x=0; x<height; x++) {
			for (int y=0; y<width; y++) {
				Location location = Location.getLocation(x, y);
				Cell cell = cells.get(location);
				String value = cell.isEmpty() ? "0" : Integer.toString(cell.getTile().getValue());
				stringBuilder.append(blanks.substring(value.length())).append(value);
			}
			stringBuilder.append("\n");
		}
		stringBuilder.append("\nThe next is: ").append(next).append("\n\n");
		return stringBuilder.toString();
	}

	public Cell[] getAppearPossiblyCells() {
		Cell[] appearPossiblyCells = new Cell[lastMovedTiles.size()];
		int i = 0;
		for (Tile tile : lastMovedTiles) {
			Location location = tile.getCell().getLocation();
			if (lastMoveDirection == Direction.UP) {
				appearPossiblyCells[i++] = getCell(location.getBottomLocation());
			} else if (lastMoveDirection == Direction.DOWN) {
				appearPossiblyCells[i++] = getCell(location.getTopLocation());
			} else if (lastMoveDirection == Direction.LEFT) {
				appearPossiblyCells[i++] = getCell(location.getRightLocation());
			} else {
				appearPossiblyCells[i++] = getCell(location.getLeftLocation());
			}
		}
		return appearPossiblyCells;
	}
}
