package ru.lsv.librarian2.models;

import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class to convert list of Book to tree view
 */
public class TreeProcessor {

    public static class TreeNode {
        public String name;
        public List<TreeNode> children;
        public Integer bookId;
        public Boolean readed;
        public Boolean mustRead;
        public Boolean deletedInLibrary;

        TreeNode(String serieName) {
            this.name = serieName;
            children = new ArrayList<>();
            this.bookId = null;
        }

        TreeNode(Book book, Integer userId) {
            this.name = book.titleWithSerie();
            children = new ArrayList<>();
            this.bookId = book.bookId;
            this.readed = book.isReaded(userId);
            this.mustRead = book.isMustRead(userId);
            this.deletedInLibrary = book.deletedInLibrary;
        }
    }

    private static final Logger log = Logger.getLogger(TreeProcessor.class);

    /**
     * Books should be ALREADY ORDERED BY serieName and numInSeries!
     */
    public static List<TreeNode> convertToTree(List<Book> books, Integer userId) {
        Book prevSerie = null;
        TreeNode serieNode = null;
        List<TreeNode> res = new LinkedList<>();
        for (Book book : books) {
            if (book.serieName != null && !book.serieName.isBlank()) {
                if (prevSerie != null && prevSerie.serieName != null) {
                    if (prevSerie.serieName.equals(book.serieName)) {
                        // It is a switch to check that serieNode exists
                        if (serieNode == null) {
                            // It is a strange case - serieNode SHOULD be created already
                            log.errorf("treeNode does not exists for serie '%s'. prevSerie.bookId: '%s'", book.serieName, prevSerie.bookId);
                            serieNode = new TreeNode(book.serieName);
                            res.add(serieNode);
                        }
                    } else {
                        // It is a new serie
                        serieNode = new TreeNode(book.serieName);
                        res.add(serieNode);
                    }
                    serieNode.children.add(new TreeNode(book, userId));
                } else {
                    // We start to track a first serie
                    serieNode = new TreeNode(book.serieName);
                    res.add(serieNode);
                    serieNode.children.add(new TreeNode(book, userId));
                }
            } else {
                // This is the book without serie
                res.add(new TreeNode(book, userId));
            }
            prevSerie = book;
        }
        return res;
    }

}