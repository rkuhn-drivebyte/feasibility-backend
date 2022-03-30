package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for creating a {@link TerminologyIndex}.
 */
@RequiredArgsConstructor
@Slf4j
public class TerminologyIndices {

    @NonNull
    private final TerminologyIndexer terminologyIndexer;

    @NonNull
    private final QueryParser indexSearchQueryParser;

    /**
     * Creates an in-memory {@link TerminologyIndex} of the given terminology entries.
     *
     * @param entries Categorized terminology entries. Entries should be flat, i.e. they should not resemble a tree.
     *                If they are given as a tree then only the root element gets indexed.
     * @return An in-memory {@link TerminologyIndex}.
     * @throws IOException If an error occurs during indexing.
     */
    public TerminologyIndex createInMemory(Map<CategoryEntry, List<TerminologyEntry>> entries)
            throws IOException {
        log.info("creating in memory terminology index");
        var inMemoryDirectory = new ByteBuffersDirectory();
        terminologyIndexer.indexEntries(inMemoryDirectory, entries);

        var indexSearcher = createIndexSearcher(inMemoryDirectory);
        return new InMemoryTerminologyIndex(mapUncategorizedEntriesToIdentifier(entries), indexSearcher,
                indexSearchQueryParser);
    }

    /**
     * Creates an {@link IndexSearcher} allowing to search the index storage that the given {@link Directory} defines.
     *
     * @param searchDirectory Storage object for the index.
     * @return The {@link IndexSearcher} for the {@link Directory}.
     * @throws IOException If an error occurs while trying to access the {@link Directory}.
     */
    private IndexSearcher createIndexSearcher(Directory searchDirectory) throws IOException {
        var indexReader = DirectoryReader.open(searchDirectory);
        return new IndexSearcher(indexReader);
    }

    /**
     * Maps all categorized {@link TerminologyEntry}s within the given collection to their unique identifier.
     * Category information are dropped.
     *
     * @param categorizedTerminologyEntries Categorized terminology entries. Entries should be flat, i.e. they should
     *                                      not resemble a tree.
     * @return All {@link TerminologyEntry}s mapped to their unique identifier.
     */
    private Map<UUID, TerminologyEntry> mapUncategorizedEntriesToIdentifier(
            Map<CategoryEntry, List<TerminologyEntry>> categorizedTerminologyEntries) {
        return categorizedTerminologyEntries.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toMap(TerminologyEntry::getId, Function.identity()));
    }
}
