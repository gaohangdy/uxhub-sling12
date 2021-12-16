/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peregrine.admin.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peregrine.adaption.PerPage;
import com.peregrine.admin.commons.Datasource;
import com.peregrine.commons.servlets.ServletHelper;
import static com.peregrine.commons.util.PerConstants.JSON;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import java.io.IOException;
import javax.servlet.Servlet;
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
            SLING_SERVLET_RESOURCE_TYPES + EQUALS + DatasourceColumnsServlet.RESOURCE_TYPE_DATASOURCE,
            SLING_SERVLET_SELECTORS + EQUALS + JSON
        }
)
@SuppressWarnings("serial")
public class DatasourceColumnsServlet extends SlingSafeMethodsServlet {

    public static final String RESOURCE_TYPE_DATASOURCE = "perapi/admin/datasourceColumns";
    
    private static final String COLUMNS_NODE = "columns";
    
    private static final Logger log = LoggerFactory.getLogger(DatasourceColumnsServlet.class);
    
    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response)
        throws IOException {
        
        Map<String, String> parameters = ServletHelper.obtainParameters(request);
        
        String path =parameters.get(PATH);
        Resource resource = request.getResourceResolver().resolve(path + "/" + COLUMNS_NODE);
        
        List<Datasource> datasourceList = new ArrayList<>();
        if (resource != null && resource.hasChildren()) {
            for (Resource res : resource.getChildren()) {
                String columnName = res.getValueMap().get("columnName", String.class) != null ? res.getValueMap().get("columnName", String.class) : res.getName();
                String title = columnName + " - " + res.getValueMap().get("datatype", String.class);
                datasourceList.add(new Datasource(columnName, title));

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
