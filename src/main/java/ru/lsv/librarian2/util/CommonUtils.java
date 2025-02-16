package ru.lsv.librarian2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Properties;

import org.jboss.logging.Logger;

import jakarta.transaction.Transactional;
import ru.lsv.librarian2.models.Book;

public class CommonUtils {

	private static final Logger LOG = Logger.getLogger(CommonUtils.class);

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

	@Transactional
	public static void updateDownloadedBook(Integer bookId, String userName) {
		LOG.infof("[bookId: %d] Removing mustRead mark and set readed", bookId);
		Book book = Book.findById(bookId);
		book.mustRead = new HashSet<>(book.mustRead);
		book.readed = new HashSet<>(book.readed);
		book.mustRead.remove(userName);
		book.readed.add(userName);
		book.persist();
	}

	public static class StoragePathOverrideException extends Exception {

		StoragePathOverrideException(Exception ex, String message) {
			super(message, ex);
		}

		StoragePathOverrideException(String message) {
			super(message);
		}
	}

	public static String overrideStoragePathIfNeeded(String storagePath, Integer libraryId)
			throws StoragePathOverrideException {
		String storageOverrideFilename = System.getProperty("library.storagePath.override", "");
		if (storageOverrideFilename != null && !storageOverrideFilename.isBlank()) {
			LOG.infof(
					"Storage path should be overwritten. Trying to find '%s'", storageOverrideFilename);
			File spOverride = new File(storageOverrideFilename);
			if (spOverride.exists()) {
				Properties props = new Properties();
				try (FileInputStream fis = new FileInputStream(spOverride)) {
					props.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
				} catch (IOException ex) {
					LOG.errorf(ex,
							"Storage path should be overwritten. But got IOException while reading a file '%s'",
							spOverride.getAbsolutePath());
					throw new StoragePathOverrideException(ex, "Exception while reading override parameters file");
				}
				String newStoragePath = props.getProperty("library.storagePath." + libraryId);
				if (newStoragePath != null) {
					LOG.infof("Storage path was overwritten from '%s' to '%s'",
							storagePath, newStoragePath);
					return newStoragePath;
				} else {
					LOG.errorf(
							"Storage path should be overwritten. But file '%s' does not contain needed key %s",
							storageOverrideFilename, "library.storagePath" + libraryId);
					throw new StoragePathOverrideException("Cannot get override library location file");
				}
			} else {
				LOG.errorf(
						"Storage path should be overwritten. But file '%s' does not exists",
						storageOverrideFilename);
				throw new StoragePathOverrideException("Cannot find override configuration file");
			}
		} else {
			return storagePath;
		}
	}

	public static String overrideINPXPathIfNeeded(String inpxPath, Integer libraryId)
			throws StoragePathOverrideException {
		String storageOverrideFilename = System.getProperty("library.storagePath.override", "");
		if (storageOverrideFilename != null && !storageOverrideFilename.isBlank()) {
			LOG.infof(
					"INPX path should be overwritten. Trying to find '%s'", storageOverrideFilename);
			File spOverride = new File(storageOverrideFilename);
			if (spOverride.exists()) {
				Properties props = new Properties();
				try (FileInputStream fis = new FileInputStream(spOverride)) {
					props.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
				} catch (IOException ex) {
					LOG.errorf(ex,
							"INPX path should be overwritten. But got IOException while reading a file '%s'",
							spOverride.getAbsolutePath());
					throw new StoragePathOverrideException(ex, "Exception while reading override parameters file");
				}
				String newINPXPath = props.getProperty("library.inpxPath." + libraryId);
				if (newINPXPath != null) {
					LOG.infof("INPX path was overwritten from '%s' to '%s'",
							inpxPath, newINPXPath);
					return newINPXPath;
				} else {
					LOG.errorf(
							"INPX path should be overwritten. But file '%s' does not contain needed key %s",
							storageOverrideFilename, "library.inpxPath" + libraryId);
					throw new StoragePathOverrideException("Cannot get override library location file");
				}
			} else {
				LOG.errorf(
						"INPX path should be overwritten. But file '%s' does not exists",
						storageOverrideFilename);
				throw new StoragePathOverrideException("Cannot find override configuration file");
			}
		} else {
			return inpxPath;
		}
	}

}
