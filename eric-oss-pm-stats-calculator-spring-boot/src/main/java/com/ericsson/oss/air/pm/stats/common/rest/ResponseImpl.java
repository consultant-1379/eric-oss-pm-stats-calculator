/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.rest;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;

/**
 * An implementation of {@link Response} to be used by {@link RestResponse}.
 *
 * @param <T>
 *            the type of {@link org.apache.http.HttpEntity} that should be retrieved, can be {@link String} or {@link InputStream}
 */

@Slf4j
public abstract class ResponseImpl<T> extends Response {

    private final Response responseObject;

    protected ResponseImpl(final T entity, final String uri, final int statusCode, final Header[] headers) {
        super();
        responseObject = buildResponseObject(entity, uri, statusCode, headers);
    }

    private Response buildResponseObject(final T entity, final String uri, final int statusCode, final Header[] headers) {
        final URI location = createUriOrEmpty(uri);
        final ResponseBuilder builder = Response.status(statusCode)
                .entity(entity)
                .contentLocation(location)
                .location(location);

        if (headers != null && headers.length > 0) {
            for (final Header thisHeader : headers) {
                builder.header(thisHeader.getName(), thisHeader.getValue()); //NOSONAR original builder is mutable, noneed to assign result
            }
        }
        return builder.build();
    }

    private static URI createUriOrEmpty(final String uriString) {
        if (uriString == null) {
            log.warn("Uri: null. Using Empty String");
        } else {
            try {
                return URI.create(uriString);
            } catch (final IllegalArgumentException e) {
                log.warn("Unable to create URL for String '{}' - {}", uriString, e);
            }
        }
        return URI.create("");
    }

    @Override
    public int getStatus() {
        return responseObject.getStatus();
    }

    @Override
    public StatusType getStatusInfo() {
        return responseObject.getStatusInfo();
    }

    @Override
    public Object getEntity() {
        return responseObject.getEntity();
    }

    @Override
    public <T> T readEntity(final Class<T> entityType) {
        return responseObject.readEntity(entityType);
    }

    @Override
    public <T> T readEntity(final GenericType<T> entityType) {
        return responseObject.readEntity(entityType);
    }

    @Override
    public <T> T readEntity(final Class<T> entityType, final Annotation[] annotations) {
        return responseObject.readEntity(entityType, annotations);
    }

    @Override
    public <T> T readEntity(final GenericType<T> entityType, final Annotation[] annotations) {
        return responseObject.readEntity(entityType, annotations);
    }

    @Override
    public boolean hasEntity() {
        return responseObject.hasEntity();
    }

    @Override
    public boolean bufferEntity() {
        return responseObject.bufferEntity();
    }

    @Override
    public void close() {
        responseObject.close();
    }

    @Override
    public MediaType getMediaType() {
        return responseObject.getMediaType();
    }

    @Override
    public Locale getLanguage() {
        return responseObject.getLanguage();
    }

    @Override
    public int getLength() {
        return responseObject.getLength();
    }

    @Override
    public Set<String> getAllowedMethods() {
        return responseObject.getAllowedMethods();
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        return responseObject.getCookies();
    }

    @Override
    public EntityTag getEntityTag() {
        return responseObject.getEntityTag();
    }

    @Override
    public Date getDate() {
        return responseObject.getDate();
    }

    @Override
    public Date getLastModified() {
        return responseObject.getLastModified();
    }

    @Override
    public URI getLocation() {
        try {
            return responseObject.getLocation();
        } catch (final AbstractMethodError e) { //NOSONAR Exception logged
            log.warn("Unable to parse location URI");
        }

        return null;
    }

    @Override
    public Set<Link> getLinks() {
        return responseObject.getLinks();
    }

    @Override
    public boolean hasLink(final String relation) {
        return responseObject.hasLink(relation);
    }

    @Override
    public Link getLink(final String relation) {
        return responseObject.getLink(relation);
    }

    @Override
    public Link.Builder getLinkBuilder(final String relation) {
        return responseObject.getLinkBuilder(relation);
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        return responseObject.getMetadata();
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
        return responseObject.getStringHeaders();
    }

    @Override
    public String getHeaderString(final String name) {
        return responseObject.getHeaderString(name);
    }
}
