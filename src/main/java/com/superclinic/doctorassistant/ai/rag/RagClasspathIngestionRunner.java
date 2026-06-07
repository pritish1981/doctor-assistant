package com.superclinic.doctorassistant.ai.rag;

import com.superclinic.doctorassistant.ai.config.RagProperties;
import com.superclinic.doctorassistant.persistence.repository.RagSourceCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "doctor-assistant.rag.classpath-docs",
        name = "auto-ingest-on-startup",
        havingValue = "true")
public class RagClasspathIngestionRunner implements ApplicationRunner {

    private final RagProperties ragProperties;
    private final RagIngestionService ragIngestionService;
    private final RagSourceCatalogRepository ragSourceCatalogRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (!ragProperties.getClasspathDocs().isEnabled()) {
            return;
        }

        if (ragSourceCatalogRepository.count() > 0) {
            log.debug("RAG source catalog already populated ({} entries), skipping classpath PDF ingest",
                    ragSourceCatalogRepository.count());
            return;
        }

        log.info("RAG catalog empty — ingesting classpath PDF knowledge documents from {}",
                ragProperties.getClasspathDocs().getLocation());
        ragIngestionService.ingestAll();
    }
}
