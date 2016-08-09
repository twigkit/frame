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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
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
        return fromURL(url, useCache, null);
    }

    public Image fromURL(final URL url, final boolean useCache, final Map<String, String> headers) throws IOException {
        return fromURL(url, useCache, headers, 0, 0);
    }

    public Image fromURL(final URL url, final boolean useCache, final Map<String, String> headers, final int newWidthInPixels, final int newHeightInPixels) throws IOException {
        boolean isMissing = true;
        Image image;

        if (useCache && repository != null && repository.exists() && cache != null) {

            if (newHeightInPixels > 0 || newHeightInPixels > 0) {

                //Retrieve the image from cache using given size
                String key = getKeyFromURLBySize(url, headers, newWidthInPixels, newHeightInPixels);
                if (cache.isKeyInCache(key)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Getting resized image from cache [" + repository.getAbsolutePath() + "]");
                    }

                    File file = new File((String) cache.get(key).getObjectValue());

                    if (file.exists()) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Found resized image [" + file.getName() + "] from cache");
                        }
                        BufferedImage buf = ImageIO.read(file);
                        image = new Image(buf);
                        image.setUrl(url);
                        return image;
                    }
                    logger.trace("Failed to retrieve resized image from cache");
                }

                //Retrieve the image from cache using original size
                key = getKeyFromURLBySize(url, headers, 0, 0);
                if (cache.isKeyInCache(key)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Getting original image from cache [" + repository.getAbsolutePath() + "]");
                    }
                    File file = new File((String) cache.get(key).getObjectValue());

                    if (file.exists()) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Found original image [" + file.getName() + "] from cache");
                        }
                        BufferedImage buf = ImageIO.read(file);
                        image = new Image(buf);
                        image.setUrl(url);
                        try {
                            //Now try to resize the image
                            image = super.resize(image, newWidthInPixels, newHeightInPixels);
                            String resizedKey = getKeyFromURLBySize(url, headers, newWidthInPixels, newHeightInPixels);
                            File resizedFile = getFileFromURL(url, headers, newWidthInPixels, newHeightInPixels);
                            try {
                                ImageIO.write(image.getBufferedImage(), Image.ContentType.PNG.getSuffix(), resizedFile);
                                cache.put(new Element(resizedKey, resizedFile.getAbsolutePath()));
                                if (logger.isTraceEnabled()) {
                                    logger.trace("Wrote resized image [" + file.getName() + ", " + image.getWidth() + "px by " + image.getHeight() + "px] to cache");
                                }
                            } catch (IOException e) {
                                if (logger.isErrorEnabled()) {
                                    logger.error("Failed to write image file into cache repository {} : {}", file.getAbsolutePath(), e);
                                }
                            } catch (Exception e) {
                                if (logger.isErrorEnabled()) {
                                    logger.error("Failed to write key-value to cache {} : {}", cache.getName(), e);
                                }
                            }
                            //In all cases, return resized image to caller
                            finally {
                                return image;
                            }
                        } catch (Exception e) {
                            logger.error("Caught an exception whilst resizing image: {}", e);
                            //return original image anyway
                            return image;
                        }
                    }
                }

                //Key wasn't in cache or file no longer exists on disk. Need to go and retrieve original file
                image = getOriginalImage(url, headers);

                //Try to cache the original image
                File file = getFileFromURL(url, headers, newWidthInPixels, newHeightInPixels);
                writeToCache(image, headers, file);
                try {
                    //Now try to resize the image
                    image = super.resize(image, newWidthInPixels, newHeightInPixels);
                    String resizedKey = getKeyFromURLBySize(url, headers, newWidthInPixels, newHeightInPixels);
                    File resizedFile = getFileFromURL(url, headers, newWidthInPixels, newHeightInPixels);
                    try {
                        ImageIO.write(image.getBufferedImage(), Image.ContentType.PNG.getSuffix(), resizedFile);
                        cache.put(new Element(resizedKey, resizedFile.getAbsolutePath()));
                        if (logger.isTraceEnabled()) {
                            logger.trace("Wrote resized image [" + file.getName() + ", " + image.getWidth() + "px by " + image.getHeight() + "px] to cache");
                        }
                    } catch (IOException e) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Failed to write image file into cache repository {} : {}", file.getAbsolutePath(), e);
                        }
                    } catch (Exception e) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Failed to write key-value to cache {} : {}", cache.getName(), e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Caught an exception whilst resizing image: {}", e);
                } finally {
                    //return original or resized image anyway
                    return image;
                }

            }

            //No need to resize. Try to get from cache otherwise get it from the source and cache it
            String key = getKeyFromURLBySize(url, headers, 0, 0);
            if (cache.isKeyInCache(key)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Getting original image from cache [" + repository.getAbsolutePath() + "]");
                }
                File file = new File((String) cache.get(key).getObjectValue());

                if (file.exists()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Found original image [" + file.getName() + "] from cache");
                    }
                    BufferedImage buf = ImageIO.read(file);
                    image = new Image(buf);
                    image.setUrl(url);
                    return image;
                }
            }
            //Key wasn't in cache or file no longer exists on disk. Need to go and retrieve original file
            image = getOriginalImage(url, headers);

            //Try to cache the original image
            File file = getFileFromURL(url, headers, 0, 0);
            writeToCache(image, headers, file);
            return image;
        } else {
            image = getOriginalImage(url, headers);
            if(newWidthInPixels > 0 || newHeightInPixels > 0) {
                try {
                    image = super.resize(image, newWidthInPixels, newHeightInPixels);
                } catch (Exception e) {
                    logger.error("Caught an exception whilst resizing image: {}", e);
                }
            }
            //return original or resized image anyway
            return image;
        }
    }


    private void writeToCache(Image image, Map<String, String> headers, File file) {
        try {
            ImageIO.write(image.getBufferedImage(), Image.ContentType.PNG.getSuffix(), file);
            cache.put(new Element(getFileFromURL(image.getUrl(),headers), file.getAbsolutePath()));
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to write image file into cache repository {} : {}", file.getAbsolutePath(), e);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to write key-value to cache {} : {}", cache.getName(), e);
            }
        }
    }

    private Image getOriginalImage(URL url, Map<String, String> headers) throws java.io.IOException {
        Image image;
        if (headers != null && headers.size() > 0) {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            for (Map.Entry<String, String> header : headers.entrySet()) {
                urlConnection.setRequestProperty(header.getKey(), header.getValue());
            }
            urlConnection.setUseCaches(false);
            image = super.from(urlConnection.getInputStream());
        } else {
            image = super.fromURL(url);
        }
        image.setUrl(url);
        return image;
    }

//    @Override
//    public Image resize(final Image image, final int newWidthInPixels, final int newHeightInPixels) throws Exception {
//        Boolean isMissing = false;
//        String key = getKeyFromURLBySize(image.getUrl(), newWidthInPixels, newHeightInPixels);
//        if (image.hasUrl() && repository != null && repository.exists() && cache != null && cache.isKeyInCache(key)) {
//            File file = new File((String) cache.get(getKeyFromURLBySize(image.getUrl(), newWidthInPixels, newHeightInPixels)).getObjectValue());
//            if (file.exists()) {
//                if (logger.isTraceEnabled()) {
//                    logger.trace("Getting resized Image [" + file.getName() + "] from cache");
//                }
//                BufferedImage buf = ImageIO.read(file);
//                Image bufferedImage = new Image(buf);
//                bufferedImage.setUrl(image.getUrl());
//                return bufferedImage;
//            } else {
//                isMissing = true;
//            }
//        }

//        return super.resize(image, newWidthInPixels, newHeightInPixels);

//        if (isMissing || (image.hasUrl() && repository != null && repository.exists() && cache != null && (!cache.isKeyInCache(key)))) {
//            File file = getFileFromURL(image.getUrl(), newWidthInPixels, newHeightInPixels);
//            try {
//                ImageIO.write(resized.getBufferedImage(), Image.ContentType.PNG.getSuffix(), file);
//                cache.put(new Element(key, file.getAbsolutePath()));
//
//                if (logger.isTraceEnabled()) {
//                    logger.trace("Wrote Image [" + file.getName() + ", " + resized.getWidth() + "px by " + resized.getHeight() + "px] to cache");
//                }
//            } catch (IOException e) {
//                if (logger.isErrorEnabled()) {
//                    logger.error("Failed to write image file into cache repository {} : {}", file.getAbsolutePath(), e.getStackTrace());
//                }
//            } catch (Exception e) {
//                if (logger.isErrorEnabled()) {
//                    logger.error("Failed to write key-value to cache {} : {}", cache.getName(), e.getStackTrace());
//                }
//            }
//        }
//        return resized;
//    }


    private File getFileFromURL(URL url) {
        return getFileFromURL(url, null, 0, 0);
    }

    private File getFileFromURL(URL url, Map<String, String> headers) {
        return getFileFromURL(url, headers, 0, 0);
    }

    private File getFileFromURL(URL url, int width, int height) {
        return getFileFromURL(url, null, width, height);
    }

    private File getFileFromURL(URL url, Map<String, String> headers, int width, int height) {
        String filePath = getFileNameFromURLBySize(url, headers, width, height);
        return new File(repository, filePath);
    }

    private String getKeyFromURLBySize(URL url, int width, int height) {
        return getKeyFromURLBySize(url, null, width, height);
    }

    private String getKeyFromURLBySize(URL url, Map<String, String> headers, int width, int height) {
        if (url == null) {
            return null;
        }
        return getFileFromURL(url, headers, width, height).getAbsolutePath();
    }

    private String getFileNameFromURLBySize(URL url, int width, int height) {
        return getFileNameFromURLBySize(url, null, width, height);
    }

    private String getFileNameFromURLBySize(URL url, Map<String, String> headers, int width, int height) {
        StringBuilder buf = new StringBuilder();
        String s = url.toString();

        //Use the headers to generate
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry e : headers.entrySet()) {
                s = s.concat((String) e.getKey()).concat((String) e.getValue());
            }
        }
        buf.append(DigestUtils.md5Hex(s));
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
