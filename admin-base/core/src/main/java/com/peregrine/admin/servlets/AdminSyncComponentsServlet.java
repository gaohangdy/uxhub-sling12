/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.peregrine.admin.servlets;

import static com.peregrine.admin.resource.AdminResourceHandlerService.COPY_FAILED;
import static com.peregrine.admin.resource.AdminResourceHandlerService.MISSING_PARENT_RESOURCE_FOR_COPY_SITES;
import static com.peregrine.admin.resource.AdminResourceHandlerService.SOURCE_SITE_DOES_NOT_EXIST;
import static com.peregrine.commons.ResourceUtils.getCopyableProperties;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import static com.peregrine.commons.util.PerConstants.APPS_ROOT;
import static com.peregrine.commons.util.PerConstants.COMPONENTS;
import static com.peregrine.commons.util.PerConstants.COMPONENT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.CONTENT_ROOT;
import static com.peregrine.commons.util.PerConstants.CREATED;
import static com.peregrine.commons.util.PerConstants.FROM_TENANT_NAME;
import static com.peregrine.commons.util.PerConstants.NAME;
import static com.peregrine.commons.util.PerConstants.OBJECTS;
import static com.peregrine.commons.util.PerConstants.OBJECT_DEFINITION_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SITE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_SUPER_TYPE;
import static com.peregrine.commons.util.PerConstants.SOURCE_PATH;
import static com.peregrine.commons.util.PerConstants.STATUS;
import static com.peregrine.commons.util.PerConstants.TO_TENANT_NAME;
import static com.peregrine.commons.util.PerConstants.TO_TENANT_TITLE;
import static com.peregrine.commons.util.PerConstants.TYPE;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static com.peregrine.commons.util.PerUtil.POST;
import static com.peregrine.commons.util.PerUtil.getNode;
import static com.peregrine.commons.util.PerUtil.getResource;
import static com.peregrine.commons.util.PerUtil.isPrimaryType;
import static com.peregrine.commons.util.PerUtil.loginService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author P0114764
 */
@Component(
        service = Servlet.class,
        property = {
            SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Admin Sync Components",
            SERVICE_VENDOR + EQUALS + PER_VENDOR,
            SLING_SERVLET_METHODS + EQUALS + POST,
            SLING_SERVLET_RESOURCE_TYPES + EQUALS + "perapi/admin/adminSyncComponents"
        }
)
@SuppressWarnings("serial")
public class AdminSyncComponentsServlet extends AbstractBaseServlet {

    private static final String FAILED_TO_CREATE_SITE = "Failed to create site";
    private static final String FAILED_TO_GET_SERVICE_RESOLVER = "Unable to get Peregrine Service Resolver";
    private static final String FAILED_TO_CREATE_TENANT_SECURITY = "Unable to create Tenant Permissions";
    private static final String DISABLE_USER_REASON = "Need to set a password first";
    private static final Pattern ANCHOR_SITE_REF_PATTERN = Pattern.compile("(<a .*?href=\"/content/)([a-z]+)/");

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {

        String fromTenant = request.getParameter(FROM_TENANT_NAME);
        String toTenant = request.getParameter(TO_TENANT_NAME);
        String title = request.getParameter(TO_TENANT_TITLE);
        boolean isAdmin = request.isAdmin();

        ResourceResolver resourceResolver = null;

        try {
            logger.trace("Update New Component form: '{}' to: '{}'", fromTenant, toTenant);
            resourceResolver = isAdmin
                    ? request.getResourceResolver()
                    : loginService(resourceResolverFactory, "peregrine-services");
            Session adminSession = resourceResolver.adaptTo(Session.class);
            UserManager userManager = AccessControlUtil.getUserManager(adminSession);

            Resource compontents = updateApps(resourceResolver, "/apps", fromTenant, toTenant);

            resourceResolver.commit();
            return new JsonResponse()
                    .writeAttribute(TYPE, SITE)
                    .writeAttribute(STATUS, CREATED)
                    .writeAttribute(NAME, toTenant)
                    //.writeAttribute(PATH, site.getPath())
                    .writeAttribute(SOURCE_PATH, CONTENT_ROOT + SLASH + fromTenant);
        } catch (LoginException e) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(FAILED_TO_GET_SERVICE_RESOLVER)
                    .setException(e);
        } catch (RepositoryException e) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(FAILED_TO_CREATE_TENANT_SECURITY)
                    .setException(e);
        } catch (Exception e) {
            return new ErrorResponse()
                    .setHttpErrorCode(SC_BAD_REQUEST)
                    .setErrorMessage(FAILED_TO_GET_SERVICE_RESOLVER)
                    .setException(e);
        } finally {
            if (!isAdmin && resourceResolver != null) {
                resourceResolver.close();
            }
        }
    }

    protected Resource updateApps(ResourceResolver resourceResolver, String tenantsParentPath, String fromName, String toName) throws Exception {

        final Resource compoentsRoot = getResource(resourceResolver, tenantsParentPath);

        if (compoentsRoot == null) {
            throw new Exception(MISSING_PARENT_RESOURCE_FOR_COPY_SITES);
        }
        final Resource source = getResource(compoentsRoot, fromName);
        if (source == null) {
            throw new Exception(String.format(SOURCE_SITE_DOES_NOT_EXIST, fromName));
        }

        Resource answer = getResource(compoentsRoot, toName);

        if (answer != null) {
            final Node rootNode = getNode(compoentsRoot);

            final List<String> superTypes = new ArrayList<>();

            final StructureCopier copier = new StructureCopier(resourceResolver, fromName, toName, answer);
            copier.copyApps(superTypes);

        }

        return answer;

    }

    private final class StructureCopier {

        private final ResourceResolver resourceResolver;
        private final String fromName;
        private final String toName;
        private final Resource sitesRoot;

        private String patternSlashName;
        private String patternNameSlash;
        private int patternLength;

        public StructureCopier(ResourceResolver resourceResolver, String fromName, String toName, Resource sitesRoot) {
            this.resourceResolver = resourceResolver;
            this.fromName = fromName;
            if (isEmpty(fromName)) {
                patternSlashName = null;
                patternNameSlash = null;
                patternLength = 0;
            } else {
                patternSlashName = SLASH + fromName;
                patternNameSlash = fromName + SLASH;
                patternLength = patternSlashName.length();
            }
            this.toName = toName;
            this.sitesRoot = sitesRoot;
        }

        public Resource copyApps(List<String> superTypes) {
            final String pathPrefix = APPS_ROOT + SLASH;
            final Resource source = getResource(resourceResolver, pathPrefix + fromName);
            if (source != null) {
                Resource target = getResource(resourceResolver, pathPrefix + toName);
                Resource answer = null;
                if (target == null) {
                    target = copyFolder(source, source.getParent(), toName);
                    answer = target;
                }
                if (target != null) {
                    // for each component in /apps/<fromTenant>/components create a stub component in /apps/<toTenant>/components
                    // with the sling:resourceSuperType set to the <fromTenant> component
                    superTypes.addAll(copyStubs(source, target, COMPONENTS));
                    // for each object in /apps/<fromTenant>/objects create a stub component in /apps/<toTenant>/objects
                    // with the sling:resourceSuperType set to the <fromTenant> object
                    copyStubs(source, target, OBJECTS);
                }
                return answer;
            }
            return null;
        }

        /**
         * Updates tenant path in anchor tags to reflect target tenant.
         *
         * @param htmlIn html to update
         * @return Rewritten HTML if there are matches, otherwise original html
         */
        private String updatePathsInAnchorTags(final String htmlIn) {
            Matcher matcher = ANCHOR_SITE_REF_PATTERN.matcher(htmlIn);
            StringBuffer htmlOut = new StringBuffer();

            while (matcher.find()) {
                matcher.appendReplacement(htmlOut, "$1" + toName + "/");
            }
            return matcher.appendTail(htmlOut).toString();
        }

        private Resource copyFolder(Resource folder, Resource targetParent, String folderName) {
            final Map<String, Object> newProperties = getCopyableProperties(folder);
            logger.trace("Resource Properties: '{}'", newProperties);
            try {
                return resourceResolver.create(targetParent, folderName, newProperties);
            } catch (PersistenceException e) {
                logger.warn(String.format(COPY_FAILED, folder.getName(), folder.getPath()), e);
            }
            return null;
        }

        private List<String> copyStubs(Resource source, Resource target, String folderName) {
            final List<String> superTypes = new ArrayList<>();
            final Resource appsSource = getResource(source, folderName);
            if (appsSource == null) {
                return superTypes;
            }
            Resource appsTarget = getResource(resourceResolver, target.getPath() + SLASH + folderName);
            if (appsTarget == null) {
                appsTarget = copyFolder(appsSource, target, folderName);
                if (appsTarget == null) {
                    return superTypes;
                }
            }
            for (Resource child : appsSource.getChildren()) {
                if (isPrimaryType(child, COMPONENT_PRIMARY_TYPE) || isPrimaryType(child, OBJECT_DEFINITION_PRIMARY_TYPE)) {
                    final ValueMap properties = child.getValueMap();
                    final Map<String, Object> newProperties = new HashMap<>(properties);
                    final String originalAppsPath = child.getPath().substring(APPS_ROOT.length() + 1);
                    superTypes.add(originalAppsPath);
                    newProperties.put(SLING_RESOURCE_SUPER_TYPE, originalAppsPath);
                    try {
                        resourceResolver.create(appsTarget, child.getName(), newProperties);
                    } catch (PersistenceException e) {
                        logger.warn(String.format(COPY_FAILED, folderName, child.getPath()), e);
                    }
                }
            }
            return superTypes;
        }
    }

}
