package com.alpha_code.alpha_code_user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Data
public class PagedResult<T> implements Serializable {
    private List<T> data;
    @JsonProperty("total_count")
    private long totalCount;
    private int page;
    @JsonProperty("per_page")
    private int perPage;
    @JsonProperty("total_pages")
    private int totalPages;
    @JsonProperty("has_next")
    private boolean hasNext;
    @JsonProperty("has_previous")
    private boolean hasPrevious;

    public PagedResult(Page<T> pageData) {
        this.data = pageData.getContent();
        this.totalCount = pageData.getTotalElements();
        this.page = pageData.getNumber() + 1;
        this.perPage = pageData.getSize();
        this.totalPages = pageData.getTotalPages();
        this.hasNext = pageData.hasNext();
        this.hasPrevious = pageData.hasPrevious();
    }
}
