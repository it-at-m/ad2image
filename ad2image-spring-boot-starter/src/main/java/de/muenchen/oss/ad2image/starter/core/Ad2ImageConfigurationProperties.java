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

import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author michael.prankl
 *
 */
public class Ad2ImageConfigurationProperties {

    @NestedConfigurationProperty
    private AdConfigurationProperties ad;

    @NestedConfigurationProperty
    private ExchangeConfigurationProperties ews;

    /**
     * Enable/disable the integration.
     */
    private boolean enabled = true;

    public AdConfigurationProperties getAd() {
        return ad;
    }

    public void setAd(AdConfigurationProperties ad) {
        this.ad = ad;
    }

    public ExchangeConfigurationProperties getEws() {
        return ews;
    }

    public void setEws(ExchangeConfigurationProperties ews) {
        this.ews = ews;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
