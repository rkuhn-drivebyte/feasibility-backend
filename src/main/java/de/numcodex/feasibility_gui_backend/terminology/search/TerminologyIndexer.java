package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexField.*;
import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexFieldBooleanValue.FALSE;
import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexFieldBooleanValue.TRUE;

/**
 * An indexer for indexing {@link TerminologyEntry}s.
 */
@Slf4j
@RequiredArgsConstructor
class TerminologyIndexer {

    @NonNull
    private final IndexWriterConfig indexWriterCfg;

    // TODO: changes state - better revise this!
    /**
     * Indexes the given categorized {@link TerminologyEntry}s within the given {@link Directory}.
     *
     * @param indexDirectory     Storage object for the index.
     * @param categorizedEntries Categorized terminology entries. Entries should be flat, i.e. they should not resemble
     *                           a tree. If they are given as a tree then only the root element gets indexed.
     * @return The directory with populated indexes.
     * @throws IOException If an error occurs during indexing.
     */
    void indexEntries(Directory indexDirectory, Map<CategoryEntry, List<TerminologyEntry>> categorizedEntries)
            throws IOException {
        var indexWriter = new IndexWriter(indexDirectory, indexWriterCfg);

        for (Map.Entry<CategoryEntry, List<TerminologyEntry>> entriesByCategory : categorizedEntries.entrySet()) {
            log.info("indexing %d terminology entries in category '%s'".formatted(entriesByCategory.getValue().size(),
                    entriesByCategory.getKey().getDisplay()));

            for (TerminologyEntry terminologyEntry : entriesByCategory.getValue()) {
                indexWriter.addDocument(createIndexDocument(terminologyEntry, entriesByCategory.getKey()));
            }
        }

        indexWriter.flush();
        indexWriter.close();
    }

    /**
     * Indexes a single {@link TerminologyEntry}.
     *
     * @param entry    The {@link TerminologyEntry} that gets indexed.
     * @param category Category that the {@link TerminologyEntry} is associated with.
     */
    private Document createIndexDocument(TerminologyEntry entry, CategoryEntry category) {
        var doc = new Document();
        doc.add(new StoredField(ID, entry.getId().toString()));
        doc.add(new TextField(DISPLAY, entry.getDisplay(), Field.Store.YES));
        doc.add(new TextField(SELECTABLE, entry.isSelectable() ? TRUE : FALSE, Field.Store.YES));
        doc.add(new TextField(CATEGORY, category.getCatId().toString(), Field.Store.YES));

        if (entry.getTermCode() != null) {
            doc.add(new TextField(TERM_CODE, entry.getTermCode().getCode(), Field.Store.YES));
        }

        return doc;
    }

    /**
     *
     */
    static class IndexField {
        static final String ID = "id";
        static final String DISPLAY = "display";
        static final String TERM_CODE = "termCode";
        static final String SELECTABLE = "selectable";
        static final String CATEGORY = "category";
    }

    static class IndexFieldBooleanValue {
        static final String TRUE = "true";
        static final String FALSE = "false";
    }
}
