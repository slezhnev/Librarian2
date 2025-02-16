package ru.lsv.librarian2.library.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ru.lsv.librarian2.library.LibraryUtils;
import ru.lsv.librarian2.models.Author;
import ru.lsv.librarian2.models.Book;
//import ru.lsv.librarian2.library.LibraryUtils;
import ru.lsv.librarian2.models.Library;

/**
 * Парсер одной строки в INP-файле, который содержится в INPX <br/>
 * Умеет самостоятельно заполнять Book
 */
public class INPRecord {

    private String[] parts = null;
    private List<INPAuthor> authors = null;
    private String[] genres = null;

    /**
     * Парсит строку в INP-файле
     *
     * @param inpString Входная строка, которую надо будет распарсить
     * @throws ru.lsv.lib.parsers.INPRecord.BadINPRecord
     *          Возникает в случае несоответствия строки формату
     */
    public INPRecord(String inpString) throws BadINPRecord {
        // AUTHOR     ;    GENRE     ;     TITLE           ; SERIES ; SERNO ; FILE ;    SIZE   ;  LIBID    ;    DEL   ;    EXT     ;       DATE        ;    LANG    ; LIBRATE  ; KEYWORDS ;
        // static char* dummy = "dummy:" "\x04" "other:" "\x04" "dummy record" "\x04"   "\x04"  "\x04" "\x04" "1" "\x04" "%d" "\x04" "1" "\x04" "EXT" "\x04" "2000-01-01" "\x04" "en" "\x04" "0" "\x04"     "\x04" "\r\n";
        // from http://www.assembla.com/code/myhomelib/subversion/nodes/Utils/InpxCreator/trunk/lib2inpx/main.cpp
        // Делим строку
        parts = inpString.split("" + ((char) 4));
        if (parts.length < 12) {
            throw new BadINPRecord("INP record has only " + parts.length + "parts");
        }
        // Выделяем авторов
        String[] tmpAuthors = parts[0].split(":");
        authors = new ArrayList<>();
        for (String auth : tmpAuthors) {
            String[] fio = auth.split(",");
            switch (fio.length) {
                case 0 ->  {
                    // Что-то странное - ничего делать не будем

                }
                case 1 ->  {
                    // Есть только фамилия
                    authors.add(new INPAuthor(fio[0], "", ""));
                }
                case 2 ->  {
                    // Есть только фамилия + имя
                    authors.add(new INPAuthor(fio[0], fio[1], ""));
                }
                case 3 ->  {
                    // Есть все
                    authors.add(new INPAuthor(fio[0], fio[1], fio[2]));
                }
            }
        }
        // Выделяем жанры
        // По жанрам ничего больше не делаем - будем потом смотреть
        genres = parts[1].split(":");
    }

    /**
     * Получение авторов записи
     *
     * @return Авторы
     */
    public List<INPAuthor> getAuthors() {
        return authors;
    }

    /**
     * Название книги
     *
     * @return см.описание
     */
    public String getTitle() {
        return parts[2];
    }

    /**
     * Серия (если нет - то "")
     *
     * @return см.описание
     */
    public String getSerie() {
        return parts[3];
    }

    /**
     * Номер книги в серии (если нет, то "")
     *
     * @return см.описание
     */
    public String getSeriesNum() {
        return parts[4];
    }

    /**
     * Файл в библиотеке
     *
     * @return см.описание
     */
    public String getFile() {
        return parts[5];
    }

    /**
     * Признак того, что файл удален в библиотеке
     *
     * @return см.описание
     */
    public boolean isDeleted() {
        return "1".equals(parts[8]);
    }

    /**
     * Расширение файла в библиотеке
     *
     * @return см.описание
     */
    public String getExt() {
        return parts[9];
    }

    /**
     * Оценка в библиотеке
     *
     * @return см.описание
     */
    public int getLibRate() {
        try {
            if (parts.length < 13) {
                return 0;
            } else {
                return Integer.parseInt(parts[12]);
            }
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("");
        for (INPAuthor author : authors) {
            res.append(author.getFamily()).append(",").append(author.getName()).append(",").append(author.getLastName()).append(":");
        }
        res.append("; ");
        for (String genre : genres) {
            res.append(genre).append(":");
        }
        res.append("; ").append(getTitle()).append("; ").append(getSerie()).append("; ").append(getSeriesNum());
        res.append("; ").append(getFile()).append("; ").append(getExt()).append("; ").append(getLibRate());
        return res.toString();
    }

    /**
     * Класс, описывающий автора в записи INP
     */
    public static class INPAuthor {
        /**
         * Фамилия
         */
        private final String family;
        /**
         * Имя
         */
        private final String name;
        /**
         * Отчество
         */
        private final String lastName;

        /**
         * Конструктор
         *
         * @param fam   Фамилия
         * @param fName Имя
         * @param lName Отчество
         */
        public INPAuthor(String fam, String fName, String lName) {
            this.family = fam;
            this.name = fName;
            this.lastName = lName;
        }

        /**
         * Фамилия
         *
         * @return см.описание
         */
        public String getFamily() {
            return family;
        }

        /**
         * Имя
         *
         * @return см.описание
         */
        public String getName() {
            return name;
        }

        /**
         * Отчество
         *
         * @return см.описание
         */
        public String getLastName() {
            return lastName;
        }
    }

    /**
     * Заполняет параметры книги из текущей записи
     *
     * @param book Книга, которую заполнять
     * @return Заполненная из текущей записи книга
     */
    public Book fillBookFrom(final Book book, final Library library) {
        book.title = getTitle();
        book.serieName = getSerie();
        Integer numInSerie;
        try {
            numInSerie = Integer.valueOf(getSeriesNum());
        } catch (NumberFormatException e) {
            numInSerie = null;
        }
        book.numInSerie = numInSerie;
        // Поехали по авторам
        List<Author> tempAuthors = new ArrayList<>();
        //book.getAuthors().clear();
        for (INPAuthor inpAuthor : getAuthors()) {
            Author author = new Author();
            // Имя
            author.firstName = inpAuthor.getName();
            // Отчество
            author.middleName = inpAuthor.getLastName();
            // Фамилия
            author.lastName = inpAuthor.getFamily();
            // Ищем такое в БД
            Optional<Author> addedAuthor = Author.addIfNotExists(author, library);
            if (addedAuthor.isPresent()) {
                tempAuthors.add(addedAuthor.get());
            }
        }
        book.authors = tempAuthors;
        book.deletedInLibrary = isDeleted();
        return book;
    }

    /**
     * Exception в случае хренового формата записи
     */
	public static class BadINPRecord extends Exception {

        /**
         * Конструктор
         *
         * @param message Сообщение об ошибке
         */
        public BadINPRecord(String message) {
            super(message);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }
}
