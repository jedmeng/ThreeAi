package cn.jedm;


class Tile {
	private Grid grid;
	private int value;
	private Cell cell;

	public Tile(Cell cell, int value) {
		this.cell = cell;
		this.value = value;

		if (cell != null) {
			this.grid = cell.getGrid();
		}
	}

	public Tile(int value) {
		this(null, value);
	}

	public Tile(Tile tile) {
		cell = tile.getCell();
		value = tile.getValue();
	}

	public Cell getCell() {
		return cell;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public void setCell(Cell cell) {
		this.cell = cell;
		this.grid = cell.getGrid();
	}

	public int getValue() {
		return value;
	}

	public boolean move(Direction direction) {
		boolean moved = false;
		Location neighbourLocation = cell.getLocation().neighbor(direction);
		while (true) {
			Cell neighbourCell = grid.getCell(neighbourLocation);
			Tile neighbourTile = grid.getTile(neighbourLocation);
			if (!neighbourLocation.isInGrid()) {
				return moved;
			} else if (neighbourCell.isEmpty()) {
				if (!moved) {
					moveTo(neighbourLocation);
					moved = true;
				}
				neighbourLocation = neighbourLocation.neighbor(direction);
			} else if (neighbourTile.move(direction)) {
				moveTo(neighbourLocation);
				return true;
			} else if (!moved && combineAble(neighbourTile)) {
				combine(neighbourTile);
				return true;
			} else {
				return moved;
			}
		}
	}

	private void moveTo(Location location) {
		cell.clear();
		cell = grid.getCell(location);
		cell.setTile(this);
	}

	public boolean isMovable() {
		return this.isMovable(Direction.values());
	}

	public boolean isMovable(Direction[] directions) {
		for (Direction direction : directions) {
			Tile neighbourTile = grid.getTile(cell.getLocation().neighbor(direction));
			if (neighbourTile != null && combineAble(neighbourTile)) {
				return true;
			}
			Cell neighbourCell = grid.getCell(cell.getLocation().neighbor(direction));
			if (neighbourCell != null && neighbourCell.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public boolean combineAble(Tile tile) {
		if (value > 2 || tile.getValue() > 2) {
			return value == tile.getValue();
		} else {
			return value != tile.getValue();
		}
	}

	public void combined() {
		value = value < 3 ? 3 : value * 2;
		if (grid.getMax().getValue() < value) {
			grid.setMax(this);
		}
	}

	public void combine(Tile tile) {
		value = 0;
		tile.combined();
		grid.removeTile(this);
	}

	public int getScore() {
		return (int)Math.pow(3, Math.log(value/3) / Math.log(2) + 1);
	}

	public String toString() {
		return "v:" + value + " " + (cell != null ? cell.getLocation() : "");
	}
}
