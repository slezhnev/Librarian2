package ru.lsv.librarian2.library.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ru.lsv.librarian2.models.Author;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.library.LibraryUtils;

/**
 * Copied from Librrian.GWT
 * 
 * Парсер fb2-книг из потока <br/>
 * Тянуто из Librarian
 * 
 * @author s.lezhnev
 */
@SuppressWarnings({})
public class FB2BookParser extends DefaultHandler {

	private String tempVal;
	private Book tempBook = null;
	private Book retBook = null;
	private Author tempAuthor = null;

	/**
	 * Парсит fr2-книгу из входного потока
	 * 
	 * @param inStream
	 *            Входной поток с книгой
	 * @param id
	 *            ID книги в библиотеке
	 * @param zipFileName
	 *            Bмя ZIPфайла с книгой в библиотеке
	 * @param crc32
	 *            CRC32 книги (для удаления дублей)
	 * @return Сформированный экземпляр book или null в случае каких-либо
	 *         проблем с парсингом
	 * @throws SAXException
	 *             В случае ошибок парсинга книги (к примеру - во входной потоке
	 *             неверно сформированный fb2)
	 * @throws ParserConfigurationException
	 *             В случае отсутствия SAX парсера
	 * @throws IOException
	 *             В случае проблем чтения из потока
	 */
	public Book parseFB2Stream(InputStream inStream, String id,
			String zipFileName, long crc32) throws SAXException,
			ParserConfigurationException, IOException {
		// Вначале читаем все в StringBuffer. Читаем ВСЕ до </description>
		/*
		 * BufferedReader reader = new BufferedReader(new
		 * InputStreamReader(inStream)); StringBuffer str = new StringBuffer();
		 * String desc = "</description>"; while (inStream.available() > 0) {
		 * String inStr = reader.readLine(); int pos =
		 * inStr.toLowerCase().indexOf(desc); if (pos > -1) {
		 * str.append(str.substring(0, pos + desc.length())); } else {
		 * str.append(inStr); } } str.append("</FictionBook>");
		 */
		//
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser parser = spf.newSAXParser();
		try {
			parser.parse(inStream, this);
		} catch (SAXException ex) {
		}
		// parser.parse(new InputSource(new StringReader(str.toString())),
		// this);
		if (retBook != null) {
			retBook.id = id;
			retBook.zipFileName = zipFileName;
			retBook.crc32 = crc32;
		}
		return retBook;
	}

	/**
	 * См. @org.xml.sax.helpers.DefaultHandler
	 * 
	 * @param uri
	 *            См. {@link org.xml.sax.helpers.DefaultHandler}
	 * @param localName
	 *            См. {@link org.xml.sax.helpers.DefaultHandler}
	 * @param qName
	 *            См. {@link org.xml.sax.helpers.DefaultHandler}
	 * @param attributes
	 *            См. {@link org.xml.sax.helpers.DefaultHandler}
	 * @throws SAXException
	 *             См. {@link org.xml.sax.helpers.DefaultHandler}
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		tempVal = "";
		if (qName.equalsIgnoreCase("title-info")) {
			tempBook = new Book();
		} else if (qName.equalsIgnoreCase("author")) {
			if (tempBook != null)
				tempAuthor = new Author();
		} else if (qName.equalsIgnoreCase("sequence")) {
			if (tempBook != null) {
				tempBook.serieName = attributes.getValue("name");
				try {
					tempBook.numInSerie = Integer.parseInt(attributes
							.getValue("number"));
				} catch (NumberFormatException ignored) {
				}
			}
		}
	}

	/**
	 * См. @org.xml.sax.helpers.DefaultHandler
	 * 
	 * @param ch
	 *            См. @org.xml.sax.helpers.DefaultHandler
	 * @param start
	 *            См. @org.xml.sax.helpers.DefaultHandler
	 * @param length
	 *            См. @org.xml.sax.helpers.DefaultHandler
	 * @throws SAXException
	 *             См. @org.xml.sax.helpers.DefaultHandler
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		tempVal = new String(ch, start, length);
	}

	/**
	 * См. @org.xml.sax.helpers.DefaultHandler
	 * 
	 * @param uri
	 *            См. @org.xml.sax.helpers.DefaultHandler
	 * @param localName
	 *            См. @org.xml.sax.helpers.DefaultHandler
	 * @param qName
	 *            См. @org.xml.sax.helpers.DefaultHandler
	 * @throws SAXException
	 *             См. @org.xml.sax.helpers.DefaultHandler
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equalsIgnoreCase("title-info")) {
			retBook = tempBook;
			tempBook = null;
		} else if (qName.equalsIgnoreCase("genre")) {
			if (tempBook != null)
				tempBook.genre = tempVal;
		} else if (qName.equalsIgnoreCase("first-name")) {
			if (tempAuthor != null)
				tempAuthor.firstName = tempVal;
		} else if (qName.equalsIgnoreCase("middle-name")) {
			if (tempAuthor != null)
				tempAuthor.middleName = tempVal;
		} else if (qName.equalsIgnoreCase("last-name")) {
			if (tempAuthor != null)
				tempAuthor.lastName = tempVal;
		} else if (qName.equalsIgnoreCase("author")) {
			if (tempBook != null) {
				// Дополнительно уже прямо тут будем обрабатывать сохранение в
				// DB!
				Optional<Author> addedAuthor = Author.addIfNotExists(tempAuthor, LibraryUtils.getCurrentLibrary());
				if (addedAuthor.isPresent())
					tempBook.authors.add(addedAuthor.get());
				tempAuthor = null;
			}
		} else if (qName.equalsIgnoreCase("book-title")) {
			if (tempBook != null)
				tempBook.title = tempVal;
		} else if (qName.equalsIgnoreCase("lang")) {
			if (tempBook != null)
				tempBook.language = tempVal;
		} else if (qName.equalsIgnoreCase("src-lang")) {
			if (tempBook != null)
				tempBook.sourceLanguage = tempVal;
		} else if (qName.equalsIgnoreCase("annotation")) {
			if (tempBook != null)
				tempBook.annotation = tempVal;
		}

	}

}
