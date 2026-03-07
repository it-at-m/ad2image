/*
 * The MIT License
 * Copyright © 2022 Landeshauptstadt München | it@M
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
package de.muenchen.oss.ad2image.starter.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author michael.prankl
 */
@Validated
@ConfigurationProperties(prefix = "de.muenchen.oss.ad2image.gravatar")
public class GravatarConfigurationProperties {

    /**
     * Enables/disables the gravatar compatability endpoint.
     */
    private boolean enabled;

    /**
     * cron expression for periodic refresh of the SHA256 email address hashes, "-" to disable.
     */
    private String hashCacheRefreshCron = "-";

    /**
     * LDAP search filter for users which should be included in generation of SHA256-hashed email
     * addresses, e.g.
     * '(&(objectClass=organizationalPerson)(mail=*))'
     */
    private String mapPopulationFilter = "(&(objectClass=organizationalPerson)(mail=*))";

    /**
     * page size for retrieval of users
     */
    private Integer pageSize = 500;

    /**
     * Default mode for Gravatar API when the requested 'd=' parameter is unsupported or missing.
     * This is independent from the main avatar API's default mode setting.
     * Defaults to M_FALLBACK_GENERIC (generic silhouette).
     */
    private Mode defaultMode = Mode.M_FALLBACK_GENERIC;

    /**
     * Whether the Gravatar compatibility endpoint is enabled.
     *
     * @return `true` if the gravatar compatibility endpoint is enabled, `false` otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHashCacheRefreshCron() {
        return hashCacheRefreshCron;
    }

    public void setHashCacheRefreshCron(String hashCacheRefreshCron) {
        this.hashCacheRefreshCron = hashCacheRefreshCron;
    }

    public String getMapPopulationFilter() {
        return mapPopulationFilter;
    }

    public void setMapPopulationFilter(String mapPopulationFilter) {
        this.mapPopulationFilter = mapPopulationFilter;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size used when retrieving users.
     *
     * @param pageSize the number of users to request per page; may be {@code null} to use the configured default (500)
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * The default Gravatar API mode used when the requested `d=` parameter is missing or not supported.
     *
     * @return the configured default `Mode` (used as the fallback for Gravatar `d=`), typically `Mode.M_FALLBACK_GENERIC`
     */
    public Mode getDefaultMode() {
        return defaultMode;
    }

    /**
     * Sets the default Gravatar API mode used when a request's `d=` parameter is unsupported or missing.
     *
     * @param defaultMode the `Mode` to use as the Gravatar endpoint default
     */
    public void setDefaultMode(Mode defaultMode) {
        this.defaultMode = defaultMode;
    }
}
