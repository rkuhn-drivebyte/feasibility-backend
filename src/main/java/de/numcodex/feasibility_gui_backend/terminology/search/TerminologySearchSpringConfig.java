package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexField.DISPLAY;

@Configuration
public class TerminologySearchSpringConfig {

    // TODO: revise doc

    /**
     * Creates a factory for creating indexes of {@link TerminologyEntry}s.
     *
     * @param terminologyIndexer An indexer for {@link TerminologyEntry}s.
     * @return Factory for creating terminology indexes.
     */
    @Bean
    public TerminologyIndices createTerminologyIndexFactory(TerminologyIndexer terminologyIndexer,
                                                            QueryParser indexSearchQueryParser) {
        return new TerminologyIndices(terminologyIndexer, indexSearchQueryParser);
    }

    /**
     * Creates an indexer that is capable of indexing {@link TerminologyEntry}s.
     *
     * @param analyzer An analyzer for tokenization of input that shall be indexed.
     * @return A terminology indexer.
     */
    @Bean
    public TerminologyIndexer createTerminologyIndexer(Analyzer analyzer) {
        return new TerminologyIndexer(analyzer);
    }

    /**
     * Creates a {@link QueryParser} that is capable of parsing Lucene search queries in their string representation.
     *
     * @param analyzer An analyzer for tokenization of search terms. Should be the same as used for indexing.
     * @return A Lucene search query parser.
     */
    @Bean
    public QueryParser createIndexSearchQueryParser(Analyzer analyzer) {
        return new QueryParser(DISPLAY, analyzer);
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
}
