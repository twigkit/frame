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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

/**
 * @author mr.olafsson
 */
public class ImageIOService implements ImageService {

    public Image fromURL(String urlAsString) throws IOException {
        if (urlAsString != null) {
            return fromURL(new URL(urlAsString));
        }

        return null;
    }

    public Image fromURL(URL url) throws IOException {
        Image image = new Image(ImageIO.read(url));
        image.setUrl(url);

        return image;
    }

    public Image from(InputStream inputStream) throws IOException {
        return new Image(ImageIO.read(inputStream));
    }

    public void write(Image image, File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        write(image, outputStream);
	    outputStream.close();
    }

    public void write(Image image, OutputStream outputStream) throws IOException {
        write(image, outputStream, Image.ContentType.PNG);

    }

    public void write(Image image, OutputStream outputStream, Image.ContentType contentType) throws IOException {
        if (image.getBufferedImage() != null) {
            ImageIO.write(image.getBufferedImage(), contentType.getSuffix(), outputStream);
        }
    }
}
