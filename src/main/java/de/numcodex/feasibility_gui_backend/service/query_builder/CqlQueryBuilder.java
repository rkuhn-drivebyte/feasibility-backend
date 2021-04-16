package de.numcodex.feasibility_gui_backend.service.query_builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.feasibility_gui_backend.model.query.StructuredQuery;
import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.Translator;
import de.numcodex.sq2cql.model.cql.Library;
import de.numcodex.sq2cql.model.structured_query.TranslationException;

import java.util.Objects;

public class CqlQueryBuilder implements QueryBuilder {

    private final Translator translator;
    private final ObjectMapper objectMapper;

    public CqlQueryBuilder(Translator translator, ObjectMapper objectMapper) {
        this.translator = Objects.requireNonNull(translator);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public String getQueryContent(StructuredQuery query) throws QueryBuilderException {
        try {
            Library library = translator.toCql(translateQuery(query));
            return library.print(PrintContext.ZERO);
        } catch (TranslationException e) {
            throw new QueryBuilderException("problem while translating a structured query into a CQL library", e);
        }
    }

    private de.numcodex.sq2cql.model.structured_query.StructuredQuery translateQuery(StructuredQuery query) throws QueryBuilderException {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(query),
                        de.numcodex.sq2cql.model.structured_query.StructuredQuery.class);
        } catch (JsonProcessingException e) {
            throw new QueryBuilderException("problem while transforming structured query", e);
        }
    }
}
