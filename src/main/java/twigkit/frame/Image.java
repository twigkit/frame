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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

/**
 * @author mr.olafsson
 */
public class Image implements Serializable {

	public static enum ContentType {

		PNG("image/png", "png"), JPEG("image/jpeg", "jpg");

		private String contentType;
		private String suffix;

		ContentType(String contentType, String suffix) {
			this.contentType = contentType;
			this.suffix = suffix;
		}

		@Override
		public String toString() {
			return contentType;
		}

		public String getSuffix() {
			return suffix;
		}
	}
	
	private BufferedImage image;

	public Image(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getBufferedImage() {
		return image;
	}

	public int getHeight() {
		return image.getHeight();
	}

	public int getWidth() {
		return image.getWidth();
	}

	public static Image fromURL(String urlAsString) throws IOException {
		if (urlAsString != null) {
			return fromURL(new URL(urlAsString));
		}

		return null;
	}

	public static Image fromURL(URL url) throws IOException {
		return new Image(ImageIO.read(url));
	}

	public static Image from(InputStream inputStream) throws IOException {
		return new Image(ImageIO.read(inputStream));
	}
}
