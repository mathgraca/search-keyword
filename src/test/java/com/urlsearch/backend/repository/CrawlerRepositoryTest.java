package com.urlsearch.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.urlsearch.backend.dto.CrawlerStatusDTO;
import org.junit.jupiter.api.Test;

public class CrawlerRepositoryTest {

    private CrawlerRepository repository = new CrawlerRepository();

    @Test
    public void findSearch () {
        var id = "1";
        repository.newSearch(id);
        var search = repository.findSearch(id);
        assertEquals(search.getId(), id);
        assertEquals(search.getStatus(), CrawlerStatusDTO.active);
    }

    @Test
    public void fisnishSearch () {
        var id = "1";
        repository.newSearch(id);
        repository.finishSearch(id);
        var search = repository.findSearch(id);
        assertEquals(search.getId(), id);
        assertEquals(search.getStatus(), CrawlerStatusDTO.done);
    }

    @Test
    public void addUrl () {
        var url = "http://teste";
        var id = "1";
        repository.newSearch(id);
        repository.addURLSearch(id, url);
        var search = repository.findSearch(id);
        assertEquals(search.getId(), id);
        assertTrue(search.getUrls().contains(url));
    }

}
