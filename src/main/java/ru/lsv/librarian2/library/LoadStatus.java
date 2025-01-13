package ru.lsv.librarian2.library;

/**
 * Статус загрузки книг в библиотеку
 * 
 * @author s.lezhnev
 */
public class LoadStatus {

	/**
	 * Название текущей обрабатываемой библиотеки
	 */
	private String currentLibrary = "";
	/**
	 * Общее количество архивов для обработки
	 */
	private int totalArcsToProcess = 0;
	/**
	 * Номер текушего обрабатываемого архива
	 */
	private int currentArcsToProcess = 0;
	/**
	 * Имя текущего обрабатываемого архива
	 */
	private String currentArcName = "";
	/**
	 * Общее количество файлов в текущем обрабатываемом архиве
	 */
	private int totalFilesToProcess = 0;
	/**
	 * Номер nекущего обрабатываемого файл
	 */
	private int currentFileToProcess = 0;
	/**
	 * Признак ошибки при загрузке
	 */
	private boolean wasErrorOnLoad = false;
	/**
	 * Mark that checking was started, but not yet finished
	 */
	private boolean checkinginProgress = false;

	/**
	 * Hide constructor
	 */
	private LoadStatus() {

	}

	/**
	 * Экземпляр инстанса
	 */
	private static LoadStatus instance = new LoadStatus();

	/**
	 * Синглтон
	 * 
	 * @return Экземпляр синглтона
	 */
	public static synchronized LoadStatus getInstance() {
		return instance;
	}

	/**
	 * Клонирование экземпляра класса <br/>
	 * Чтобы избежать проблем при выгрузке через GsonBuilder
	 */
	public synchronized LoadStatus clone() {
		LoadStatus res = new LoadStatus();
		res.currentArcName = this.currentArcName;
		res.currentArcsToProcess = this.currentArcsToProcess;
		res.currentFileToProcess = this.currentFileToProcess;
		res.totalArcsToProcess = this.totalArcsToProcess;
		res.totalFilesToProcess = this.totalFilesToProcess;
		res.currentLibrary = this.currentLibrary;
		res.wasErrorOnLoad = this.wasErrorOnLoad;
		return res;
	}

	/**
	 * @return the totalArcsToProcess
	 */
	public synchronized int getTotalArcsToProcess() {
		return totalArcsToProcess;
	}

	/**
	 * @param totalArcsToProcess
	 *            the totalArcsToProcess to set
	 */
	public synchronized void setTotalArcsToProcess(int totalArcsToProcess) {
		this.totalArcsToProcess = totalArcsToProcess;
		this.currentArcsToProcess = 0;
	}

	/**
	 * @return the currentArcsToProcess
	 */
	public synchronized int getCurrentArcsToProcess() {
		return currentArcsToProcess;
	}

	/**
	 * @param currentArcsToProcess
	 *            the currentArcsToProcess to set
	 * @param currentArcName
	 *            the currentArcName to set
	 */
	public synchronized void setCurrentArcsToProcess(int currentArcsToProcess,
			String currentArcName) {
		this.currentArcsToProcess = currentArcsToProcess;
		this.currentArcName = currentArcName;
	}

	/**
	 * Установить следующий архив для обработки и увеличить на 1 счетчик
	 * обработанных архивов
	 * 
	 * @param currentArcName
	 *            Имя архива
	 */
	public synchronized void nextArcsToProcess(String currentArcName) {
		this.currentArcName = currentArcName;
		currentArcsToProcess++;
		if (currentArcsToProcess > totalArcsToProcess) {
			currentArcsToProcess = totalArcsToProcess;
		}
	}

	/**
	 * @return the currentArcName
	 */
	public synchronized String getCurrentArcName() {
		return currentArcName;
	}

	/**
	 * @return the totalFilesToProcess
	 */
	public synchronized int getTotalFilesToProcess() {
		return totalFilesToProcess;
	}

	/**
	 * @param totalFilesToProcess
	 *            the totalFilesToProcess to set
	 */
	public synchronized void setTotalFilesToProcess(int totalFilesToProcess) {
		this.totalFilesToProcess = totalFilesToProcess;
		// Тут ноль - поскольку имя будет устанавливаться в nextFileToProcess
		this.currentFileToProcess = 0;
	}

	/**
	 * @return the currentFileToProcess
	 */
	public synchronized int getCurrentFileToProcess() {
		return currentFileToProcess;
	}

	/**
	 * @param currentFileToProcess
	 *            the currentFileToProcess to set
	 */
	public synchronized void setCurrentFileToProcess(int currentFileToProcess) {
		this.currentFileToProcess = currentFileToProcess;
	}

	/**
	 * Увеличить на 1 число обработанных файлов в архиве
	 */
	public synchronized void nextFileToProcess() {
		currentFileToProcess++;
		if (currentFileToProcess > totalFilesToProcess) {
			currentFileToProcess = totalFilesToProcess;
		}
	}

	/**
	 * Выставляет отметку о том, что начато сохранение книг <br/>
	 * Признак - currentFileToProcess = totalFilesToProcess + 1
	 */
	public synchronized void setSaveBooksMark() {
		currentFileToProcess = totalFilesToProcess + 1;
	}

	/**
	 * @return the currentLibrary
	 */
	public synchronized String getCurrentLibrary() {
		return currentLibrary;
	}

	/**
	 * @param currentLibrary
	 *            the currentLibrary to set
	 */
	public synchronized void setCurrentLibrary(String currentLibrary) {
		this.currentLibrary = currentLibrary;
	}

	/**
	 * @return the wasErrorOnLoad
	 */
	public boolean isWasErrorOnLoad() {
		return wasErrorOnLoad;
	}

	/**
	 * @param wasErrorOnLoad the wasErrorOnLoad to set
	 */
	public void setWasErrorOnLoad(boolean wasErrorOnLoad) {
		this.wasErrorOnLoad = wasErrorOnLoad;
	}

	public boolean isCheckinginProgress() {
		return checkinginProgress;
	}

	public void setCheckinginProgress(boolean checkinginProgress) {
		this.checkinginProgress = checkinginProgress;
	}

	/**
	 * Сбрасывает состояние в "исходное"
	 */
	public synchronized void clear() {
		currentLibrary = "";
		totalArcsToProcess = 0;
		currentArcsToProcess = 0;
		currentArcName = "";
		totalFilesToProcess = 0;
		currentFileToProcess = 0;
		wasErrorOnLoad = false;
		checkinginProgress = false;
	}

}
