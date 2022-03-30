package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.*;

import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexFieldBooleanValue.TRUE;
import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexField.*;

/**
 * Index of a terminology that is held in memory.
 */
@RequiredArgsConstructor
@Slf4j
class InMemoryTerminologyIndex implements TerminologyIndex {

    @NonNull
    private final Map<UUID, TerminologyEntry> terminologyEntries;

    @NonNull
    private final IndexSearcher terminologyIndexSearcher;

    @NonNull
    private final QueryParser indexSearchQueryParser;

    @Override
    public Optional<TerminologyEntry> searchById(UUID entryId) {
        return Optional.ofNullable(terminologyEntries.get(entryId));
    }

    @Override
    public List<TerminologyEntry> searchSelectableEntries(String searchTerm) throws TerminologySearchException {
        try {
            // TODO: highly controversial due to readability - please discuss in review!
            var searchQueryTemplate = "(%s:\"%s\" OR %s:\"%s\") AND %s:\"%s\"";
            var searchQuery = indexSearchQueryParser.parse(searchQueryTemplate.formatted(DISPLAY, searchTerm,
                    TERM_CODE, searchTerm, SELECTABLE, TRUE));
            return runSearchQuery(searchQuery);
        } catch (ParseException e) {
            throw new TerminologySearchException("cannot parse search query", e);
        }
    }

    @Override
    public List<TerminologyEntry> searchSelectableEntries(String searchTerm, UUID terminologyCategoryId)
            throws TerminologySearchException {

        try {
            // TODO: highly controversial due to readability - please discuss in review!
            var searchQueryTemplate = "(%s:\"%s\" OR %s:\"%s\") AND %s:\"%s\" AND %s:\"%s\"";
            var searchQuery = indexSearchQueryParser.parse(searchQueryTemplate.formatted(DISPLAY, searchTerm,
                    TERM_CODE, searchTerm, SELECTABLE, TRUE, CATEGORY, terminologyCategoryId.toString()));
            return runSearchQuery(searchQuery);
        } catch (ParseException e) {
            throw new TerminologySearchException("cannot parse search query", e);
        }
    }

    /**
     * Runs the {@link Query} against this index and returns any matching {@link TerminologyEntry}s.
     *
     * @param searchQuery The search query to be used.
     * @return List of matching terminology entries.
     * @throws TerminologySearchException If an error occurs while searching the index.
     */
    private List<TerminologyEntry> runSearchQuery(Query searchQuery) throws TerminologySearchException {
        try {
            // TODO: This should be adjustable. However, at this point we are mimicking former implementation details.
            //       Thus, this should be targeted in a separate issue.
            var topResults = terminologyIndexSearcher.search(searchQuery, 20);
            return lookupTerminologyEntries(topResults);
        } catch (IOException e) {
            throw new TerminologySearchException("error while searching in index with search query: '%s'"
                    .formatted(searchQuery), e);
        }
    }

    /**
     * Looks up terminology entries based on the results of a search.
     *
     * @param docs Results of an index search.
     * @return The actual {@link TerminologyEntry}s associated with the results of an index search.
     * @throws IOException If the document can not be lookup up within the index.
     */
    private List<TerminologyEntry> lookupTerminologyEntries(TopDocs docs) throws IOException {
        var lookedUpTerminologyEntries = new ArrayList<TerminologyEntry>();
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            var doc = terminologyIndexSearcher.doc(scoreDoc.doc);
            var terminologyEntryId = doc.get(ID);
            lookedUpTerminologyEntries.add(terminologyEntries.get(UUID.fromString(terminologyEntryId)));
        }

        return lookedUpTerminologyEntries;
    }
}
