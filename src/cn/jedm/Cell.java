package cn.jedm;


public class Cell {
	private Grid grid;
	private Location location;
	private Tile tile = null;

	public Cell(Grid grid, Location location, Tile tile) {
		this.grid = grid;
		this.location = location;
		this.tile = tile;

		if (tile != null) {
			tile.setCell(this);
		}
	}

	public Cell(Grid grid, Location location) {
		this(grid, location, null);
	}

	public Cell(Cell cell) {
		grid = cell.getGrid();
		location = cell.getLocation();
		tile = cell.getTile();
	}

	public void setTile(Tile tile) {
		this.tile = tile;
		tile.setCell(this);
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public Grid getGrid() {
		return grid;
	}

	public boolean isEmpty() {
		return tile == null;
	}

	public void clear() {
		tile = null;
	}

	public Tile getTile() {
		return this.tile;
	}

	public Location getLocation() {
		return location;
	}

	public String toString() {
		return isEmpty() ? "E " + location.toString() : tile.toString();
	}
}
