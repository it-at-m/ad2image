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

/**
 * @author michael.prankl
 * @see <a href=
 *      "https://learn.microsoft.com/en-us/exchange/client-developer/web-service-reference/sizerequested">EWS
 *      SizeRequested</a>
 */
public enum ImageSize {
    HR64("HR64x64", 64),
    HR96("HR96x96", 96),
    HR120("HR120x120", 120),
    HR240("HR240x240", 240),
    HR360("HR360x360", 360),
    HR432(
            "HR432x432",
            432),
    HR504("HR504x504", 504),
    HR648("HR648x648", 648);

    private final String sizeRequestedValue;
    private final int sizePixels;

    private ImageSize(String sizeRequestedValue, int sizePixels) {
        this.sizeRequestedValue = sizeRequestedValue;
        this.sizePixels = sizePixels;
    }

    public String getSizeRequestedValue() {
        return sizeRequestedValue;
    }

    public int getSizePixels() {
        return sizePixels;
    }

    public static ImageSize getAdDefaultImageSize() {
        return ImageSize.HR64;
    }

}
