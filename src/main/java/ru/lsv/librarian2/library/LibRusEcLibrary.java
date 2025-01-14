package ru.lsv.librarian2.library;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import jakarta.transaction.Transactional;
import ru.lsv.librarian2.library.parsers.FB2ZipFileParser;
import ru.lsv.librarian2.library.parsers.FileParserListener;
import ru.lsv.librarian2.library.parsers.INPRecord;
import ru.lsv.librarian2.library.parsers.INPXParser;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.FileEntity;
import ru.lsv.librarian2.models.Library;
import ru.lsv.librarian2.util.CommonUtils;
import ru.lsv.librarian2.util.CommonUtils.StoragePathOverrideException;

/**
 * lib.rus.ec (in torrent format) support
 */
public class LibRusEcLibrary implements LibraryRealization {

	private final Logger LOG = Logger.getLogger(LibRusEcLibrary.class);

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
		List<String> newFiles;
		if (LibraryUtils.getCurrentLibrary() == null) {
			LOG.error("Current library does not set - reporting no new book");
			return null;
		}
		Library library = LibraryUtils.getCurrentLibrary();
		// Get file list in folder
		String storagePath;
		try {
			storagePath = CommonUtils.overrideStoragePathIfNeeded(library.storagePath, library.libraryId);
		} catch (StoragePathOverrideException e) {
			LOG.errorf(e, "[libId: %d] Got an exception while trying to ovewrite storage path",
					library.libraryId);
			return null;
		}
		LOG.infof("[libId: %d] Getting files in \"%s\"",
				library.libraryId,
				storagePath);
		File storage = new File(storagePath);
		String[] fileArray = storage.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".zip");
			}
		});
		if ((fileArray == null) || (fileArray.length == 0)) {
			LOG.errorf("[libId: %d] We do not have files in specified library location \"%s\" - reporting no new book",
					library.libraryId,
					storagePath);
			return null;
		}
		List<String> files = Arrays.asList(fileArray);
		// We cannot filter at this place - we should process the library case, it
		// should not contain any file
		Set<String> libFiles = FileEntity.getEntities(LibraryUtils.getCurrentLibrary().libraryId).stream()
				.map(el -> el.name).collect(Collectors.toSet());
		if (libFiles.size() == 0) {
			LOG.infof("[libId: %d] Library does not contain any files - assume all files as new",
					library.libraryId);
			newFiles = files;
		} else {
			// for (String file : fileArray) {
			// if (!libFiles.contains(file)) {
			// newFiles.add(file);
			// }
			// }
			LOG.infof("[libId: %d] Filtering for new files", library.libraryId);
			newFiles = files.stream().filter(el -> !libFiles.contains(el)).collect(Collectors.toList());
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
		if (LibraryUtils.getCurrentLibrary() == null) {
			LOG.error("Current library does not set - nothing to process");
			return -1;
		}
		Library library = LibraryUtils.getCurrentLibrary();
		// Поехали получать дифф
		List<String> newFiles = getFilesDiff();
		if (newFiles == null) {
			LOG.infof("[libId: %d] New files found - nothing to process", library.libraryId);
			return -1;
		}
		if (newFiles.size() == 0) {
			LOG.errorf("[libId: %d] getFilesDiff return 0-sized list instead of null", library.libraryId);
			return 0;
		}
		FB2ZipFileParser zipParser = new FB2ZipFileParser();
		// fire listener
		if (diffListener != null)
			diffListener.totalFilesInDiffCounted(newFiles.size());
		//
		zipParser.addListener(fileListener);
		boolean hasFailed = false;
		Map<String, INPRecord> inpRecords = null;
		if (library.inpxPath != null && !library.inpxPath.isBlank()) {
			String inpxPath;
			try {
				inpxPath = CommonUtils.overrideINPXPathIfNeeded(library.inpxPath, library.libraryId);
				try {
					inpRecords = new INPXParser(inpxPath).getRecords();
				} catch (IOException e) {
					inpRecords = null;
				}
				LOG.infof("[libId: %d] INPX file was loaded successfully", library.libraryId);
			} catch (StoragePathOverrideException e) {
				LOG.errorf(e, "[libId: %d] Got an exception while trying to ovewrite INPX path",
						library.libraryId);
				inpRecords = null;
			}
		}
		String storagePath;
		try {
			storagePath = CommonUtils.overrideStoragePathIfNeeded(library.storagePath, library.libraryId);
		} catch (StoragePathOverrideException e) {
			LOG.errorf(e, "[libId: %d] Got an exception while trying to ovewrite storage path",
					library.libraryId);
			return -1;
		}
		if (diffListener != null)
			diffListener.checkingFinishedStartToProcess();
		for (String file : newFiles) {
			try {
				LOG.infof("[libId: %d] Processing %s", library.libraryId, storagePath + File.separatorChar + file);
				// fire listener
				if (diffListener != null)
					diffListener.beginNewFile(file);
				// Формируем список книг
				File fl = new File(storagePath + File.separatorChar + file);
				List<Book> books = zipParser.parseZipFile(storagePath + File.separatorChar + file, inpRecords);
				LOG.infof("[libId: %d] Processing %s - total books: %d", library.libraryId, file, books.size());
				// Сохраняем
				// Дата и время добавления. Для одного диффа - оно одинаково
				if (diffListener != null) {
					diffListener.fileProcessSavingBooks(file);
				}
				LOG.infof("[libId: %d] Processing %s - storing books and processed file", library.libraryId, file);
				FileEntity fe = new FileEntity();
				fe.name = file;
				fe.size = fl.length();
				fe.library = library;
				persistBooksAndFileEntity(books, fe);
			} catch (Exception e) {
				if (diffListener != null)
					diffListener.fileProcessFailed(file, e.getMessage());
				LOG.errorf(e, "[libId: %d] Got an exception while processing %s", library.libraryId, file);
			}
		}
		return hasFailed ? -2 : 0;
	}

	@Transactional
	private void persistBooksAndFileEntity(List<Book> books, FileEntity fe) {
		Book.persist(books.stream().map(el -> {
			el.addTime = new java.sql.Date(new Date().getTime());
			return el;
		}));
		fe.persist();
	}
}
