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

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.net.URL;

/**
 * @author mr.olafsson
 */
public class Image implements Serializable {
	
	public static enum ContentType {

		PNG("image/png", "png"), JPEG("image/jpeg", "jpg"), TIFF("image/tiff", "tif"), UNKNOWN("application/octet-stream", "");

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
	private URL url;
	private ContentType type;

	public Image(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getBufferedImage() {
		return image;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(final URL url) {
		this.url = url;
	}

	public boolean hasUrl() {
		return url != null;
	}

	public int getHeight() {
		return image.getHeight();
	}

	public int getWidth() {
		return image.getWidth();
	}

	public ContentType getType() {
		return type;
	}

	public void setType(ContentType type) {
		this.type = type;
	}
}
