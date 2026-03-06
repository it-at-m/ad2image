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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapEncoder;

import javax.naming.directory.Attribute;
import java.util.List;
import java.util.Optional;

public class DirectoryLookupService {

    private static final Logger log = LoggerFactory.getLogger(DirectoryLookupService.class);

    private final LdapTemplate ldapTemplate;
    private final AdConfigurationProperties adConfigurationProps;

    public DirectoryLookupService(LdapTemplate ldapTemplate, AdConfigurationProperties adConfigurationProps) {
        this.ldapTemplate = ldapTemplate;
        this.adConfigurationProps = adConfigurationProps;
    }

    /**
     * @param uid uid of a user
     * @return a {@link User}
     * @throws IncorrectResultSizeDataAccessException if there is more than one user with the given uid
     */
    public Optional<User> findUserInDirectory(String uid) {
        String userSearchFilter = this.adConfigurationProps.getUserSearchFilter();
        String safeUid = LdapEncoder.filterEncode(uid);
        String finalFilter = userSearchFilter.replace("{uid}", safeUid);
        String uidAttribute = this.adConfigurationProps.getUidAttribute();
        String mailAttribute = this.adConfigurationProps.getMailAttribute();
        String thumbnailPhotoAttribute = this.adConfigurationProps.getThumbnailPhotoAttribute();
        String snAttribute = this.adConfigurationProps.getSnAttribute();
        String givenNameAttribute = this.adConfigurationProps.getGivenNameAttribute();
        log.debug("Searching for user '{}' in AD ...", uid);
        List<User> searchResult = ldapTemplate.search(
                LdapQueryBuilder.query().base(this.adConfigurationProps.getUserSearchBase())
                        .filter(finalFilter),
                (AttributesMapper<User>) attributes -> {
                    User u = new User();
                    u.setUid((String) attributes.get(uidAttribute).get());
                    u.setEmail((String) attributes.get(mailAttribute).get());
                    Attribute thumbnailAttribute = attributes.get(thumbnailPhotoAttribute);
                    if (thumbnailAttribute != null) {
                        u.setThumbnailPhoto((byte[]) thumbnailAttribute.get());
                    }
                    Attribute snAttr = attributes.get(snAttribute);
                    if (snAttr != null) {
                        u.setSn((String) snAttr.get());
                    }
                    Attribute givenNameAttr = attributes.get(givenNameAttribute);
                    if (givenNameAttr != null) {
                        u.setGivenName((String) givenNameAttr.get());
                    }
                    return u;
                });
        if (searchResult.isEmpty()) {
            log.debug("No user '{}' found.", uid);
            return Optional.empty();
        } else if (searchResult.size() == 1) {
            User first = searchResult.getFirst();
            log.debug("Found user '{}' in directory, mail = '{}'.", uid, first.getEmail());
            return Optional.of(first);
        } else {
            throw new IncorrectResultSizeDataAccessException(1, searchResult.size());
        }
    }
}
