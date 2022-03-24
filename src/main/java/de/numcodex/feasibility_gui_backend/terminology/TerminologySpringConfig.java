package de.numcodex.feasibility_gui_backend.terminology;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import de.numcodex.feasibility_gui_backend.terminology.search.TerminologyIndices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

@Slf4j
@Configuration
public class TerminologySpringConfig {

    @Value("${app.ontologyFolder}")
    private String profileDirectoryPath;

    @Bean
    TerminologyService createTerminology(@Qualifier("terminology") ObjectMapper jsonUtil,
                                         TerminologyIndices indexFactory,
                                         File[] terminologyFiles) throws IOException {

        var terminologyTreesByCategory = readTerminologyFiles(jsonUtil, terminologyFiles);
        var categories = new ArrayList<CategoryEntry>();
        var entries = new HashMap<CategoryEntry, List<TerminologyEntry>>();

        for (TerminologyEntry terminologyTree : terminologyTreesByCategory) {
            var category = extractCategory(terminologyTree);
            categories.add(category);
            entries.put(category, flattenTerminology(terminologyTree));
        }

        return new TerminologyService(categories, indexFactory.createInMemory(entries));
    }

    private List<TerminologyEntry> readTerminologyFiles(ObjectMapper jsonUtil, File[] terminologyFiles)
            throws IOException {
        var terminologyTrees = new ArrayList<TerminologyEntry>();

        for (File terminologyFile : terminologyFiles) {
            log.info("reading terminology file at '%s'".formatted(terminologyFile.getAbsolutePath()));
            terminologyTrees.add(jsonUtil.readValue(terminologyFile, TerminologyEntry.class));
        }

        return terminologyTrees;
    }

    private CategoryEntry extractCategory(TerminologyEntry terminologyTree) {
        return new CategoryEntry(terminologyTree.getId(), terminologyTree.getDisplay());
    }

    private List<TerminologyEntry> flattenTerminology(TerminologyEntry rootTerminologyEntry) {
        var entries = new ArrayList<TerminologyEntry>();

        var traversalStack = new Stack<TerminologyEntry>();
        traversalStack.push(rootTerminologyEntry);

        TerminologyEntry currentEntry;
        while (!traversalStack.empty()) {
            currentEntry = traversalStack.pop();
            entries.add(currentEntry);

            var children = currentEntry.getChildren();
            traversalStack.addAll(children);
        }

        return entries;
    }

    @Bean
    @Qualifier("terminology")
    File[] getTerminologyFiles(FileFilter isFileFilter) {
        log.info("reading terminology files from '%s'".formatted(profileDirectoryPath));
        var profileDirectory = new File(profileDirectoryPath);
        var profileFiles = profileDirectory.listFiles(isFileFilter);
        if (profileFiles == null) {
            throw new IllegalArgumentException("Could not list files in %s. Is this a directory?"
                    .formatted(profileDirectoryPath));
        }
        return profileFiles;
    }

    @Bean
    @Qualifier("terminology")
    ObjectMapper terminologyJsonUtil() {
        return new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Bean
    FileFilter isFileFilter() {
        return FileFilterUtils.fileFileFilter();
    }
}
