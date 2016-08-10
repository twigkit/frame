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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author mr.olafsson
 */
public class CachedImageServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CachedImageServiceTest.class);
    private CachedImageIOService cachedService;
    private BasicImageService service;
    private TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void init() {
        Properties properties = new Properties();
        properties.setProperty("services.images.cache.name", "frame-cache");
        properties.setProperty("services.images.offline.path", "/tmp/twigkit/offline");
        service = new CachedImageIOService(properties);
        cachedService = new CachedImageIOService(properties);
    }

    @Test
    public void testContentType() {
        Assert.assertEquals("image/png", Image.ContentType.PNG.toString());
        Assert.assertEquals("image/jpeg", Image.ContentType.JPEG.toString());
        Assert.assertEquals("png", Image.ContentType.PNG.getSuffix());
        Assert.assertEquals("jpg", Image.ContentType.JPEG.getSuffix());
    }

    @Test
    public void testImageFromFile() {
        Assert.assertNotNull(getImage());
    }

    @Test
    public void testResizeImage() throws Exception {
        Image resized = service.resize(getImage(), 100, 0);

        Assert.assertEquals(100, resized.getWidth());
    }

    @Test
    public void testResizeTIFFImage() throws Exception {
        Image resized = service.resize(getImage("kodak.tif"), 450, 0);

        Assert.assertEquals(450, resized.getWidth());

        File f = new File(folder.getRoot(), "kodak-small.tif");
        try {
            service.write(resized, f);
        } catch (IOException e) {
            logger.error("Failed to write image", e);
        }

        Assert.assertTrue(f.length() > 6000 && f.length() < 6500);

        f.delete();
    }

    @Test
    public void testWriteImage() throws Exception {
        Image resized = service.resize(getImage(), 150, 0);

        File f = new File(folder.getRoot(), "myFile");
        try {
            service.write(resized, f);
        } catch (IOException e) {
            logger.error("Failed to write image", e);
        }

        Assert.assertTrue(f.length() > 50000 && f.length() < 60000);

        f.delete();
    }

    @Test
    public void testWriteOutputStream() throws Exception {
        Image resized = service.resize(getImage(), 150, 0);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            service.write(resized, stream);
        } catch (IOException e) {
            logger.error("Failed to write image", e);
        }

        Assert.assertTrue(stream.size() > 50000 && stream.size() < 60000);
    }

    @Ignore
    @Test
    public void testCaching() throws Exception {
        URL url = new URL("http://dev.twigkit.net/twigkit/services/images/resize/?cacheOriginal=false&url=http%3A%2F%2Fdesigningthesearchexperience.com%2Fimages%2Fcover%2Fdesigning-the-search-experience_large.jpg");
        Map<String,String> headers = new HashMap<String,String>(1);
        headers.put("COOKIE","_ga=GA1.2.1472257993.1470653684; SPRING_SECURITY_REMEMBER_ME_COOKIE=YWRyaWVuQHR3aWdraXQuY29tOjE0NzE5NTM0NDc5NTg6Mjk5M2U5ZDc1ZWQ0YjRiYjhlMzRlN2UwZjI1M2ZmNjM; JSESSIONID=DCBA4E64852DDFE819065BEB3AB61181");
        Image image = cachedService.fromURL(url,true,headers,0,200);
        image = cachedService.fromURL(url,true,headers,0,400);
        long time = System.currentTimeMillis();
        while(System.currentTimeMillis() < time + 6000L) {}
        image = cachedService.fromURL(url,true,headers,0,200);


    }
    private Image getImage() {
        return getImage("sample.jpg");
    }

    private Image getImage(String name) {
        try {
            return new ImageIOService().from(new java.io.FileInputStream(new File(this.getClass().getClassLoader().getResource(name).getFile())));
        } catch (IOException e) {
            logger.error("Failed to get image", e);
        }

        return null;
    }
}
