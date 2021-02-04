package com.urlsearch.backend.dto;

import java.util.HashSet;
import java.util.Set;

public class CrawlerSearchedDTO {

    private String id;

    private CrawlerStatusDTO status;

    private Set<String> urls;

    public CrawlerSearchedDTO () {

    }

    public CrawlerSearchedDTO (final String id, final CrawlerStatusDTO status) {
        this.id = id;
        this.status = status;
        this.urls = new HashSet<>();
    }

    public String getId () {
        return id;
    }

    public void setId (final String id) {
        this.id = id;
    }

    public CrawlerStatusDTO getStatus () {
        return status;
    }

    public void setStatus (final CrawlerStatusDTO status) {
        this.status = status;
    }

    public Set<String> getUrls () {
        return urls;
    }

    public void setUrls (final Set<String> urls) {
        this.urls = urls;
    }
}
