package ru.lsv.librarian2.library;

/**
 * Интерфейс реализации callback'ов при обработке diff'а библиотеки <br/>
 * Тянуто из Librarian'а
 * 
 * @author s.lezhnev
 */
public interface LibraryDiffListener {

	/**
	 * Посчитано общее количество файлов в дифе
	 * 
	 * @param totalFilesInDiff
	 *            Общее количество файлов в дифе
	 */
	void totalFilesInDiffCounted(int totalFilesInDiff);

	/**
	 * Начата обработка нового файла
	 * 
	 * @param fileName
	 *            Имя файла
	 */
	void beginNewFile(String fileName);

	/**
	 * При обработке файла прилетел exception
	 * 
	 * @param fileName
	 *            Имя файла
	 * @param msg
	 *            Сообщение exception'а
	 */
	void fileProcessFailed(String fileName, String msg);

	/**
	 * Начали процесс сохранения книг (он длительный)
	 * 
	 * @param fileName
	 *            Имя файла
	 */
	void fileProcessSavingBooks(String fileName);

}
