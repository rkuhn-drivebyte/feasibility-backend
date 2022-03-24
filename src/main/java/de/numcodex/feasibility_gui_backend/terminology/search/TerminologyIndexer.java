package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;

import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexBooleanValue.FALSE;
import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexBooleanValue.TRUE;
import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexIdentifier.*;

/**
 * An indexer for indexing a single {@link TerminologyEntry}.
 */
class TerminologyIndexer {

    /**
     * Indexes a single {@link TerminologyEntry} using the given {@link IndexWriter}.
     *
     * @param directoryWriter Writer for writing index information to the index storage.
     * @param entry           The {@link TerminologyEntry} that gets indexed.
     * @param category        Category that the {@link TerminologyEntry} is associated with.
     * @throws IOException If an error occurs during indexing.
     */
    void index(IndexWriter directoryWriter, TerminologyEntry entry, CategoryEntry category) throws IOException {
        var doc = new Document();
        doc.add(new StoredField(ID, entry.getId().toString()));
        doc.add(new TextField(DISPLAY, entry.getDisplay(), Field.Store.YES));
        doc.add(new TextField(SELECTABLE, entry.isSelectable() ? TRUE : FALSE, Field.Store.YES));
        doc.add(new TextField(CATEGORY, category.getCatId().toString(), Field.Store.YES));

        if (entry.getTermCode() != null) {
            doc.add(new TextField(TERM_CODE, entry.getTermCode().getCode(), Field.Store.YES));
        }

        directoryWriter.addDocument(doc);
    }

    static class IndexIdentifier {
        static final String ID = "id";
        static final String DISPLAY = "display";
        static final String TERM_CODE = "termCode";
        static final String SELECTABLE = "selectable";
        static final String CATEGORY = "category";
    }

    static class IndexBooleanValue {
        static final String TRUE = "true";
        static final String FALSE = "false";
    }
}
