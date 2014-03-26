package cn.jedm;


class Location {
	int x, y;

	static Location[][] cache = new Location[Grid.height][Grid.width];
	static Location errorLocation = new Location(-1, -1);
	static Location getLocation(int x, int y) {
		if (x < 0 || y < 0 || x >= Grid.height || y >= Grid.width) {
			return errorLocation;
		}
		if (cache[x][y] == null) {
			cache[x][y] = new Location(x, y);
		}
		return cache[x][y];
	}

	private Location(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isInGrid() {
		return x >=0 && y >= 0 && x < Grid.height && y < Grid.width;
	}

	public Location getTopLocation() {
		return Location.getLocation(0, y);
	}

	public Location getBottomLocation() {
		return Location.getLocation(Grid.height - 1, y);
	}
	public Location getLeftLocation() {
		return Location.getLocation(x, 0);
	}
	public Location getRightLocation() {
		return Location.getLocation(x, Grid.width - 1);
	}

	public int hashCode() {
		return y * Grid.width + x;
	}

	public Location neighbor(Direction direction) {
		switch (direction) {
			case UP:
				return getLocation(x -1, y);
			case DOWN:
				return getLocation(x +1, y);
			case LEFT:
				return getLocation(x, y - 1);
			case RIGHT:
				return getLocation(x, y + 1);
		}
		return null;
	}

	public String toString() {
		return "x: " + this.x + " y: " + this.y;
	}

}
