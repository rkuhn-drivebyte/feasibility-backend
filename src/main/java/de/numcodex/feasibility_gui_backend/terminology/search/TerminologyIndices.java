package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    private final Analyzer analyzer;

    @NonNull
    private final TerminologyIndexer terminologyIndexer;

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
        indexEntries(inMemoryDirectory, entries, analyzer);

        var indexSearcher = createIndexSearcher(inMemoryDirectory);
        return new InMemoryTerminologyIndex(mapUncategorizedEntriesToIdentifier(entries), indexSearcher);
    }

    /**
     * Indexes the given categorized {@link TerminologyEntry}s using the given {@link Directory}.
     * The {@link Analyzer} performs operations on eligible index attributes of each {@link TerminologyEntry} before
     * the actual indexing.
     *
     * @param indexDirectory     Storage object for the index.
     * @param categorizedEntries Categorized terminology entries. Entries should be flat, i.e. they should not resemble
     *                           a tree. If they are given as a tree then only the root element gets indexed.
     * @param tokenAnalyzer      Operator for eligible index attributes.
     * @throws IOException If an error occurs during indexing.
     */
    private void indexEntries(Directory indexDirectory, Map<CategoryEntry, List<TerminologyEntry>> categorizedEntries,
                              Analyzer tokenAnalyzer) throws IOException {
        var directoryWriter = new IndexWriter(indexDirectory, new IndexWriterConfig(tokenAnalyzer));

        for (Entry<CategoryEntry, List<TerminologyEntry>> entriesByCategory : categorizedEntries.entrySet()) {
            log.info("indexing %d terminology entries in category '%s'".formatted(entriesByCategory.getValue().size(),
                    entriesByCategory.getKey().getDisplay()));

            for (TerminologyEntry terminologyEntry : entriesByCategory.getValue()) {
                terminologyIndexer.index(directoryWriter, terminologyEntry, entriesByCategory.getKey());
            }
        }

        directoryWriter.flush();
        directoryWriter.close();
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
