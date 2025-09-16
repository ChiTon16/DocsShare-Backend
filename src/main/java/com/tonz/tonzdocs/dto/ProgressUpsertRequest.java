// com/tonz/tonzdocs/dto/ProgressUpsertRequest.java
package com.tonz.tonzdocs.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProgressUpsertRequest(
        @JsonAlias({"document_id", "documentId"})
        Integer documentId,

        @JsonAlias({"last_page", "lastPage"})
        Integer lastPage,

        @JsonAlias({"percent"})
        Double percent,

        @JsonAlias({"session_read_seconds", "sessionReadSeconds"})
        Long sessionReadSeconds
) {}
