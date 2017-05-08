package com.peregrine.admin.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.peregrine.admin.servlets.ServletHelper.convertSuffixToParams;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=insert node at servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES + "=api/admin/insertNodeAt"
        }
)
@SuppressWarnings("serial")
public class InsertNodeAt extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(InsertNodeAt.class);

    @Reference
    ModelFactory modelFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {

        Map<String, String> params = convertSuffixToParams(request);
        String path = params.get("path");
        String component = params.get("component");
        if(component.startsWith("/apps")) {
            component = component.substring(component.indexOf('/', 1) + 1);
        }
        String drop = params.get("drop");
        log.debug(params.toString());

        try {
            Session session = request.getResourceResolver().adaptTo(Session.class);
            if ("into".equals(drop)) {
                Node node = session.getNode(path).addNode("n"+UUID.randomUUID(), "nt:unstructured");
                node.setProperty("sling:resourceType", component);
                session.save();
                response.sendRedirect(path+".model.json");
            } else if("before".equals(drop)) {
                Node node = session.getNode(path);
                Node parent = node.getParent();
                Node newNode = parent.addNode("n"+UUID.randomUUID(), "nt:unstructured");
                newNode.setProperty("sling:resourceType", component);
                parent.orderBefore(newNode.getName(), node.getName());
                session.save();
                response.sendRedirect(parent.getPath()+".model.json");

            } else if("after".equals(drop)) {
                Node node = session.getNode(path);
                Node parent = node.getParent();
                Node newNode = parent.addNode("n"+UUID.randomUUID(), "nt:unstructured");
                newNode.setProperty("sling:resourceType", component);
                Node after = null;
                for (NodeIterator it = parent.getNodes(); it.hasNext(); ) {
                    Node child = (Node) it.next();
                    if(child.getPath().equals(node.getPath())) {
                        if(it.hasNext()) {
                            after = (Node) it.next();
                            break;
                        }
                    }
                }
                if(after != null) {
                    // if we are inserting in the last position then we are already at the right place
                    parent.orderBefore(newNode.getName(), after.getName());
                }
                session.save();
                response.sendRedirect(parent.getPath()+".model.json");

            }
        } catch (RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace(response.getWriter());
        }

    }

}
