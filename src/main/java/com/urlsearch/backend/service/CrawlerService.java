package com.urlsearch.backend.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.urlsearch.backend.dto.CrawlerSearchedDTO;
import com.urlsearch.backend.repository.CrawlerRepository;
import com.urlsearch.backend.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

    private CrawlerRepository repository;

    private HttpClient httpClient;

    public CrawlerService (CrawlerRepository repository, HttpClient httpClient) {
        this.repository = repository;
        this.httpClient = httpClient;
    }

    public String insertNewCrawling () {
        String id = null;

        while (id == null) {
            final String idCrawling = Utils.generateRamdonlyId();
            final var search = repository.findSearch(id);
            if (search == null) {
                id = idCrawling;
            }
        }

        repository.newSearch(id);
        logger.info("new inserted id=" + id);
        return id;
    }

    public Set<String> startCrawling (String idCrawling, URL url, String keyword, int maxResults)
            throws URISyntaxException, IOException, InterruptedException, ParserConfigurationException, SAXException {

        final var urlSearched = new HashSet<String>();
        final var urlKeywordFounded = new HashSet<String>();

        final String baseUrl = putPathBaseUrl(url);

        logger.info("M=startCrawling id=" + idCrawling + " BaseUrl=" + baseUrl);
        final Set<String> st = searchKeywordUrl(idCrawling, url, keyword, urlSearched, urlKeywordFounded, baseUrl,
                maxResults);
        searchAllUrl(idCrawling, st, keyword, urlSearched, urlKeywordFounded, baseUrl, maxResults);

        return urlKeywordFounded;
    }

    public CrawlerSearchedDTO findCrawling (String id) {
        return repository.findSearch(id);
    }

    public void finishCrawling (String id) {
        logger.info("M=finishCrawling Finish crawling id=" + id);
        repository.finishSearch(id);
    }

    private void searchAllUrl (final String idCrawling, Set<String> urlSearch, String keyword,
            Set<String> urlSearched, HashSet<String> urlKeywordFounded, String baseUrl, int numMaxRegister) {
        for (String url : urlSearch) {
            if (continueSearch(urlKeywordFounded, numMaxRegister)) {
                break;
            }
            if (urlSearched.contains(url)) {
                continue;
            }

            final URL nextURL = getUrl(url);
            if (nextURL != null) {
                try {
                    final Set<String> nextUrl = searchKeywordUrl(idCrawling, nextURL, keyword, urlSearched,
                            urlKeywordFounded,
                            baseUrl, numMaxRegister);

                    searchAllUrl(idCrawling, nextUrl, keyword, urlSearched, urlKeywordFounded, baseUrl, numMaxRegister);
                } catch (Exception e) {
                    logger.error("problema ao buscar URL", e);
                }
            }
        }
    }

    private String putPathBaseUrl (final URL url) {
        var baseUrl = url.toString().replace("http://", "").replace("https://", "");
        if (!lastCharIsSlash(baseUrl)) {
            baseUrl += "/";
        }
        return baseUrl;
    }

    private boolean lastCharIsSlash (final String baseUrl) {
        return baseUrl.lastIndexOf("/") == baseUrl.length() - 1;
    }

    private boolean continueSearch (final HashSet<String> urlKeywordFounded, final int numMaxRegister) {
        return numMaxRegister != -1 && urlKeywordFounded.size() >= numMaxRegister;
    }

    private URL getUrl (String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            logger.info("M=getUrl  Url Invalida: url=" + url);
            return null;
        }
    }

    private Set<String> searchKeywordUrl (final String idCrawling, final URL url,
            final String keyword, final Set<String> urlSearched,
            final HashSet<String> urlKeywordFounded, String baseUrl, int maxNumRegister)
            throws URISyntaxException, IOException, InterruptedException, ParserConfigurationException, SAXException {
        final var request = HttpRequest.newBuilder(url.toURI()).build();

        final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        final var urlStr = url.toString();

        if (isResponseOkAndBodyHtml(response)) {
            var body = response.body();

            if (isUrlNotFoundedAndHaveKeyword(keyword, urlKeywordFounded, urlStr, body)) {
                insertNewUrlSearch(idCrawling, urlKeywordFounded, urlStr);
                if (continueSearch(urlKeywordFounded, maxNumRegister)) {
                    return new HashSet<String>();
                }
            }

            urlSearched.add(urlStr);

            final var urlToSearch = getUrlsHtml(url, body, baseUrl);
            urlToSearch.removeAll(urlSearched);

            return urlToSearch;

        } else {
            urlSearched.add(urlStr);
            logger.error("M=searchKeywordUrl Problem to search URL=" + url.toString() + " statusCode=" + response
                    .statusCode());
            return new HashSet<String>();
        }
    }

    private boolean isResponseOkAndBodyHtml (final HttpResponse<String> response) {
        return response.statusCode() == 200 && isContentTypeHtml(response);
    }

    private boolean isUrlNotFoundedAndHaveKeyword (final String keyword, final HashSet<String> urlKeywordFounded,
            final String urlStr, final String body) {
        return !urlKeywordFounded.contains(urlStr) && body.toLowerCase().contains(keyword.toLowerCase());
    }

    private void insertNewUrlSearch (final String idCrawling, final HashSet<String> urlKeywordFounded,
            final String urlStr) {
        urlKeywordFounded.add(urlStr);
        logger.info("M=searchKeywordUrl Add new Url idCrawling=" + idCrawling + " url=" + urlStr);
        repository.addURLSearch(idCrawling, urlStr);
    }

    private boolean isContentTypeHtml (HttpResponse response) {
        return response.headers().firstValue("Content-Type").orElse("text/html").contains(("text/html"));
    }

    private Set<String> getUrlsHtml (URL urlHost, String bodyHtml, String baseUrl)
            throws IOException, SAXException, ParserConfigurationException {

        final var setUrl = new HashSet<String>();

        var dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        var db = dbf.newDocumentBuilder();

        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(bodyHtml));

        Document doc = db.parse(is);

        insertUrlSet(setUrl, doc, "a", "href", baseUrl, urlHost);
        insertUrlSet(setUrl, doc, "iframe", "src", baseUrl, urlHost);
        insertUrlSet(setUrl, doc, "link", "href", baseUrl, urlHost);

        return setUrl;
    }

    private void insertUrlSet (final HashSet<String> setUrl, final Document doc,
            final String elementByTag, final String atribute,
            String baseURl, final URL urlHost) {
        var nodeLista = doc.getElementsByTagName(elementByTag);

        for (int i = 0; i < nodeLista.getLength(); i++) {
            Node node = nodeLista.item(i);
            if (node.hasAttributes()) {
                Attr attr = (Attr) node.getAttributes().getNamedItem(atribute);
                if (attr != null) {
                    String newURL = attr.getValue();
                    if (!ifFtp(newURL) && !isEmail(newURL)) {
                        if (isUrlIncomplete(newURL)) {
                            newURL = concatStringUrl(urlHost, newURL);
                        }
                        if (isUrlTextHtml(newURL) && newURL.contains(baseURl)) {
                            logger.info("M=insertUrlSet add to ser newURL=" + newURL);
                            setUrl.add(newURL);
                        }
                    }
                }
            }
        }
    }

    private String concatStringUrl (final URL urlHost, String newURL) {
        String protocol = urlHost.getProtocol();
        String host = urlHost.getHost();

        newURL = protocol + "://" + host + "/" + newURL.replace("../", "");
        return newURL;
    }

    private boolean isUrlIncomplete (final String newURL) {
        return !newURL.contains("https:") && !newURL.contains("http:");
    }

    private boolean isUrlTextHtml (final String url) {
        var lenghtUrl = url.length();
        if (lenghtUrl < 3) {
            return true;
        }
        return !url.substring(lenghtUrl - 3, lenghtUrl).equals("gif")
                && !url.substring(lenghtUrl - 3, lenghtUrl).equals("jpg")
                && !url.substring(lenghtUrl - 3, lenghtUrl).equals("png")
                && !url.substring(lenghtUrl - 3, lenghtUrl).equals("css");
    }

    private boolean isEmail (final String url) {
        return url.length() > 7 && url.substring(0, 7).equals("mailto:");
    }

    private boolean ifFtp (final String url) {
        return url.length() > 7 && url.substring(0, 4).equals("ftp:");
    }

}
