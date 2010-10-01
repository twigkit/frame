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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author mr.olafsson
 */
public class CachedImageService extends ImageIOService {

	private static final Logger logger = LoggerFactory.getLogger(CachedImageService.class);

	public static final String WIDTH = "_w";
	public static final String HEIGHT = "_h";
	public static final String SERVICES_IMAGES_OFFLINE_PATH = "services.images.offline.path";

	private Properties properties;
	private File repository;

	public CachedImageService(Properties properties) {
		this.properties = properties;

		if (properties.containsKey(SERVICES_IMAGES_OFFLINE_PATH)) {
			String offlinePath = properties.getProperty(SERVICES_IMAGES_OFFLINE_PATH);

			repository = new File(offlinePath);

			if (!repository.exists()) {
				try {
					FileUtils.forceMkdir(repository);
					logger.info("CachedImageService created offline repository: " + repository.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			logger.info("CachedImageService offline path: " + offlinePath);
		} else {
			logger.error("CachedImageService failed to load. An offline path (" + SERVICES_IMAGES_OFFLINE_PATH + ") must be specified in twigkit.properties!");
		}
	}

	public Image fromURL(String urlAsString, int width, int height) throws IOException {
		if (urlAsString != null) {
			return fromURL(new URL(urlAsString), width, height);
		}

		return null;
	}

	public Image fromURL(URL url, int width, int height) throws IOException {
		if (repository != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Getting Image from cache [" + repository.getAbsolutePath() + "]");
			}
			File file = getFileFromURL(url, width, height);
			if (file.exists()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Getting Image [" + file.getName() + "] from cache");
				}
				BufferedImage buf = ImageIO.read(file);
				Image image = new Image(buf);
				return image;
			}
		}

		return resize(url, Image.fromURL(url), width, height);
	}

	private File getFileFromURL(URL url, int height, int width) {
		String filePath = getFileFromURLBySize(url, height, width);
		File file = new File(repository, filePath);

		return file;
	}

	private String getFileFromURLBySize(URL url, int width, int height) {
		StringBuilder buf = new StringBuilder();
		buf.append(DigestUtils.md5Hex(url.toString()));
		if (width > 0) {
			buf.append(WIDTH);
			buf.append(width);
		}

		if (height > 0) {
			buf.append(HEIGHT);
			buf.append(height);
		}
		
		buf.append(".");
		buf.append(Image.ContentType.PNG.getSuffix());

		return buf.toString();
	}

	public Image resize(final URL url, final Image image, final int newWidthInPixels, final int newHeightInPixels) throws IOException {
		Image resized = super.resize(image, newWidthInPixels, newHeightInPixels);

		if (repository != null) {
			File file = getFileFromURL(url, newWidthInPixels, newHeightInPixels);
			ImageIO.write(resized.getBufferedImage(), Image.ContentType.PNG.getSuffix(), file);

			if (logger.isDebugEnabled()) {
				logger.debug("Wrote Image [" + file.getName() + ", " + resized.getWidth() + "px by " + resized.getHeight() + "px] to cache");
			}
		}

		return resized;
	}
}
