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

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    String uid;
    String email;
    byte[] thumbnailPhoto;
    String sn;
    String givenName;

    /**
     * Retrieves the user's unique identifier.
     *
     * @return the user's unique identifier (uid)
     */
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getThumbnailPhoto() {
        return thumbnailPhoto;
    }

    /**
     * Set the user's thumbnail photo data.
     *
     * @param thumbnailPhoto the image bytes for the user's thumbnail (e.g., JPEG/PNG), or `null` to
     *            clear it
     */
    public void setThumbnailPhoto(byte[] thumbnailPhoto) {
        this.thumbnailPhoto = thumbnailPhoto;
    }

    /**
     * Gets the user's surname (family name).
     *
     * @return the surname (family name) of the user, or {@code null} if not set
     */
    public String getSn() {
        return sn;
    }

    /**
     * Sets the user's surname (family name).
     *
     * @param sn the surname to set
     */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /**
     * Obtain the user's given (first) name.
     *
     * @return the given name, or {@code null} if not set
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Sets the user's given name (first name).
     *
     * @param givenName the given name to assign to the user
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

}
