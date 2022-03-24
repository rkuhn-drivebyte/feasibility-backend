package de.numcodex.feasibility_gui_backend.terminology;

import de.numcodex.feasibility_gui_backend.terminology.search.TerminologySearchSpringConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("terminology")
@ExtendWith(SpringExtension.class)
@Import({
        TerminologySpringConfig.class,
        TerminologySearchSpringConfig.class
        })
@WebMvcTest(
        controllers = TerminologyRestController.class,
        properties = {
                "app.ontologyFolder=src/test/resources/de.numcodex.feasibility_gui_backend.terminology.ui_profiles"
        }
)
@SuppressWarnings("NewClassNamingConvention")
public class TerminologyRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRootEntries_SucceedsWith200() throws Exception {
        mockMvc.perform(get("/api/v1/terminology/root-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$..display", containsInAnyOrder("Biobank", "Prozedur")));
    }

    // TODO: This should be revised in a separate issue - why is the response body empty?!
    //       There is no indication that something has not been found.
    @Test
    public void testEntryById_SucceedsIfNotFoundWithEmpty200() throws Exception {
        var nonExistingEntryId = "654046bb-cf81-4a5b-a24b-be4c5743e842";
        mockMvc.perform(get("/api/v1/terminology/entries/{nodeId}", nonExistingEntryId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void testEntryById_SucceedsWith200() throws Exception {
        // this UUID is part of the Prozedur.json file within the test resources directory
        var operations = "17b15d41-33c2-e606-531c-1354fb95efe9";
        mockMvc.perform(get("/api/v1/terminology/entries/{nodeId}", operations))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.id", is("17b15d41-33c2-e606-531c-1354fb95efe9")))
                .andExpect(jsonPath("$.leaf", is(false)))
                .andExpect(jsonPath("$.selectable", is(true)))
                .andExpect(jsonPath("$.timeRestrictionAllowed", is(true)))
                .andExpect(jsonPath("$.valueDefinitions", hasSize(0)))
                .andExpect(jsonPath("$.attributeDefinitions", hasSize(0)))
                .andExpect(jsonPath("$.display", is("OPERATIONEN")))
                .andExpect(jsonPath("$.termCodes", hasSize(1)))
                .andExpect(jsonPath("$.termCodes[0].code", is("5")))
                .andExpect(jsonPath("$.termCodes[0].display", is("OPERATIONEN")))
                .andExpect(jsonPath("$.termCodes[0].system", is("http://fhir.de/CodeSystem/dimdi/ops")))
                .andExpect(jsonPath("$.termCodes[0].version", is("2021")))
                .andExpect(jsonPath("$.children", hasSize(1)))
                .andExpect(jsonPath("$.children[0].id", is("d226b0ce-c7fc-4eb2-af77-9f19a6874d05")))
                .andExpect(jsonPath("$.children[0].leaf", is(false)))
                .andExpect(jsonPath("$.children[0].selectable", is(true)))
                .andExpect(jsonPath("$.children[0].timeRestrictionAllowed", is(true)))
                .andExpect(jsonPath("$.children[0].valueDefinitions", hasSize(0)))
                .andExpect(jsonPath("$.children[0].attributeDefinitions", hasSize(0)))
                .andExpect(jsonPath("$.children[0].display", is("Zusatzinformationen zu Operationen")))
                .andExpect(jsonPath("$.children[0].termCodes", hasSize(1)))
                .andExpect(jsonPath("$.children[0].termCodes[0].code", is("5-93...5-99")))
                .andExpect(jsonPath("$.children[0].termCodes[0].system", is("http://fhir.de/CodeSystem/dimdi/ops")))
                .andExpect(jsonPath("$.children[0].termCodes[0].version", is("2021")))
                .andExpect(jsonPath("$.children[0].termCodes[0].display", is("Zusatzinformationen zu Operationen")))
                .andExpect(jsonPath("$.children[0].children", hasSize(0)));
    }

    @Test
    public void testEntryById_OnlyDirectChildrenOfFoundElementAreReturned() throws Exception {
        // this UUID is part of the Prozedur.json file within the test resources directory
        var procedure = "6c0976dd-9e0f-4855-84be-36bacc1f62e6";
        mockMvc.perform(get("/api/v1/terminology/entries/{nodeId}", procedure))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.id", is("6c0976dd-9e0f-4855-84be-36bacc1f62e6")))
                .andExpect(jsonPath("$.children", hasSize(1)))
                .andExpect(jsonPath("$.children[0].id", is("17b15d41-33c2-e606-531c-1354fb95efe9")))
                // we know for sure that this element actually has at least one child defined, but it must not be returned!
                .andExpect(jsonPath("$.children[0].children", hasSize(0)));
    }

    @Test
    public void testSelectableEntries_FailsIfQueryIsMissingWith400() throws Exception {
        mockMvc.perform(get("/api/v1/terminology/selectable-entries"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSelectableEntries_ReturnsEmptyListIfCategoryIdIsUnknown() throws Exception {
        var testSearchQuery = "###ZZZ";
        var unknownTestSearchCategoryId = "a275e457-fa67-4d55-bed5-ebe592156222";

        mockMvc.perform(get("/api/v1/terminology/selectable-entries?query={query}&categoryId={categoryId}",
                testSearchQuery, unknownTestSearchCategoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testSelectableEntries_ReturnsEmptyListIfNothingIsFoundInAllCategories() throws Exception {
        var testSearchQuery = "###ZZZ";
        mockMvc.perform(get("/api/v1/terminology/selectable-entries?query={query}", testSearchQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testSelectableEntries_ReturnsEmptyListIfNothingIsFoundInSpecificCategory() throws Exception {
        var testSearchQuery = "###ZZZ";
        // this UUID is part of the Prozedur.json file within the test resources directory
        var testSearchCategory = "6c0976dd-9e0f-4855-84be-36bacc1f62e6";

        mockMvc.perform(get("/api/v1/terminology/selectable-entries?query={query}&categoryId={categoryId}",
                        testSearchQuery, testSearchCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testSelectableEntries_ReturnsEmptyListIfQueryIsPresentInAnotherCategory() throws Exception {
        // there is a terminology entry present for this query, but only within the Prozedur category
        var testSearchQuery = "OPERATIONEN";
        // this UUID is part of the Biobank.json file within the test resources directory
        var testSearchCategory = "54f3a5c7-5ac1-4301-b763-292ee7888278";

        mockMvc.perform(get("/api/v1/terminology/selectable-entries?query={query}&categoryId={categoryId}",
                        testSearchQuery, testSearchCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testSelectableEntries_ReturnsPopulatedListIfSomethingIsFoundInAnyCategory() throws Exception {
        var testSearchQuery = "OPERATIONEN";

        mockMvc.perform(get("/api/v1/terminology/selectable-entries?query={query}", testSearchQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$..display", hasItems("OPERATIONEN",
                        "Zusatzinformationen zu Operationen")));
    }

    @Test
    public void testSelectableEntries_ReturnsPopulatedListIfSomethingIsFoundInSpecificCategory() throws Exception {
        var testSearchQuery = "OPERATIONEN";
        // this UUID is part of the Prozedur.json file within the test resources directory
        var testSearchCategory = "6c0976dd-9e0f-4855-84be-36bacc1f62e6";

        mockMvc.perform(get("/api/v1/terminology/selectable-entries?query={query}&categoryId={categoryId}",
                        testSearchQuery, testSearchCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$..display", hasItems("OPERATIONEN",
                        "Zusatzinformationen zu Operationen")));
    }

    @Test
    public void testSelectableEntries_ReturnsPopulatedListOfOnlySelectableEntries() throws Exception {
        var testSearchQuery = "SELECTION";
        // this UUID is part of the Biobank.json file within the test resources directory
        var testSearchCategory = "54f3a5c7-5ac1-4301-b763-292ee7888278";

        mockMvc.perform(get("/api/v1/terminology/selectable-entries?query={query}&categoryId={categoryId}",
                        testSearchQuery, testSearchCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].display", is("SELECTION Selectable")))
                .andExpect(jsonPath("$[0].selectable", is(true)))
                .andExpect(jsonPath("$[0].id", is("849e42a1-dea4-4918-bc31-ae1550103548")));
    }
}
