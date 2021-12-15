/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peregrine.admin.util;

import org.apache.sling.api.resource.Resource;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;

/**
 *
 * @author gaoha
 */
public class AdminUtil {
    public static Resource getPageResource(Resource resource) {
        Resource parentResource = resource;
        while (parentResource != null && !PAGE_PRIMARY_TYPE.equals(parentResource.getResourceType())) {
            parentResource = parentResource.getParent();
        }
        return parentResource;
    }
}
