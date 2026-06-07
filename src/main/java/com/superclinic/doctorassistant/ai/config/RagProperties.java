package com.superclinic.doctorassistant.ai.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "doctor-assistant.rag")
public class RagProperties {

    @Min(1)
    @Max(50)
    private int topK = 5;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double similarityThreshold = 0.75;

    @NotNull
    private String defaultSourceFilter = "";

    @NotNull
    private Chunking chunking = new Chunking();

    @NotNull
    private ClasspathDocs classpathDocs = new ClasspathDocs();

    @Getter
    @Setter
    public static class ClasspathDocs {

        /** Load PDF knowledge documents from classpath (e.g. src/main/resources/docs). */
        private boolean enabled = true;

        /** Classpath location pattern for PDF files. */
        private String location = "classpath:docs/*.pdf";

        /** Ingest PDFs into the vector store on application startup when the catalog is empty. */
        private boolean autoIngestOnStartup = true;
    }

    @Getter
    @Setter
    public static class Chunking {

        /** FAQ entries are short Q&A pairs — keep each document as a single chunk. */
        @Min(100)
        @Max(10000)
        private int faqChunkSize = 8000;

        /** Insurance policies are long — split into ~500-token segments. */
        @Min(100)
        @Max(2000)
        private int insuranceChunkSize = 500;

        @Min(0)
        @Max(500)
        private int insuranceChunkOverlap = 50;

        /** Doctor profiles fit in one chunk; large limit avoids unnecessary splits. */
        @Min(100)
        @Max(10000)
        private int doctorProfileChunkSize = 8000;
    }
}
