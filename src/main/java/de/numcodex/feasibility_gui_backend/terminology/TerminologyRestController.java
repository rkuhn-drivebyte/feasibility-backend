package de.numcodex.feasibility_gui_backend.terminology;


import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import de.numcodex.feasibility_gui_backend.terminology.search.TerminologySearchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
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
    public List<TerminologyEntry> getSelectableEntries(@RequestParam("query") String searchTerm,
                                                       @RequestParam(value = "categoryId") Optional<UUID> categoryId) {
        try {
            if (categoryId.isPresent()) {
                return terminologyService.searchSelectableEntries(searchTerm, categoryId.get());
            } else {
                return terminologyService.searchSelectableEntries(searchTerm);
            }
        } catch (TerminologySearchException e) {
            // TODO: handle this!!!
            throw new RuntimeException();
        }
    }
}
