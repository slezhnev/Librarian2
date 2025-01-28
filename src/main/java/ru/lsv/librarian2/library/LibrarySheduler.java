package ru.lsv.librarian2.library;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang3.NotImplementedException;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.lsv.librarian2.library.parsers.FileParserListener;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.Library;

@ApplicationScoped
public class LibrarySheduler {

	private final Logger LOG = Logger.getLogger(LibrarySheduler.class);

	private volatile ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	@Inject
	private LibRusEcLibrary libRusEcImplementation;

	@Inject
	private LibraryUtils utils;

	/**
	 * Timeout between scans in minutes
	 */
	private final int timeoutBetweenScans = 1;

	/**
	 * @return the scheduler
	 */
	public synchronized void checkForNewBook() {
		if (LoadStatus.getInstance().getInProgress() == null || LoadStatus.getInstance().getInProgress().isDone()) {
			if (LoadStatus.getInstance().getLastAnalysisFinished() == null || Duration
					.between(LoadStatus.getInstance().getLastAnalysisFinished(), LocalDateTime.now()).toMinutes() > timeoutBetweenScans) {
				LOG.info("Start new scan...");
				LoadStatus.getInstance().setCheckinginProgress(true);
				LoadStatus.getInstance().setInProgress(scheduler.submit(() -> {
					service();
				}));
			}
		}
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

		@Override
		public void checkingFinishedStartToProcess() {
			LoadStatus.getInstance().setCheckinginProgress(false);
		}

	}

	@Transactional
	public List<Library> getAllLibraries() {
		return Library.listAll();
	}

	/**
	 * Поиск новых книг в библиотеках
	 */
	public void service() {
		try {
			// Получаем ВСЕ библиотеки
			LOG.info("Getting libraries");
			List<Library> libraries = getAllLibraries();
			if (libraries != null) {
				for (Library library : libraries) {
					utils.setCurrentLibrary(library);
					// Смотрим тип библиотеки - ищем реализацию
					LibraryRealization libRes = null;
					switch (library.libraryKind) {
						case 1 -> {
							// Либрусек
							LOG.info("Found Librusec library - got representation");
							libRes = libRusEcImplementation;
						}
						case 2 -> {
							LOG.error("Found Flibusta library - not implemented");
							throw new NotImplementedException("Support of Flibusta library does not implemented");
						}
						default -> {
							LOG.errorf("Invalid library kind - %d, name - %s, id - %d", library.libraryKind,
									library.name, library.libraryId);
							throw new NotImplementedException("Library kind does not supported");
						}
					}
					if (libRes != null) {
						// Что-то начинаем делать...
						LOG.info("Trying to find a new books");
						int res = libRes.IsNewBooksPresent();
						if (res == 1) {
							LOG.info("Found new book - start to process it");
							// Чота есть!
							LibraryProcessingCallback callback = new LibraryProcessingCallback();
							// Запущаем!
							libRes.processNewBooks(callback, callback);
							// В завершении - сбросим все в LoadStatus
							LoadStatus.getInstance().clear();
						} else if (res == -1) {
							LOG.error("Have a problem during new files processing");
							LoadStatus.getInstance().setWasErrorOnLoad(true);
						} else {
							LOG.info("No new book found");
						}
					}
				}
			}
		} catch (Throwable e) {
			LOG.error("Got an exception", e);
		} finally {
			LoadStatus.getInstance().setCheckinginProgress(false);
			LoadStatus.getInstance().setLastAnalysisFinished(LocalDateTime.now());
		}
	}

}
