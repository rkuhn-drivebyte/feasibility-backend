package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.common.api.TermCode;
import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@Import({TerminologySearchSpringConfig.class})
public class InMemoryTerminologyIndexIT {

    private static final String TERM_CODE_1_CODE = "Test1";
    private static final String TERM_CODE_1_1_CODE = "Test1.1";
    private static final String TERM_CODE_2_CODE = "Test2";
    private static final String TERM_CODE_2_1_CODE = "Test2.1";
    private static final String TERM_CODE_3_CODE = "Test3";
    private static final UUID TERMINOLOGY_ENTRY_1_ID = UUID.fromString("7f170980-a0a9-4c42-806a-9dbdaf553229");
    private static final String TERMINOLOGY_ENTRY_1_DISPLAY = "Blutgefäß";
    private static final UUID TERMINOLOGY_ENTRY_1_1_ID = UUID.fromString("cfb6b8a6-24f9-4659-b089-cb243b6a1b63");
    private static final String TERMINOLOGY_ENTRY_1_1_DISPLAY = "Blutgefäßwand";
    private static final UUID TERMINOLOGY_ENTRY_2_ID = UUID.fromString("4c852757-2052-42d4-b7e9-c2fab9d6c881");
    private static final String TERMINOLOGY_ENTRY_2_DISPLAY = "Gehirn";
    private static final UUID TERMINOLOGY_ENTRY_2_1_ID = UUID.fromString("33ab5e1d-28e2-4bb1-8d0b-ea9e40e9380b");
    private static final String TERMINOLOGY_ENTRY_2_1_DISPLAY = "Gehirnwasser";
    private static final UUID TERMINOLOGY_ENTRY_3_ID = UUID.fromString("759f50b4-55be-476c-80e9-eb17819bac5d");
    private static final String TERMINOLOGY_ENTRY_3_DISPLAY = "Auge";
    private static final UUID CATEGORY_1_ID = UUID.fromString("f093378f-f1c2-448e-bb13-4aa2fb01692b");
    private static final UUID CATEGORY_2_ID = UUID.fromString("2bb6b12b-c0b6-4b8e-b13f-fc6499d71fe3");

    private TerminologyEntry terminologyEntry1;
    private TerminologyEntry terminologyEntry1_1;
    private TerminologyEntry terminologyEntry2;
    private TerminologyEntry terminologyEntry2_1;
    private TerminologyEntry terminologyEntry3;

    @Autowired
    private TerminologyIndices terminologyIndexFactory;

    private TerminologyIndex inMemoryTerminologyIndex;

    /**
     * Creates a Terminology hierarchy that is as follows:
     *
     * CATEGORY_1
     *  |
     *  +---+ TERMINOLOGY_ENTRY_1
     *      |   |
     *      |   +---+ TERMINOLOGY_ENTRY_1_1
     *      |
     *      + TERMINOLOGY_ENTRY_1_1 (separately indexed)
     *      |
     *      + TERMINOLOGY_ENTRY_2
     *          |
     *          +---+ TERMINOLOGY_ENTRY_2_1
     * CATEGORY_2
     *  |
     *  +---+ TERMINOLOGY_ENTRY_3
     *
     * @throws IOException If setting up the test environment fails.
     */
    @BeforeEach
    public void setUp() throws IOException {
        var testCategory1 = createTestCategoryEntry(CATEGORY_1_ID);
        var testCategory2 = createTestCategoryEntry(CATEGORY_2_ID);

        terminologyEntry1 = createTestTerminologyEntry(TERMINOLOGY_ENTRY_1_ID, TERMINOLOGY_ENTRY_1_DISPLAY,
                TERM_CODE_1_CODE, true);
        terminologyEntry1_1 = createTestTerminologyEntry(TERMINOLOGY_ENTRY_1_1_ID, TERMINOLOGY_ENTRY_1_1_DISPLAY,
                TERM_CODE_1_1_CODE, false);
        terminologyEntry1.setChildren(List.of(terminologyEntry1_1));

        terminologyEntry2 = createTestTerminologyEntry(TERMINOLOGY_ENTRY_2_ID, TERMINOLOGY_ENTRY_2_DISPLAY,
                TERM_CODE_2_CODE, true);
        terminologyEntry2_1 = createTestTerminologyEntry(TERMINOLOGY_ENTRY_2_1_ID, TERMINOLOGY_ENTRY_2_1_DISPLAY,
                TERM_CODE_2_1_CODE, false);
        terminologyEntry2.setChildren(List.of(terminologyEntry2_1));

        terminologyEntry3 = createTestTerminologyEntry(TERMINOLOGY_ENTRY_3_ID, TERMINOLOGY_ENTRY_3_DISPLAY,
                TERM_CODE_3_CODE, true);


        var categorizedTerminologyEntries = Map.of(
                testCategory1, List.of(terminologyEntry1, terminologyEntry1_1, terminologyEntry3),
                testCategory2, List.of(terminologyEntry2)
        );

        inMemoryTerminologyIndex = terminologyIndexFactory.createInMemory(categorizedTerminologyEntries);
    }

    @Test
    public void testSearchById_ReturnAbsentIfIdentifierIsUnknown() {
        var unknownTerminologyEntryIdentifier = UUID.fromString("65240192-2ac5-4869-9552-0ea476e3826e");
        var searchResult = inMemoryTerminologyIndex.searchById(unknownTerminologyEntryIdentifier);

        assertTrue(searchResult.isEmpty());
    }

    @Test
    public void testSearchById_ReturnsEntryIfFound() {
        var searchResult = inMemoryTerminologyIndex.searchById(TERMINOLOGY_ENTRY_1_ID);

        assertTrue(searchResult.isPresent());
        assertEquals(terminologyEntry1, searchResult.get());
    }

    @Test
    public void testSearchById_ChildrenAreNotIndexedUnlessHavingSeparateEntry() {
        assertTrue(inMemoryTerminologyIndex.searchById(TERMINOLOGY_ENTRY_2_1_ID).isEmpty());
        assertTrue(inMemoryTerminologyIndex.searchById(TERMINOLOGY_ENTRY_1_1_ID).isPresent());
    }

//    public void testSearchSelectableEntries_


    /*
        Additional test cases:
        - unselectable entries are not found
     */

    @Test
    public void testSearchSelectableEntries_UnselectableEntriesAreNotFound() throws TerminologySearchException {
        var searchResult = inMemoryTerminologyIndex.searchSelectableEntries("blut");

        assertEquals(1, searchResult.size());
//        assertTrue(searchResult.isEmpty());
    }

//    public void

    private TerminologyEntry createTestTerminologyEntry(UUID id, String display, String termCodeCode,
                                                        boolean selectable) {
        var termCode = new TermCode();
        termCode.setCode(termCodeCode);
        termCode.setDisplay("Test Term Code");
        termCode.setSystem("https://my.test.term.code");
        termCode.setVersion("1.0");


        var terminologyEntry = new TerminologyEntry();
        terminologyEntry.setId(id);
        terminologyEntry.setChildren(List.of());
        terminologyEntry.setDisplay(display);
        terminologyEntry.setTermCode(termCode);
        terminologyEntry.setTermCodes(List.of());
        terminologyEntry.setLeaf(true);
        terminologyEntry.setSelectable(selectable);
        terminologyEntry.setTimeRestrictionAllowed(false);
        terminologyEntry.setValueDefinitions(List.of());
        terminologyEntry.setAttributeDefinitions(List.of());

        return terminologyEntry;
    }

    private CategoryEntry createTestCategoryEntry(UUID id) {
        return new CategoryEntry(id, "test category");
    }
}
