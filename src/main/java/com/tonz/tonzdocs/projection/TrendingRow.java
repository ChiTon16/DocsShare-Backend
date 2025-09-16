package com.tonz.tonzdocs.dto.projection;

import java.util.Date;

public interface TrendingRow {
    Integer getDocumentId();
    String  getTitle();
    String  getFilePath();
    Date    getUploadTime();
    Integer getUserId();
    String  getUserName();
    Integer getSubjectId();
    String  getSubjectName();
    Double  getScore();
}
