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
package twigkit.frame.util;


import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import twigkit.frame.Image;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author mr.olafsson
 */
public class TIFFUtils {

	public static Image getPage(String urlAsString, int page) throws IOException {
		return getPage(new URL(urlAsString), page);
	}

	public static Image getPage(URL url, int page) throws IOException {
		Image image = getPage(url.openStream(), page);
		image.setUrl(url);
		return image;
	}

	public static Image getPage(InputStream stream, int page) throws IOException {
		TIFFImageReader t = (TIFFImageReader) new TIFFImageReaderSpi().createReaderInstance();
		t.setInput(ImageIO.createImageInputStream(stream));

		Image image = new Image(t.read(page));

		t.dispose();
		stream.close();

		return image;
	}
}
