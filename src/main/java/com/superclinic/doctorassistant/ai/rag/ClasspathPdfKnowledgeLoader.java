package com.superclinic.doctorassistant.ai.rag;

import com.superclinic.doctorassistant.ai.config.RagProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClasspathPdfKnowledgeLoader {

    private final RagProperties ragProperties;
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    public List<IngestibleDocument> loadAll() {
        if (!ragProperties.getClasspathDocs().isEnabled()) {
            return List.of();
        }

        try {
            Resource[] resources = resourcePatternResolver.getResources(ragProperties.getClasspathDocs().getLocation());
            List<IngestibleDocument> documents = new ArrayList<>();
            for (Resource resource : resources) {
                if (!resource.exists() || !resource.isReadable()) {
                    continue;
                }
                toIngestibleDocument(resource).ifPresent(documents::add);
            }
            log.info("Loaded {} classpath PDF knowledge document(s) from {}", documents.size(),
                    ragProperties.getClasspathDocs().getLocation());
            return documents;
        } catch (IOException ex) {
            log.warn("Failed to scan classpath PDF location: {}",
                    ragProperties.getClasspathDocs().getLocation(), ex);
            return List.of();
        }
    }

    public List<IngestibleDocument> loadBySourceType(KnowledgeSourceType sourceType) {
        return loadAll().stream()
                .filter(document -> document.sourceType() == sourceType)
                .toList();
    }

    private java.util.Optional<IngestibleDocument> toIngestibleDocument(Resource resource) {
        String filename = resource.getFilename();
        if (!StringUtils.hasText(filename)) {
            return java.util.Optional.empty();
        }

        KnowledgeSourceType sourceType = resolveSourceType(filename);
        if (sourceType == null) {
            log.warn("Skipping PDF with unmapped filename: {}", filename);
            return java.util.Optional.empty();
        }

        String content = extractText(resource, filename);
        if (!StringUtils.hasText(content)) {
            log.warn("Skipping PDF with no extractable text: {}", filename);
            return java.util.Optional.empty();
        }

        UUID sourceId = UUID.nameUUIDFromBytes(("classpath-pdf:" + filename).getBytes());
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(RagMetadataKeys.CATEGORY, defaultCategory(sourceType));
        metadata.put(RagMetadataKeys.SOURCE_FILE, filename);

        return java.util.Optional.of(new IngestibleDocument(
                sourceType,
                sourceId,
                toTitle(filename),
                content.trim(),
                true,
                metadata));
    }

    private String extractText(Resource resource, String filename) {
        try {
            PagePdfDocumentReader reader = new PagePdfDocumentReader(
                    resource,
                    PdfDocumentReaderConfig.builder()
                            .withPagesPerDocument(1)
                            .build());
            List<Document> pages = reader.get();
            StringBuilder text = new StringBuilder();
            for (Document page : pages) {
                if (StringUtils.hasText(page.getText())) {
                    if (!text.isEmpty()) {
                        text.append("\n\n");
                    }
                    text.append(page.getText().trim());
                }
            }
            return normalizeExtractedText(text.toString());
        } catch (Exception ex) {
            log.warn("Failed to read PDF '{}': {}", filename, ex.getMessage());
            return "";
        }
    }

    private static String normalizeExtractedText(String text) {
        return text.lines()
                .map(line -> line.replaceAll("\\s+", " ").trim())
                .filter(line -> !line.isEmpty())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("")
                .trim();
    }

    static KnowledgeSourceType resolveSourceType(String filename) {
        String normalized = filename.toLowerCase(Locale.ROOT);
        if (normalized.contains("faq")) {
            return KnowledgeSourceType.FAQ;
        }
        if (normalized.contains("insurance")) {
            return KnowledgeSourceType.INSURANCE_POLICY;
        }
        if (normalized.contains("appointment")) {
            return KnowledgeSourceType.APPOINTMENT_POLICY;
        }
        if (normalized.contains("doctor")) {
            return KnowledgeSourceType.DOCTOR_PROFILE;
        }
        return null;
    }

    static String toTitle(String filename) {
        String baseName = filename.replaceAll("(?i)\\.pdf$", "");
        return baseName.replace('_', ' ');
    }

    private static String defaultCategory(KnowledgeSourceType sourceType) {
        return switch (sourceType) {
            case DOCTOR_PROFILE -> "doctor";
            case FAQ -> "faq";
            case INSURANCE_POLICY -> "insurance";
            case APPOINTMENT_POLICY -> "appointments";
        };
    }
}
