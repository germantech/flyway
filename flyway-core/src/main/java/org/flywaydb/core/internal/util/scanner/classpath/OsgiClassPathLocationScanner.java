/**
 * Copyright 2010-2015 Axel Fontaine
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.util.scanner.classpath;

import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

/**
 * OSGi specific scanner that performs the migration search in
 * the current bundle's classpath.
 *
 * <p>
 * The resources that this scanner returns can only be loaded if
 * Flyway's ClassLoader has access to the bundle that contains the migrations.
 * </p>
 */
public class OsgiClassPathLocationScanner implements ClassPathLocationScanner {
	
	final private Bundle hostBundle;

    public OsgiClassPathLocationScanner(Bundle hostBundle) {
		super();
		this.hostBundle = hostBundle;
	}

	public Set<String> findResourceNames(String location, URL locationUrl) throws IOException {
        Set<String> resourceNames = new TreeSet<String>();

//      Bundle bundle = FrameworkUtil.getBundle(getClass());
        @SuppressWarnings({"unchecked"})
        Enumeration<URL> entries = hostBundle.findEntries(locationUrl.getPath(), "*", true);

        if (entries != null) {
            while (entries.hasMoreElements()) {
                URL entry = entries.nextElement();
                String resourceName = getPathWithoutLeadingSlash(entry);

                resourceNames.add(resourceName);
            }
        }
        
    	// Try loading any classes at this location with bundle wiring
    	BundleRevision bRevision = (BundleRevision) hostBundle.adapt(BundleRevision.class);
    	BundleWiring wiring = bRevision.getWiring();
    	Collection<String> classResources = wiring.listResources(locationUrl.getPath(), null, BundleWiring.LISTRESOURCES_RECURSE);
    	// If any class resources were found, add them
    	if(classResources != null)
    		resourceNames.addAll(classResources);

        return resourceNames;
    }
	
    private String getPathWithoutLeadingSlash(URL entry) {
        final String path = entry.getPath();

        return path.startsWith("/") ? path.substring(1) : path;
    }
}
