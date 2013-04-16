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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author mr.olafsson
 */
public class CachedImageServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CachedImageServiceTest.class);

	private BasicImageService service;
	private TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void init() {
		Properties properties = new Properties();
		properties.setProperty("services.images.offline.path", "/tmp/twigkit/offline/images/");
		service = new CachedImageIOService(properties);
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
