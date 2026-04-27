package com.IntraConnect.storageConfig;

import com.IntraConnect._enum.Systemtype;
import com.IntraConnect.queryExec.transaction.Transaction;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class StorageSystemConfig {
	
	List<RangeBox> rangeList = new ArrayList<>();
	
	public void load(Element elStorage){
		try {
			List<Element> elSrms = (elStorage != null) ? elStorage.getChildren("SRM") : new ArrayList<>();
			for (Element elSrm : elSrms) {
				String id = elSrm.getAttributeValue("id");
				String pattern = elSrm.getAttributeValue("pattern");
				/** Fächer*/
				Element elRack = elSrm.getChild("Rack");
				List<Element> elSides = elRack.getChildren("Side");
				for (Element elSide : elSides) {
					String seite = elSide.getAttributeValue("type");
					List<Element> elRanges = elSide.getChildren("Range");
					for (Element elRange : elRanges) {
						// X-Koordinate
						Element elX = elRange.getChild("X");
						int xStart = Integer.parseInt(elX.getAttributeValue("start"));
						int xEnd = Integer.parseInt(elX.getAttributeValue("end"));
						// Y-Koordinate
						Element elY = elRange.getChild("Y");
						int yStart = Integer.parseInt(elY.getAttributeValue("start"));
						int yEnd = Integer.parseInt(elY.getAttributeValue("end"));

						// Tiefe des Faches
						Element elDepth = elRange.getChild("Depth");
						int count = Integer.parseInt(elDepth.getAttributeValue("count"));
						// Erlaubte Kategorien für dieses Fach
						Element elPermission = elRange.getChild("Permission");
						String permission = (elPermission != null) ? elPermission.getValue() : "";
						
						RangeBox rangeBox = new RangeBox(id, seite,
								xStart, xEnd,
								yStart, yEnd,
								count, permission,
								pattern);
						
						rangeList.add(rangeBox);
					}
				}
				
			}
		} catch (RuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void validate(){
		//* Check, ob ein overlap existiert.
		for (int i = 0; i < rangeList.size(); i++) {
			for (int j = i + 1; j < rangeList.size(); j++) {
				RangeBox boxA = rangeList.get(i);
				RangeBox boxB = rangeList.get(j);
				
				if (boxA.overlaps(boxB)) {
					throw new RuntimeException("Überlappung gefunden zwischen Index " + i + " und " + j);
				}
			}
		}
	}
	
	public void register(){
		//** hier die Fächer zusammenbasteln
		// und in die Datenbank eintragen
		List<Object[]> coors = new ArrayList<>();
		for (RangeBox rb : rangeList) {
			// 1. Vorbereitung: Minus entfernen und Längen der Platzhalter bestimmen
			String pattern = rb.pattern.replace("-", "");
			int patterLength = pattern.length();
			// Wie viele X, Y, Z sind im Pattern? (z.B. "XXX" -> 3)
			int xLen = countOccurrences(pattern, 'X');
			int yLen = countOccurrences(pattern, 'Y');
			int zLen = countOccurrences(pattern, 'Z');
			
			// Das Basis-Gerüst ohne die Zahlen-Platzhalter (G und S setzen wir fest ein)
			int sPos = pattern.indexOf("S");
			int gPos = pattern.indexOf("G");

            // 2. Erstelle den StringBuilder direkt aus dem Pattern
			StringBuilder sb = new StringBuilder(pattern);
			
			for (int x = rb.xStart; x <= rb.xEnd; x++) {
				for (int y = rb.yStart; y <= rb.yEnd; y++) {
					for (int z = 1; z <= rb.depth; z++) {
						
						if (sPos != -1) {
							int gLen = countOccurrences(pattern, 'S');
							sb.replace(sPos, sPos + gLen, rb.side); // Ersetzt die S-Gruppe durch z.B. "L" oder "1" oder "01"
						}
						if (gPos != -1) {
							int gLen = countOccurrences(pattern, 'G');
							sb.replace(gPos, gPos + gLen, rb.srm); // Ersetzt die G-Gruppe durch z.B. "SR01"
						}
						
						// Jetzt ersetzen wir die Platzhalter im StringBuilder
						replaceInBuilder(sb, "X", x, xLen);
						replaceInBuilder(sb, "Y", y, yLen);
						replaceInBuilder(sb, "Z", z, zLen);
						
						String coordinate = sb.toString();
						if(coordinate.length() != patterLength){
							throw new RuntimeException("Koordinatenlänge des Faches '"+coordinate+"' passen nicht zum Pattern "+rb.pattern);
						}
						coors.add(new Object[]{coordinate});
					}
				}
			}
		}

		try (Transaction transaction = Transaction.create()){

				String sql = "INSERT INTO POINTS (NAME, type, BLOCKED) VALUES (?, '" + Systemtype.RACK.name() + "', 0)";
				transaction.insertBatch(sql, coors);
				transaction.commit();
				
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Ersetzt den Platzhalter (z.B. "XXX") im StringBuilder durch die
	 * formatierte Zahl (z.B. "005").
	 */
	private void replaceInBuilder(StringBuilder sb, String placeholderChar, int value, int length) {
		String search = placeholderChar.repeat(length);
		int start = sb.indexOf(search);
		if (start != -1) {
			// Formatiert die Zahl mit führenden Nullen
			String formattedValue = String.format("%0" + length + "d", value);
			sb.replace(start, start + length, formattedValue);
		}
	}
	
	/**
	 * Hilfsmethode zum Zählen der Platzhalter-Länge
	 */
	private int countOccurrences(String text, char c) {
		int count = 0;
		for (char ch : text.toCharArray()) {
			if (ch == c) count++;
		}
		return count;
	}
}
