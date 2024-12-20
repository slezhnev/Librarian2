package ru.lsv.librarian2.util;

public class AccessUtils {

	public static String updateSearch(String searchString) {
		if (searchString == null || searchString.isBlank()) {
			return "%";
		} else {
			String replaced = searchString.replace('*', '%');
			if (!replaced.endsWith("%")) {
				return replaced + "%";
			} else {
				return replaced;
			}
		}
	}

}
