package twigkit.frame.util;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twigkit.frame.Image;
import twigkit.frame.ImageIOService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author mr.olafsson
 */
public class TIFFUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(TIFFUtilsTest.class);

	@Test
	@Ignore
	public void multiPage() {
		try {
			Image img = TIFFUtils.getPage(new java.io.FileInputStream(new File(this.getClass().getClassLoader().getResource("multipage-sample.tif").getFile())), 1);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			new ImageIOService().write(img, stream);

			Assert.assertEquals(107489, stream.size());
		} catch (IOException e) {
			logger.error("Failed to load TIFF image", e);
		}
	}
}
