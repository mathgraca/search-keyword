package com.urlsearch.backend.repository;

import java.util.HashMap;
import java.util.Map;

import com.urlsearch.backend.dto.CrawlerSearchedDTO;
import com.urlsearch.backend.dto.CrawlerStatusDTO;

public class CrawlerRepository {

    private final static Map<String, CrawlerSearchedDTO> mpResults = new HashMap<String, CrawlerSearchedDTO>();

    public void newSearch (String id) {
        final var search = new CrawlerSearchedDTO(id, CrawlerStatusDTO.active);
        mpResults.put(id, search);
    }

    public void finishSearch (String id) {
        final var search = mpResults.get(id);
        search.setStatus(CrawlerStatusDTO.done);
        mpResults.put(id, search);
    }

    public CrawlerSearchedDTO findSearch (String id) {
        return mpResults.get(id);
    }

    public void addURLSearch (String id, String url) {
        final var search = mpResults.get(id);
        search.getUrls().add(url);
        mpResults.put(id, search);
    }

}
