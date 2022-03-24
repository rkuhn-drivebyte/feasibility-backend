package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class TerminologySearchSpringConfig {

    /**
     * Creates a factory for creating indexes of {@link TerminologyEntry}s.
     *
     * @param analyzer           An analyzer for indexing a terminology.
     * @param terminologyIndexer An indexer for {@link TerminologyEntry}s.
     * @return Factory for creating terminology indexes.
     */
    @Bean
    public TerminologyIndices createTerminologyIndexFactory(Analyzer analyzer, TerminologyIndexer terminologyIndexer) {
        return new TerminologyIndices(analyzer, terminologyIndexer);
    }

    /**
     * Creates an analyzer for indexing a terminology.
     * This analyzer runs operations on strings that shall be indexed BEFORE they are indexed. In other words, an
     * analyzer tokenizes strings in a certain fashion.
     * <p>
     * For our use case we are going to create tokens whenever a whitespaces occurs within a string. Furthermore,
     * this string gets transformed to be all lowercase.
     *
     * @return An analyzer for indexing a terminology.
     * @throws IOException If a tokenizer can not be found.
     */
    @Bean
    public Analyzer createAnalyzer() throws IOException {
        return CustomAnalyzer.builder()
                .withTokenizer("whitespace")
                .addTokenFilter("lowercase")
                .build();
    }

    /**
     * Creates an indexer that is capable of indexing a sinlge {@link TerminologyEntry}.
     *
     * @return A terminology indexer.
     */
    @Bean
    public TerminologyIndexer createTerminologyIndexer() {
        return new TerminologyIndexer();
    }
}
