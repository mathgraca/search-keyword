package com.urlsearch.backend.resource;

import java.net.URL;
import java.net.http.HttpClient;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.urlsearch.backend.dto.CrawlerInputDTO;
import com.urlsearch.backend.dto.CrawlerOutputDTO;
import com.urlsearch.backend.dto.CrawlerSearchedDTO;
import com.urlsearch.backend.exception.NotFoundException;
import com.urlsearch.backend.repository.CrawlerRepository;
import com.urlsearch.backend.service.CrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlerResource {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerResource.class);

    private static final ExecutorService pool = Executors.newWorkStealingPool();

    private CrawlerService service;

    public CrawlerResource () {
        service = new CrawlerService(new CrawlerRepository(), HttpClient.newHttpClient());
    }

    public CrawlerOutputDTO startCrawling (final CrawlerInputDTO dto, URL url, int maxResults) {

        final String idCrwaling = service.insertNewCrawling();

        searchUrlAsync(url, maxResults, dto.getKeyword(), idCrwaling);

        final CrawlerOutputDTO outputDto = new CrawlerOutputDTO();
        outputDto.setId(idCrwaling);

        return outputDto;
    }

    private void searchUrlAsync (final URL url, final int maxResults, final String keyword,
            final String idCrwaling) {
        pool.submit(callAsyncSearch(url, maxResults, keyword, idCrwaling));
    }

    private Callable<Void> callAsyncSearch (final URL url, final int maxResults, final String keyword,
            final String idCrwaling) {
        return new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                service.startCrawling(idCrwaling, url, keyword, maxResults);
                service.finishCrawling(idCrwaling);
                return null;

            }
        };
    }

    public CrawlerSearchedDTO searchCrawling (String id) {
        final CrawlerSearchedDTO search = service.findCrawling(id);
        if (search == null) {
            throw new NotFoundException();
        }
        return search;
    }

}
