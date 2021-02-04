package com.urlsearch.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiPredicate;
import javax.xml.parsers.ParserConfigurationException;

import com.urlsearch.backend.repository.CrawlerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

public class CrawlerServiceTest {

    private CrawlerService service;

    private HttpClient httpClient;

    private CrawlerRepository repository;

    @BeforeEach
    public void before () throws IOException, InterruptedException {
        repository = Mockito.mock(CrawlerRepository.class);
        httpClient = Mockito.mock(HttpClient.class);

        service = new CrawlerService(repository, httpClient);
    }

    @Test
    public void insertNewSearch () {
        String id = service.insertNewCrawling();
        assertEquals(id.length(), 8);

    }

    @Test
    public void searchWithoutLimit ()
            throws IOException, InterruptedException, ParserConfigurationException, SAXException, URISyntaxException {
        doReturn(getResponseMock()).when(httpClient).send(Mockito.any(), Mockito.any());

        var search = service.startCrawling("1", new URL("https://base.com/teste"), "teste", -1);

        assertEquals(search.size(), 3);
    }

    @Test
    public void searchWithLimit ()
            throws IOException, InterruptedException, ParserConfigurationException, SAXException, URISyntaxException {
        doReturn(getResponseMock()).when(httpClient).send(Mockito.any(), Mockito.any());

        var search = service.startCrawling("1", new URL("https://base.com/teste"), "teste", 2);

        assertEquals(search.size(), 2);
    }

    public HttpResponse getResponseMock () {
        HttpResponse response = Mockito.mock(HttpResponse.class);

        doReturn(getHeadresMock()).when(response).headers();
        doReturn(getMockBodyHtml()).when(response).body();
        Mockito.when(response.statusCode()).thenReturn(200);
        return response;
    }

    public HttpHeaders getHeadresMock () {
        var mpHeaders = new HashMap<String, List<String>>();
        var lstContentType = new ArrayList<String>();
        lstContentType.add("text/html");
        mpHeaders.put("Content-Type", lstContentType);

        var biPredicate = Mockito.mock(BiPredicate.class);
        Mockito.when(biPredicate.test(Mockito.any(), Mockito.any())).thenReturn(true);

        var header = HttpHeaders.of(mpHeaders, biPredicate);

        return header;
    }

    private String getMockBodyHtml () {
        return "<html>"
                + "<body>"
                + "<a href='https://base.com/teste/teste.com'>teste</a>"
                + "<a href='http://testeNo.com/certo'>teste No</a>"
                + "<a href='http://testeNop.com/certo'>teste No</a>"
                + "<a href='https://base.com/testeFail/teste2/teste.com'>teste fail</a>"
                + "<a href='https://base.com/teste/teste2/teste.com'>teste 2</a>"
                + "<src img='https://base.com/teste/teste2/teste.gif'/>"
                + "</body>"
                + "</html>";
    }
}
