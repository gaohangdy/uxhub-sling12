/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.peregrine.admin.servlets;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.peregrine.commons.servlets.AbstractBaseServlet;

import org.osgi.service.component.annotations.Component;
import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_GET_LANGUAGES;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import static com.peregrine.commons.util.PerUtil.*;

import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.TITLE;
import static com.peregrine.commons.util.PerConstants.JSON;

import org.apache.sling.api.resource.Resource;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Provides Admin's Languages in a JSON representation
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Language Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_GET_LANGUAGES,
        SLING_SERVLET_SELECTORS + EQUALS + JSON
    }
)
@SuppressWarnings("serial")
public class ListLanguagesServlet extends AbstractBaseServlet {

    private static final String RESOURCE_NOT_FOUND = "Resource not found";
    private static final String I18N_PATH = "/i18n/admin";
    private static final String LANGUAGES = "languages";

    @Override
    protected Response handleRequest(Request request) throws IOException, ServletException {
        Resource resource = request.getResourceByPath(I18N_PATH);

        if(resource == null) {
            return new ErrorResponse()
                .setHttpErrorCode(SC_BAD_REQUEST)
                .setErrorMessage(RESOURCE_NOT_FOUND)
                .setRequestPath(I18N_PATH);
        }

        List<Resource> languages = StreamSupport.stream(resource.getChildren().spliterator(), false)
        .collect(Collectors.toList());

        JsonResponse answer = new JsonResponse();
        answer.writeArray(LANGUAGES);

        for (Resource language : languages) {
            answer.writeObject();
            answer.writeAttribute(NAME, language.getName());
            answer.writeAttribute(TITLE, language.getValueMap().get(JCR_TITLE, String.class));
            answer.writeClose();
        }
        answer.writeClose();
        return answer;
    }

}
