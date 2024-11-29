package ru.lsv.librarian2.library;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ru.lsv.librarian2.library.parsers.FB2ZipFileParser;
import ru.lsv.librarian2.library.parsers.FileParserListener;
import ru.lsv.librarian2.library.parsers.INPRecord;
import ru.lsv.librarian2.library.parsers.INPXParser;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.FileEntity;
import ru.lsv.librarian2.models.Library;

/**
 * Реализация работы с библиотекой Либрусек User: Lsv Date: 07.11.2010 Time:
 * 13:49:55
 */
public class LibRusEcLibrary implements LibraryRealization {
	/**
	 * см. {@link ru.lsv.lib.library.LibraryRealization}
	 * 
	 * @return см. {@link ru.lsv.lib.library.LibraryRealization}
	 */
	@Override
	public int IsNewBooksPresent() {
		List<String> newFiles = getFilesDiff();
		if (newFiles == null)
			return -1;
		return (newFiles.size() == 0 ? 0 : 1);
	}

	/**
	 * Получение списка новых zip-файлов - т.е. которые есть в storagePath, но их
	 * нет в db
	 * 
	 * @return Сформированный список файлов или null - если что-то не того
	 */
	private List<String> getFilesDiff() {
		ArrayList<String> newFiles = new ArrayList<String>();
		if (LibraryUtils.getCurrentLibrary() == null)
			return null;
		Library library = LibraryUtils.getCurrentLibrary();
		// Получим список zip-файлов в директории
		File storage = new File(library.storagePath);
		String[] fileArray = storage.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".zip");
			}
		});
		// Если оно пустой или нулевой длины - то что-то сломалось в проверке
		if ((fileArray == null) || (fileArray.length == 0)) {
			return null;
		}
		// TreeSet<String> files = new
		// TreeSet<String>(Arrays.asList(fileArray));
		Set<String> libFiles = FileEntity.getEntitiesAsStream(LibraryUtils.getCurrentLibrary().libraryId)
				.map(el -> el.name).collect(Collectors.toSet());
		if (libFiles.size() == 0) {
			newFiles.addAll(Arrays.asList(fileArray));
		} else {
			for (String file : fileArray) {
				if (!libFiles.contains(file)) {
					newFiles.add(file);
				}
			}
		}
		return newFiles;
	}

	/**
	 * см. @ru.lsv.lib.library.LibraryRealization
	 * 
	 * @param fileListener Листенер при обработке файлов. См
	 *                     {@link ru.lsv.lib.parsers.FileParserListener}
	 * @param diffListener Листенер при обработке архивов. См.
	 *                     {@link ru.lsv.lib.library.LibraryDiffListener}
	 * @return см. {@link ru.lsv.lib.library.LibraryRealization}
	 */
	@Override
	public int processNewBooks(LibraryDiffListener diffListener, FileParserListener fileListener) {
		// Поехали получать дифф
		List<String> newFiles = getFilesDiff();
		if (newFiles == null)
			return -1;
		Library library = LibraryUtils.getCurrentLibrary();
		if (newFiles.size() == 0)
			return 0;// Ну а чего - разве не нормально прочиталось :)?
		FB2ZipFileParser zipParser = new FB2ZipFileParser();
		// fire listener
		if (diffListener != null)
			diffListener.totalFilesInDiffCounted(newFiles.size());
		//
		zipParser.addListener(fileListener);
		boolean hasFailed = false;
		Map<String, INPRecord> inpRecords = null;
		if (library.inpxPath != null && !library.inpxPath.isBlank()) {
			// Пробуем загрузить INPX-файл
			try {
				inpRecords = new INPXParser(library.inpxPath).getRecords();
			} catch (IOException e) {
				inpRecords = null;
			}
		}
		for (String file : newFiles) {
			try {
				// fire listener
				if (diffListener != null)
					diffListener.beginNewFile(file);
				// Формируем список книг
				File fl = new File(library.storagePath + File.separatorChar + file);
				List<Book> books = zipParser.parseZipFile(library.storagePath + File.separatorChar + file, inpRecords);
				// Сохраняем
				// Дата и время добавления. Для одного диффа - оно одинаково
				if (diffListener != null)
					diffListener.fileProcessSavingBooks(file);
				Book.persist(books.stream().map(el -> {
					el.addTime = new java.sql.Date(new Date().getTime());
					return el;
				}));
				// Все обработалось. Надо бы, наверное, и файл в library
				// сохранить
				FileEntity fe = new FileEntity();
				fe.name = file;
				fe.size = fl.length();
				fe.library = LibraryUtils.getCurrentLibrary();
				fe.persist();
			} catch (Exception e) {
				if (diffListener != null)
					diffListener.fileProcessFailed(file, e.getMessage());
			}
		}
		return hasFailed ? -2 : 0;
	}
}
