// repo/projection/RecentSubjectRow.java
package com.tonz.tonzdocs.repository;

import java.time.LocalDateTime;

public interface RecentSubjectRow {
    Integer getSubjectId();
    String getSubjectName();
    String getSubjectCode();
    Long getTotalDocsInSubject();
    Long getDocsViewedByUser();
    LocalDateTime getLastViewedAt();
}
