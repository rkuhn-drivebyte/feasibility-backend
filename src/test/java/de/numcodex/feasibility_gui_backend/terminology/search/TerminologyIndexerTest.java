package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.common.api.TermCode;
import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndexer.IndexField.TERM_CODE;
import static org.junit.jupiter.api.Assertions.*;

public class TerminologyIndexerTest {

    private static TerminologyIndexer terminologyIndexer;

    @BeforeAll
    public static void setUp() throws IOException {
        var analyzer = CustomAnalyzer.builder()
                .withTokenizer("whitespace")
                .addTokenFilter("lowercase")
                .build();

        terminologyIndexer = new TerminologyIndexer(analyzer);
    }

    @Test
    public void testIndexEntries_EmptyCollectionDoesNotGetIndexed() throws IOException {
        var directory = (Directory)new ByteBuffersDirectory();
        directory = terminologyIndexer.indexEntries(directory, Map.of());

        assertEquals(0, directory.listAll().length);
    }


//    @Test
//    public void testIndex_FailsWhenIndexWriterIsAlreadyClosed() throws IOException {
//        var terminologyEntry = createDefaultTestTerminologyEntry();
//        var categoryEntry = createDefaultCategoryEntry();
//
//        var indexWriter = new IndexWriter(new ByteBuffersDirectory(), new IndexWriterConfig());
//        indexWriter.close();
//
//        assertThrows(AlreadyClosedException.class, () -> terminologyIndexer.index(indexWriter, terminologyEntry,
//                categoryEntry));
//    }
//
//    @Test
//    public void testIndex_TermCodeFieldsOnlyGetsCreatedIfEntryDoesHaveOne() throws IOException {
//        var terminologyEntryWithoutTermCode = createDefaultTestTerminologyEntry();
//        terminologyEntryWithoutTermCode.setTermCode(null);
//        var terminologyEntryWithTermCode = createDefaultTestTerminologyEntry();
//
//        var directory = new ByteBuffersDirectory();
//        var indexWriter = new IndexWriter(directory, new IndexWriterConfig());
//
//        terminologyIndexer.index(indexWriter, terminologyEntryWithoutTermCode, createDefaultCategoryEntry());
//        assertFalse(indexWriter.getFieldNames().contains(TERM_CODE));
//
//        terminologyIndexer.index(indexWriter, terminologyEntryWithTermCode, createDefaultCategoryEntry());
//        assertTrue(indexWriter.getFieldNames().contains(TERM_CODE));
//    }
//
//    @Test
//    public void testIndex_IndexIdentifierFieldsAreUsed() throws IOException, IllegalAccessException {
//        var terminologyEntry = createDefaultTestTerminologyEntry();
//        var categoryEntry = createDefaultCategoryEntry();
//
//        var indexWriter = new IndexWriter(new ByteBuffersDirectory(), new IndexWriterConfig());
//        terminologyIndexer.index(indexWriter, terminologyEntry, categoryEntry);
//
//        var fields = TerminologyIndexer.IndexField.class.getDeclaredFields();
//        var indexIdentifierClass = TerminologyIndexer.IndexField.class;
//
//        for (Field field : fields) {
//            var fieldValue = field.get(indexIdentifierClass);
//            assertTrue(indexWriter.getFieldNames().contains(fieldValue));
//        }
//    }

    private TerminologyEntry createDefaultTestTerminologyEntry() {
        var termCode = new TermCode();
        termCode.setCode("Test1.2");
        termCode.setDisplay("Test Term Code");
        termCode.setSystem("https://my.test.term.code");
        termCode.setVersion("1.0");


        var terminologyEntry = new TerminologyEntry();
        terminologyEntry.setId(UUID.randomUUID());
        terminologyEntry.setChildren(List.of());
        terminologyEntry.setDisplay("Test");
        terminologyEntry.setTermCode(termCode);
        terminologyEntry.setTermCodes(List.of());
        terminologyEntry.setLeaf(true);
        terminologyEntry.setSelectable(true);
        terminologyEntry.setTimeRestrictionAllowed(false);
        terminologyEntry.setValueDefinitions(List.of());
        terminologyEntry.setAttributeDefinitions(List.of());

        return terminologyEntry;
    }

    private CategoryEntry createDefaultCategoryEntry() {
        return new CategoryEntry(UUID.fromString("9f65df10-6827-486b-b3d5-926c03af15a9"), "test");
    }
}
