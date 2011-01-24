/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package twigkit.frame;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author mr.olafsson
 */
public class BasicImageIOService extends ImageIOService implements BasicImageService {

	public Image resize(Image image, int newWidthInPixels, int newHeightInPixels) throws Exception {
        int calcWidth = newWidthInPixels > 0 ? newWidthInPixels : (newHeightInPixels * image.getWidth() / image.getHeight());
        int calcHeight = newHeightInPixels > 0 ? newHeightInPixels : (newWidthInPixels * image.getHeight() / image.getWidth());

        int type = image.getBufferedImage().getType();
        if (type == BufferedImage.TYPE_CUSTOM) {
            // PNG images have a custom type, this will preserve alpha channel
            type = BufferedImage.TYPE_4BYTE_ABGR;
        }

        BufferedImage scaledBI = new BufferedImage(calcWidth, calcHeight, type);
        Graphics2D g = scaledBI.createGraphics();
//        g.clearRect(0, 0, calcWidth, calcHeight);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setComposite(AlphaComposite.Src);

        BufferedImage source = image.getBufferedImage();

        // Center cropping if necessary
        if (newWidthInPixels == newHeightInPixels && source.getWidth() != source.getHeight()) {
            int top = 0, left = 0, width = source.getWidth(), height = source.getHeight();
            if (width > source.getHeight()) {
                left = (width - height) / 2;
                width = height;
            } else {
                top = (height - width) / 2;
                height = width;
            }
            source = image.getBufferedImage().getSubimage(left, top, width, height);
        }

        g.drawImage(source, 0, 0, calcWidth, calcHeight, null);
        g.dispose();

        return new twigkit.frame.Image(scaledBI);
    }
}
