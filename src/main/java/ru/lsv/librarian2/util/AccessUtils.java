package ru.lsv.librarian2.util;

public class AccessUtils {

	public static String updateSearch(String searchString) {
		if (searchString == null || searchString.isBlank()) {
			return "%";
		} else {
			return searchString + "%";
		}
	}

}
