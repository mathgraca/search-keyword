package com.urlsearch.backend.dto;

public enum CrawlerStatusDTO {
    active("active"),
    done("done");

    private String status;

    CrawlerStatusDTO (String status) {
        this.status = status;
    }

    public String getStatus () {
        return status;
    }
}
