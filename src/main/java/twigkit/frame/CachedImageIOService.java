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
public class CachedImageIOService extends BasicImageIOService {

	private static final Logger logger = LoggerFactory.getLogger(CachedImageIOService.class);

	public static final String WIDTH = "_w";
	public static final String HEIGHT = "_h";
	public static final String SERVICES_IMAGES_OFFLINE_PATH = "services.images.offline.path";

	private File repository;

    public CachedImageIOService() {
        // Make sure you call setOfflinePath();
    }

	public CachedImageIOService(Properties properties) {
		this(properties.getProperty(SERVICES_IMAGES_OFFLINE_PATH));
	}

	public CachedImageIOService(String offlinePath) {
        setOfflinePath(offlinePath);
	}

    public void setOfflinePath(String offlinePath) {
        if (offlinePath != null && offlinePath.length() > 0) {
            repository = new File(offlinePath);

            if (!repository.exists()) {
                try {
                    FileUtils.forceMkdir(repository);
                    logger.info("CachedImageIOService created offline repository: " + repository.getAbsolutePath());
                } catch (IOException e) {
                    logger.error("Failed to create offline repository", e);
                }
            }

            logger.info("CachedImageIOService offline path: " + repository.getAbsolutePath());
        } else {
            logger.info("CachedImageIOService disabled! An offline path must be specified!");
        }
    }

    @Override
    public Image fromURL(final URL url) throws IOException {
        return fromURL(url, true);
    }

	public Image fromURL(final URL url, boolean writeToCache) throws IOException {
		if (repository != null && repository.exists()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Getting Image from cache [" + repository.getAbsolutePath() + "]");
			}

			File file = getFileFromURL(url, 0, 0);

			if (file.exists()) {
				if (logger.isTraceEnabled()) {
					logger.trace("Getting Image [" + file.getName() + "] from cache");
				}
				BufferedImage buf = ImageIO.read(file);
				Image image = new Image(buf);
				image.setUrl(url);
				return image;
			} else {
				Image image = super.fromURL(url);
				image.setUrl(url);

                if (writeToCache) {
                    ImageIO.write(image.getBufferedImage(), Image.ContentType.PNG.getSuffix(), file);

                    if (logger.isTraceEnabled()) {
                        logger.trace("Wrote Image (original) [" + file.getName() + ", " + image.getWidth() + "px by " + image.getHeight() + "px] to cache");
                    }
                }

				return image;
			}
		}

		logger.warn("Failed to cache Image [" + url + "], returning without caching!");
		return super.fromURL(url);
	}

	private File getFileFromURL(URL url) {
		return getFileFromURL(url, 0, 0);
	}

	private File getFileFromURL(URL url, int height, int width) {
		String filePath = getFileNameFromURLBySize(url, height, width);
		File file = new File(repository, filePath);

		return file;
	}

	private String getFileNameFromURLBySize(URL url, int width, int height) {
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

	@Override
	public Image resize(final Image image, final int newWidthInPixels, final int newHeightInPixels) throws Exception {
		if (repository != null && repository.exists() && image.hasUrl()) {
			File file = getFileFromURL(image.getUrl(), newWidthInPixels, newHeightInPixels);

			if (file.exists()) {
				if (logger.isTraceEnabled()) {
					logger.trace("Getting Image [" + file.getName() + "] from cache");
				}
				BufferedImage buf = ImageIO.read(file);
				Image bufferedImage = new Image(buf);
				bufferedImage.setUrl(image.getUrl());

				return bufferedImage;
			} else {
				Image resized = super.resize(image, newWidthInPixels, newHeightInPixels);
				ImageIO.write(resized.getBufferedImage(), Image.ContentType.PNG.getSuffix(), file);

				if (logger.isTraceEnabled()) {
					logger.trace("Wrote Image [" + file.getName() + ", " + resized.getWidth() + "px by " + resized.getHeight() + "px] to cache");
				}

                return resized;
			}
		}

		logger.warn("Failed to cache Image [" + image.getUrl() + "], returning without caching!");
		return super.resize(image, newWidthInPixels, newHeightInPixels);
	}
}
