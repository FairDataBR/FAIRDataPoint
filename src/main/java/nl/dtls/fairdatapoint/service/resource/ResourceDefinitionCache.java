/**
 * The MIT License
 * Copyright © 2017 DTL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package nl.dtls.fairdatapoint.service.resource;

import lombok.Getter;
import lombok.Setter;
import nl.dtls.fairdatapoint.database.mongo.repository.ResourceDefinitionRepository;
import nl.dtls.fairdatapoint.entity.resource.ResourceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.dtls.fairdatapoint.config.CacheConfig.RESOURCE_DEFINITION_CACHE;
import static nl.dtls.fairdatapoint.config.CacheConfig.RESOURCE_DEFINITION_PARENT_CACHE;

@Service
public class ResourceDefinitionCache {

    @Getter
    private static class ResourceDefinitionParents {
        private final HashSet<ResourceDefinition> parents = new HashSet<>();

        public void add(ResourceDefinition rdParent) {
            parents.add(rdParent);
        }
    }

    @Autowired
    private ConcurrentMapCacheManager cacheManager;

    @Autowired
    private ResourceDefinitionRepository resourceDefinitionRepository;

    @PostConstruct
    public void computeCache() {
        // Get cache
        Cache cache = cache();
        Cache parentCache = parentCache();

        // Clear cache
        cache.clear();
        parentCache.clear();

        // Add to cache
        List<ResourceDefinition> rds = resourceDefinitionRepository.findAll();
        rds.forEach(rd -> {
            parentCache.put(rd.getUuid(), new ResourceDefinitionParents());
        });
        rds.forEach(rd -> {
            cache.put(rd.getUuid(), rd);
            rd.getChildren().forEach(c -> parentCache.get(c.getResourceDefinitionUuid(), ResourceDefinitionParents.class).add(rd));
        });
    }

    public ResourceDefinition getByUuid(String uuid) {
        return cache().get(uuid, ResourceDefinition.class);
    }

    public Set<ResourceDefinition> getParentsByUuid(String uuid) {
        var parents = parentCache().get(uuid, ResourceDefinitionParents.class);
        if (parents == null) {
            computeCache(); // Try to recompute cache (the object should be there)
            parents = parentCache().get(uuid, ResourceDefinitionParents.class);
            if (parents == null) {
                return Collections.emptySet();
            }
        }
        return parents.getParents();
    }

    private Cache cache() {
        return cacheManager.getCache(RESOURCE_DEFINITION_CACHE);
    }

    private Cache parentCache() {
        return cacheManager.getCache(RESOURCE_DEFINITION_PARENT_CACHE);
    }


}
