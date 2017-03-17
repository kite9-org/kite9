package org.kite9.framework.logging;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tabular displayer
 * 
 */
public class Table {

	List<Integer> widths = new ArrayList<Integer>();

	List<String[]> rows = new ArrayList<String[]>();

	public void addRow(Object... items) {
		List<String> longRow = new ArrayList<String>();

		for (Object object : items) {
			if (object==null) {
				longRow.add("null");
			} else if (object.getClass().isArray()) {
				int length = Array.getLength(object);

				for (int i = 0; i < length; i++) {
					Object item = Array.get(object, i);
					longRow.add(item.toString());
				}
			} else if (object instanceof Collection<?>) {
				Collection<?> c = (Collection<?>) object;
				for (Object object2 : c) {
					longRow.add(object2.toString());
				}
			} else {
				longRow.add(object.toString());
			}
		}

		addArrayRow((String[]) longRow.toArray(new String[longRow.size()]));

	}
	
	public void removeLastRow() {
		rows.remove(rows.size()-1);
	}

	public void addArrayRow(String[] row) {
		rows.add(row);

		int col = 0;
		for (String string : row) {
			if (widths.size() > col) {
				widths.set(col, widths.get(col) > string.length() ? widths.get(col) : string.length());
			} else {
				widths.add(string.length());
			}
			col++;
		}
	}
	
	public void addObjectRow(Object[] row) {
		String[] items = new String[row.length];
		for (int i = 0; i < items.length; i++) {
			items[i] = row[i].toString();
		}
		addArrayRow(items);
	}
	
	public void addDoubleRow(double[] row) {
		String[] items = new String[row.length];
		for (int i = 0; i < items.length; i++) {
			items[i] = Double.toString(row[i]);
		}
		addArrayRow(items);
	}

	public void display(StringBuffer sb) {
		for (String[] row : rows) {
			int colno = 0;
			for (String col : row) {
				int width = widths.get(colno) + 1;
				sb.append(col);
				for (int i = col.length(); i < width; i++) {
					sb.append(" ");
				}

				colno++;
			}
			sb.append("\n");
		}
	}
	
}
