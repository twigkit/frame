/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package twigkit.frame;

import net.sf.ehcache.event.CacheEventListener;
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

import net.sf.ehcache.*;


/**
 * @author mr.olafsson
 */
public class CachedImageIOService extends BasicImageIOService {

    private static final Logger logger = LoggerFactory.getLogger(CachedImageIOService.class);

    public static final String WIDTH = "_w";
    public static final String HEIGHT = "_h";
    public static final String SERVICES_IMAGES_OFFLINE_PATH = "services.images.offline.path";
    public static final String CACHE_NAME_PROPERTY = "services.images.cache.name";
    private String cacheName;
    private Ehcache cache;
    private static CacheManager cacheManager;

    private File repository;

    public CachedImageIOService() {
        // Make sure you call setOfflinePath();
    }

    public CachedImageIOService(Properties properties) {
        setOfflinePath(properties.getProperty(SERVICES_IMAGES_OFFLINE_PATH));
        setCacheName(properties.getProperty(CACHE_NAME_PROPERTY));

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
                    repository = null;
                    return;
                }
            }

            logger.info("CachedImageIOService offline path: " + repository.getAbsolutePath());
        } else {
            logger.info("CachedImageIOService disabled! An offline path must be specified!");
        }
    }

    public void setCacheName(String cacheName) {
        if (cacheName != null & !cacheName.equals("")) {
            this.cacheName = cacheName;
            createCacheManager();
            cache = getOrCreateCache();
        }
    }

    public void createCacheManager() {
        URL url = getClass().getResource("/cache.xml");
        this.cacheManager = CacheManager.newInstance(url);
    }

    public Ehcache getOrCreateCache() {
        if (!cacheManager.cacheExists(cacheName)) {
            logger.debug("Creating cache from defaults!");
            cacheManager.addCache(cacheName);
            Cache cache = (Cache) cacheManager.getEhcache(cacheName);
            // TODO Seems to have no effect?
            cache.getCacheConfiguration().setCopyOnRead(true);
            cache.getCacheConfiguration().setCopyOnWrite(true);
            if (logger.isTraceEnabled()) {
//            if (!cache.isStatisticsEnabled()) {
//                cache.setStatisticsEnabled(true);
//            }
                logger.trace("Cache [" + cacheName + "] TimeToIdleSeconds: " + cache.getCacheConfiguration().getTimeToIdleSeconds());
            }
            CacheEventListener myListener = new CacheEventListenerImpl();
            cache.getCacheEventNotificationService().registerListener(myListener);
            return cache;
        } else {
            return cacheManager.getEhcache(cacheName);
        }
    }

    @Override
    public Image fromURL(final URL url) throws IOException {
        return fromURL(url, true);
    }

    public Image fromURL(final URL url, boolean useCache) throws IOException {
        boolean isMissing = false;
        String key = getKeyFromURLBySize(url, 0, 0);
        if (useCache && repository != null && repository.exists() && cache != null && cache.isKeyInCache(key)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Getting Image from cache [" + repository.getAbsolutePath() + "]");
            }

            File file = new File((String) cache.get(key).getObjectValue());

            if (file.exists()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Getting Image [" + file.getName() + "] from cache");
                }
                BufferedImage buf = ImageIO.read(file);
                Image image = new Image(buf);
                image.setUrl(url);
                return image;
            } else {
                isMissing = true;
            }
        }

        Image image = super.fromURL(url);
        image.setUrl(url);

        if (isMissing || (useCache && repository != null && repository.exists() && cache != null && (!cache.isKeyInCache(key)))) {

            File file = getFileFromURL(url);
            try {
                ImageIO.write(image.getBufferedImage(), Image.ContentType.PNG.getSuffix(), file);
                cache.put(new Element(key, file.getAbsolutePath()));

                if (logger.isTraceEnabled()) {
                    logger.trace("Wrote Image (original) [" + file.getName() + ", " + image.getWidth() + "px by " + image.getHeight() + "px] to cache");
                }
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to write image file into cache repository {} : {}", file.getAbsolutePath(), e.getStackTrace());
                }
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to write key-value to cache {} : {}", cache.getName(), e.getStackTrace());
                }
            }
            return image;
        }
        return image;

    }

    @Override
    public Image resize(final Image image, final int newWidthInPixels, final int newHeightInPixels) throws Exception {
        Boolean isMissing = false;
        String key = getKeyFromURLBySize(image.getUrl(), newWidthInPixels, newHeightInPixels);
        if (image.hasUrl() && repository != null && repository.exists() && cache != null && cache.isKeyInCache(key)) {
            File file = new File((String) cache.get(getKeyFromURLBySize(image.getUrl(), newWidthInPixels, newHeightInPixels)).getObjectValue());
            if (file.exists()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Getting resized Image [" + file.getName() + "] from cache");
                }
                BufferedImage buf = ImageIO.read(file);
                Image bufferedImage = new Image(buf);
                bufferedImage.setUrl(image.getUrl());
                return bufferedImage;
            } else {
                isMissing = true;
            }
        }

        Image resized = super.resize(image, newWidthInPixels, newHeightInPixels);

        if (isMissing || (image.hasUrl() && repository != null && repository.exists() && cache != null && (!cache.isKeyInCache(key)))) {
            File file = getFileFromURL(image.getUrl(), newWidthInPixels, newHeightInPixels);
            try {
                ImageIO.write(resized.getBufferedImage(), Image.ContentType.PNG.getSuffix(), file);
                cache.put(new Element(key, file.getAbsolutePath()));

                if (logger.isTraceEnabled()) {
                    logger.trace("Wrote Image [" + file.getName() + ", " + resized.getWidth() + "px by " + resized.getHeight() + "px] to cache");
                }
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to write image file into cache repository {} : {}", file.getAbsolutePath(), e.getStackTrace());
                }
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to write key-value to cache {} : {}", cache.getName(), e.getStackTrace());
                }
            }
        }
        return resized;
    }


    private File getFileFromURL(URL url) {
        return getFileFromURL(url, 0, 0);
    }

    private File getFileFromURL(URL url, int width, int height) {
        String filePath = getFileNameFromURLBySize(url, width, height);
        File file = new File(repository, filePath);

        return file;
    }

    private String getKeyFromURLBySize(URL url, int width, int height) {
        if (url == null) {
            return null;
        }
        return getFileFromURL(url, width, height).getAbsolutePath();
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

    public static boolean deleteFromRepository(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }
}
