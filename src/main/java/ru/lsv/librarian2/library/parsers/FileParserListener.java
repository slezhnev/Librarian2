package ru.lsv.librarian2.library.parsers;

import ru.lsv.librarian2.models.Book;

/**
 * Класс организации callback'ов при парсинге zip-файла библиотеки. Ибо иначе
 * оно там умедитируется <br/>
 * Тянуто из Librarian
 * 
 * @author s.lezhnev
 */
public interface FileParserListener {

	/**
	 * Посчитано общее количество файлов в зипе
	 * 
	 * @param numFilesInZip
	 *            Количество файлов
	 */
	void inArchiveFilesCounted(int numFilesInZip);

	/**
	 * Обрабатываем файл
	 * 
	 * @param fileName
	 *            Имя файла
	 * @param book
	 *            Книга унутре
	 */
	void inArchiveFileProcessed(String fileName, Book book);

	/**
	 * Профейлилась обработка файла
	 * 
	 * @param fileName
	 *            Имя файла
	 */
	void inArchiveFileParseFailed(String fileName);
}
