package com.urlsearch.backend.dto;

import java.util.Objects;

/**
 * @author matheus.graca
 * @version : $<br/>
 * : $
 * @since 27/01/2021 19:13
 */
public class CrawlerOutputDTO {

    private String id;

    public String getId () {
        return id;
    }

    public void setId (final String id) {
        this.id = id;
    }

    @Override
    public boolean equals (final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final CrawlerOutputDTO that = (CrawlerOutputDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode () {
        return Objects.hash(id);
    }
}
