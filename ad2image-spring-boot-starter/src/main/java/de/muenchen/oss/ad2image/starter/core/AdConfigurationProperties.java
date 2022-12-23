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

import jakarta.validation.constraints.NotEmpty;

/**
 * @author michael.prankl
 */
@Validated
@ConfigurationProperties(prefix = "de.muenchen.oss.ad2image.ad")
public class AdConfigurationProperties {

    /**
     * Connection URL for AD server, for example 'ldaps://ad.mydomain.com:636'.
     */
    @NotEmpty
    private String url;

    /**
     * Bind User-DN for AD authentication.
     */
    private String userDn;

    /**
     * Password for AD authentication.
     */
    private String password;

    @NotEmpty
    private String uidAttribute = "uid";
    @NotEmpty
    private String mailAttribute = "mail";
    @NotEmpty
    private String thumbnailPhotoAttribute = "thumbnailPhoto";

    /**
     * User Search Base for user lookup, for example 'OU=Users,DC=mycompany,DC=com'.
     */
    @NotEmpty
    private String userSearchBase;

    /**
     * User Search filter, {uid} will be replaced with the requested user uid.
     */
    @NotEmpty
    private String userSearchFilter = "(&(objectClass=organizationalPerson)(cn={uid}))";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUidAttribute() {
        return uidAttribute;
    }

    public void setUidAttribute(String uidAttribute) {
        this.uidAttribute = uidAttribute;
    }

    public String getMailAttribute() {
        return mailAttribute;
    }

    public void setMailAttribute(String mailAttribute) {
        this.mailAttribute = mailAttribute;
    }

    public String getThumbnailPhotoAttribute() {
        return thumbnailPhotoAttribute;
    }

    public void setThumbnailPhotoAttribute(String thumbnailPhotoAttribute) {
        this.thumbnailPhotoAttribute = thumbnailPhotoAttribute;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public void setUserSearchBase(String userSearchBase) {
        this.userSearchBase = userSearchBase;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

}
