package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexBooleanValue.TRUE;
import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexIdentifier.*;

@RequiredArgsConstructor
@Slf4j
class InMemoryTerminologyIndex implements TerminologyIndex {

    @NonNull
    private final Map<UUID, TerminologyEntry> terminologyEntries;

    @NonNull
    private final IndexSearcher terminologyIndexSearcher;

    @Override
    public Optional<TerminologyEntry> searchById(UUID entryId) {
        return Optional.ofNullable(terminologyEntries.get(entryId));
    }

    @Override
    public List<TerminologyEntry> searchSelectableEntries(String searchTerm) throws TerminologySearchException {
        var bootstrappedQueryBuilder = createBootstrappedQueryBuilder(searchTerm);
        return runSearchQuery(bootstrappedQueryBuilder.build());
    }

    @Override
    public List<TerminologyEntry> searchSelectableEntries(String searchTerm, UUID terminologyCategory)
            throws TerminologySearchException {
        var bootstrappedQueryBuilder = createBootstrappedQueryBuilder(searchTerm);

        var categoryQuery = new PhraseQuery.Builder()
                .add(new Term(CATEGORY, terminologyCategory.toString()))
                .build();

        return runSearchQuery(
                bootstrappedQueryBuilder.add(categoryQuery, BooleanClause.Occur.MUST)
                        .build());
    }

    /**
     * Creates a bootstrapped boolean query builder based on the given search term.
     * The result is equivalent to the following search query:
     * (display:"<searchTerm>" OR termCode:"<searchTerm>") AND selectable:"true"
     *
     * @param searchTerm The term that is searched for. It gets searched for this term within `display` and `termCode`.
     * @return A {@link BooleanQuery.Builder} bootstrapped with the mentioned search query.
     */
    private BooleanQuery.Builder createBootstrappedQueryBuilder(String searchTerm) {
        var displayQuery = new PrefixQuery(new Term(DISPLAY, searchTerm));
        var termCodeQuery = new PrefixQuery(new Term(TERM_CODE, searchTerm));

        var displayOrTermCodeSearchQuery = new BooleanQuery.Builder()
                .add(new BooleanQuery.Builder()
                                .add(new BooleanClause(displayQuery, BooleanClause.Occur.SHOULD))
                                .add(new BooleanClause(termCodeQuery, BooleanClause.Occur.SHOULD))
                                .build(),
                        BooleanClause.Occur.MUST)
                .build();

        var selectableSearchQuery = new PhraseQuery.Builder()
                .add(new Term(SELECTABLE, TRUE))
                .build();

        return new BooleanQuery.Builder()
                .add(displayOrTermCodeSearchQuery, BooleanClause.Occur.MUST)
                .add(selectableSearchQuery, BooleanClause.Occur.FILTER);
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
