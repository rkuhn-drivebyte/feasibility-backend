package de.numcodex.feasibility_gui_backend.terminology;


import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import de.numcodex.feasibility_gui_backend.terminology.search.TerminologySearchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Entrypoint for REST endpoints regarding terminology definitions.
 */
@RequestMapping("api/v1/terminology")
@RestController
@CrossOrigin
public class TerminologyRestController {

    @Autowired
    private TerminologyService terminologyService;

    @GetMapping("entries/{nodeId}")
    public TerminologyEntry getEntry(@PathVariable UUID nodeId) {
        return terminologyService.getTerminologyEntryById(nodeId);
    }

    @GetMapping("root-entries")
    public List<CategoryEntry> getCategories() {
        return terminologyService.getTerminologyCategories();
    }

    @GetMapping("selectable-entries")
    public List<TerminologyEntry> getSelectableEntries(@RequestParam("query") String query,
                                                       @RequestParam(value = "categoryId", required = false) UUID categoryId) {
        try {
            if (categoryId == null) {
                return terminologyService.searchSelectableEntries(query.toLowerCase());
            } else {
                return terminologyService.searchSelectableEntries(query.toLowerCase(), categoryId);
            }
        } catch (TerminologySearchException e) {
            // TODO: handle this!!!
            throw new RuntimeException();
        }
    }
}
