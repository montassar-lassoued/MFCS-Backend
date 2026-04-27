package com.IntraConnect.visualization;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ArrowDto {
	public String id;
	@JsonProperty("from") // Mappt XML 'from' auf JSON 'from'
	public String fromRectId;
	@JsonProperty("to")
	public String toRectId;
	public String direction;
	public double speed;
	public List<Point> waypoints;
}
