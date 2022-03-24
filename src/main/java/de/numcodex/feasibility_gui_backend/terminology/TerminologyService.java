package de.numcodex.feasibility_gui_backend.terminology;

import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndex;
import de.numcodex.feasibility_gui_backend.terminology.search.TerminologySearchException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class TerminologyService {

    // TODO: these have to come in already sorted
    @NonNull
    private final List<CategoryEntry> categories;

    @NonNull
    private final TerminologyIndex entries;

    public TerminologyEntry getTerminologyEntryById(UUID entryId) {
        return entries.searchById(entryId)
                .map(TerminologyEntry::copyWithDirectChildren)
                .orElse(null);
    }

    public List<CategoryEntry> getTerminologyCategories() {
        return categories;
    }

    // TODO: maybe translate this into an exception that is more generic
    public List<TerminologyEntry> searchSelectableEntries(String search) throws TerminologySearchException {
        return entries.searchSelectableEntries(search)
                .stream().map(TerminologyEntry::copyWithDirectChildren)
                .collect(Collectors.toList());
    }

    // TODO: maybe translate this into an exception that is more generic
    public List<TerminologyEntry> searchSelectableEntries(String search, UUID categoryId) throws TerminologySearchException {
        return entries.searchSelectableEntries(search, categoryId)
                .stream().map(TerminologyEntry::copyWithDirectChildren)
                .collect(Collectors.toList());
    }
}