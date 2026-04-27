package com.IntraConnect.storageConfig;

public class RangeBox {
	int xStart, xEnd, yStart, yEnd, depth;
	String side; // Um Side L und R getrennt zu halten
	String srm;  // SRM-ID um Gänge getrennt zu halten
	String permission; // was darf ins Fach rein (nach Kategorien)
	String pattern; // für den Aufbau der Fach-Koordinate
	
	public RangeBox(String srm, String side,
					int xStart, int xEnd,
					int yStart, int yEnd,
					int depth, String permission,
					String pattern) {
		this.srm = srm;
		this.side = side;
		this.xStart = xStart;
		this.xEnd = xEnd;
		this.yStart = yStart;
		this.yEnd = yEnd;
		this.depth = depth;
		this.permission = permission;
		this.pattern = pattern;
	}
	
	public boolean overlaps(RangeBox other) {
		// gleiche SRM, nur vergleichen, wenn sie auf der gleichen Seite sind
		if (!this.side.equals(other.side) || !this.srm.equals(other.srm)) return false;
		
		// Standard-Rechteck-Kollisionsprüfung
		// ein Bereich überschneidet sich NICHT, wenn einer komplett links, rechts,
		// oberhalb oder unterhalb des anderen liegt.
		return (xStart < other.xEnd && xEnd > other.xStart ) ||
				(xStart > other.xEnd && xEnd > other.xEnd ) ;//&&
				//yStart <= other.yEnd && yEnd >= other.yStart;
	}
}
