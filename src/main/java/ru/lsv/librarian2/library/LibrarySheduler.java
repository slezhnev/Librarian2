package ru.lsv.librarian2.library;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.lsv.librarian2.library.parsers.FileParserListener;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.Library;

/**
 * Шедулер обработки новых книг в библиотеках
 * 
 * @author s.lezhnev
 */
public class LibrarySheduler {

	/**
	 * Шедулер
	 */
	private static volatile ScheduledExecutorService scheduler = null;

	/**
	 * @return the scheduler
	 */
	public static synchronized ScheduledExecutorService getScheduler() {
		if (scheduler == null) {
			scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.scheduleWithFixedDelay(() -> {
				service();
			}, 0, 1, TimeUnit.DAYS);
		}
		return scheduler;
	}

	/**
	 * Класс переноса результатов обработки библиотеки в LoadStatus
	 * 
	 * @author s.lezhnev
	 * 
	 */
	private static class LibraryProcessingCallback implements LibraryDiffListener, FileParserListener {

		@Override
		public void inArchiveFilesCounted(int numFilesInZip) {
			LoadStatus.getInstance().setTotalFilesToProcess(numFilesInZip);
			LoadStatus.getInstance().setCurrentFileToProcess(1);
		}

		@Override
		public void inArchiveFileProcessed(String fileName, Book book) {
			LoadStatus.getInstance().nextFileToProcess();
		}

		@Override
		public void inArchiveFileParseFailed(String fileName) {
			LoadStatus.getInstance().nextFileToProcess();
		}

		@Override
		public void totalFilesInDiffCounted(int totalFilesInDiff) {
			LoadStatus.getInstance().setTotalArcsToProcess(totalFilesInDiff);
		}

		@Override
		public void beginNewFile(String fileName) {
			LoadStatus.getInstance().nextArcsToProcess(fileName);
		}

		@Override
		public void fileProcessFailed(String fileName, String msg) {
			// TODO Auto-generated method stub

		}

		@Override
		public void fileProcessSavingBooks(String fileName) {
			// Делаем хитрый ход конем
			LoadStatus.getInstance().setSaveBooksMark();
		}

	}

	/**
	 * Поиск новых книг в библиотеках
	 */
	public static void service() {
		// Получаем ВСЕ библиотеки
		List<Library> libraries = Library.listAll();
		if (libraries != null) {
			for (Library library : libraries) {
				LibraryUtils.setCurrentLibrary(library);
				// Смотрим тип библиотеки - ищем реализацию
				LibraryRealization libRes = null;
				switch (library.libraryKind) {
					case 1 -> {
						// Либрусек
						libRes = new LibRusEcLibrary();
					}
					case 2 -> {
					}
					default -> {
					}
				}
				if (libRes != null) {
					// Что-то начинаем делать...
					int res = libRes.IsNewBooksPresent();
					if (res == 1) {
						// Чота есть!
						LibraryProcessingCallback callback = new LibraryProcessingCallback();
						// Запущаем!
						libRes.processNewBooks(callback, callback);
						// В завершении - сбросим все в LoadStatus
						LoadStatus.getInstance().clear();
					} else if (res == -1) {
						LoadStatus.getInstance().setWasErrorOnLoad(true);
					}
				}
			}
		}
	}

}
