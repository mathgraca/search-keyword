package com.urlsearch.backend;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.urlsearch.backend.dto.CrawlerInputDTO;
import com.urlsearch.backend.exception.MandatoryFieldNotPresentException;
import com.urlsearch.backend.exception.NotFoundException;
import com.urlsearch.backend.exception.UnsuportedMediaException;
import com.urlsearch.backend.resource.CrawlerResource;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.utils.StringUtils;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main (String[] args) throws MalformedURLException {

        //BASE_URL
        var baseUrl = System.getenv("BASE_URL");
        logger.info("baseURL=" + baseUrl);

        var maxResults = System.getenv("MAX_RESULTS");
        logger.info("maxResults=" + maxResults);

        if (baseUrl == null) {
            logger.error("Environment BASE_URL not reported");
            throw new IllegalArgumentException("Environment BASE_URL not reported");
        }

        final var url = getUrl(baseUrl);

        final var maxResultInt = getMaxResultInt(maxResults);

        get("/crawl/:id", (req, res) -> {
            var search = new CrawlerResource().searchCrawling(req.params("id"));
            res.type("application/json");
            return new Gson().toJson(search);
        });
        post("/crawl", (req, res) -> {
            var input = validadeInput(req);
            var outputDto = new CrawlerResource().startCrawling(input, url, maxResultInt);
            res.type("application/json");
            return new Gson().toJson(outputDto);
        });
        exception(UnsuportedMediaException.class, (e, request, response) -> {
            logger.error("UnsuportedMediaException", e);
            response.status(415);
            response.body("Unsupported Content-Type");
        });
        exception(JsonSyntaxException.class, (e, request, response) -> {
            logger.error("JsonSyntaxException", e);
            response.status(400);
            response.body("Bad Request");
        });
        exception(MandatoryFieldNotPresentException.class, (e, request, response) -> {
            logger.error("MandatoryFieldNotPresentException", e);
            String s = e.getFieldNotPresent().stream().map(Object::toString).collect(Collectors.joining(","));
            response.status(400);
            response.body("Mandatory Field not present " + s);
        });
        exception(NotFoundException.class, (e, request, response) -> {
            logger.error("NotFoundException", e);
            response.status(404);
            response.body("Not Found");
        });
        exception(IllegalArgumentException.class, (e, request, response) -> {
            logger.error("IllegalArgumentException", e);
            response.status(400);
            response.body(e.getMessage());
        });
    }

    private static CrawlerInputDTO validadeInput (final Request req) {
        if (!req.contentType().equals("application/json")) {
            throw new UnsuportedMediaException();
        }
        final CrawlerInputDTO dto = new Gson().fromJson(req.body(), CrawlerInputDTO.class);
        var keyword = dto.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            logger.error("field keyword not present");
            throw new MandatoryFieldNotPresentException(Arrays.asList("keyword"));
        }
        if (keyword.length() < 4 || keyword.length() > 32) {
            logger.error("field keyword must be between 4 and 32 characters");
            throw new IllegalArgumentException("field keyword must be between 4 and 32 characters");
        }
        return dto;
    }

    private static int getMaxResultInt (final String maxResults) {
        var maxResultInt = -1;
        if (maxResults != null) {
            try {
                maxResultInt = Integer.parseInt(maxResults);
            } catch (NumberFormatException e) {
                logger.error("Environment MAX_RESULTS is not a valid integer");
                throw new IllegalArgumentException("Environment MAX_RESULTS is not a valid integer");
            }
        }
        if (maxResultInt < -1 || maxResultInt == 0) {
            logger.error("Environment MAX_RESULTS must be -1 or greater than 0 ");
            throw new IllegalArgumentException("Environment MAX_RESULTS must be -1 or greater than 0 ");
        }
        return maxResultInt;
    }

    private static URL getUrl (String baseUrl) {
        URL url;
        try {
            url = new URL(baseUrl);
        } catch (Exception e) {
            logger.error("Environment BASE_URL is not a valid url");
            throw new IllegalArgumentException("Environment BASE_URL is not a valid url");
        }
        return url;
    }
}
