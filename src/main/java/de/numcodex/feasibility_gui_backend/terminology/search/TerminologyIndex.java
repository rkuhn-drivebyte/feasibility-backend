package de.numcodex.feasibility_gui_backend.terminology.search;

import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Describes an index of one or more tree based {@link TerminologyEntry} instances.
 */
public interface TerminologyIndex {
    /**
     * Tries to find a {@link TerminologyEntry} based on its identifier.
     *
     * @param entryId Identifier of the entry.
     * @return The {@link TerminologyEntry} associated with the given identifier.
     */
    Optional<TerminologyEntry> searchById(UUID entryId);

    /**
     * Tries to search for selectable {@link TerminologyEntry}s with the given search term. This search is NOT category
     * restricted, i.e. {@link TerminologyEntry}s of ALL categories are taken into account.
     * <p>
     * When searching for the search term, only takes `display` and `termCode` attributes of a {@link TerminologyEntry}
     * into account.
     *
     * @param searchTerm The term that is searched for.
     * @return A list of selectable {@link TerminologyEntry}s that match the given search term.
     * @throws TerminologySearchException If an error occurs while searching.
     */
    List<TerminologyEntry> searchSelectableEntries(String searchTerm) throws TerminologySearchException;

    /**
     * Tries to search for selectable {@link TerminologyEntry}s with the given search term. This search is restricted to
     * the category identified by the given category identifier.
     * <p>
     * When searching for the search term, only takes `display` and `termCode` attributes of a {@link TerminologyEntry}
     * into account.
     *
     * @param searchTerm          The term that is searched for.
     * @param terminologyCategory Identifier of the category that is used for this search.
     * @return A list of selectable {@link TerminologyEntry}s that match the given search term.
     * @throws TerminologySearchException If an error occurs while searching.
     */
    List<TerminologyEntry> searchSelectableEntries(String searchTerm, UUID terminologyCategory)
            throws TerminologySearchException;
}
