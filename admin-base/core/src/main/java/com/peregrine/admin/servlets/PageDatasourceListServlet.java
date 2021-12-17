/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peregrine.admin.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.adaption.PerPage;
import com.peregrine.admin.commons.Datasource;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.commons.servlets.ServletHelper;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.sling.api.resource.Resource;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import org.osgi.service.component.annotations.Component;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static com.peregrine.commons.util.PerConstants.PATH;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gaoha
 */
@Component(
        service = Servlet.class,
        property = {
            SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "List Servlet",
            SERVICE_VENDOR + EQUALS + PER_VENDOR,
            SLING_SERVLET_METHODS + EQUALS + GET,
            SLING_SERVLET_RESOURCE_TYPES + EQUALS + PageDatasourceListServlet.RESOURCE_TYPE_DATASOURCE,
            SLING_SERVLET_SELECTORS + EQUALS + JSON
        }
)
@SuppressWarnings("serial")
public class PageDatasourceListServlet extends SlingSafeMethodsServlet {

    public static final String RESOURCE_TYPE_DATASOURCE = "perapi/admin/pageDatasourceList";
    
    private static final Logger log = LoggerFactory.getLogger(PageDatasourceListServlet.class);
    
    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response)
        throws IOException {
        
        Map<String, String> parameters = ServletHelper.obtainParameters(request);
        
        String path =parameters.get(PATH);
        Resource resource = request.getResourceResolver().resolve(path);
        PerPage page = resource.adaptTo(PerPage.class);
        
        Resource datasourceRs = page.getContentResource().getChild("datasources");
        
        List<Datasource> datasourceList = new ArrayList<>();
        if (datasourceRs != null) {
            Map<String, String> mp = new HashMap<>();
            for (Resource res : datasourceRs.getChildren()) {
                String datasourcePath = res.getValueMap().get("datasource", String.class);
                Resource dsResource = request.getResourceResolver().resolve(datasourcePath);
                String title = dsResource.getValueMap().get("name", String.class);
                datasourceList.add(new Datasource(datasourcePath, title));

            }
        }   
        
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(response.getWriter(), datasourceList);
        } catch (IOException e) {
            log.error("ObjectMapper error",e.getMessage());
        }          
    }
}
