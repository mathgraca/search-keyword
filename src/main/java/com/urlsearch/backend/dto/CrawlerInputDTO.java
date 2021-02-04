package com.urlsearch.backend.dto;

import java.util.Objects;

/**
 * @author mgraca
 * @version : $<br/>
 * : $
 * @since 27/01/2021 19:13
 */
public class CrawlerInputDTO {

    private String keyword;

    public String getKeyword () {
        return keyword;
    }

    public void setKeyword (final String keyword) {
        this.keyword = keyword;
    }

    @Override
    public boolean equals (final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final CrawlerInputDTO that = (CrawlerInputDTO) o;
        return Objects.equals(keyword, that.keyword);
    }

    @Override
    public int hashCode () {
        return Objects.hash(keyword);
    }
}
