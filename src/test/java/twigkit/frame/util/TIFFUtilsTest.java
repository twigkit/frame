package twigkit.frame.util;

import org.junit.Assert;
import org.junit.Test;
import twigkit.frame.Image;
import twigkit.frame.ImageIOService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author mr.olafsson
 */
public class TIFFUtilsTest {

	@Test
	public void multiPage() {
		try {
			Image img = TIFFUtils.getPage(new java.io.FileInputStream(new File(this.getClass().getClassLoader().getResource("multipage-sample.tif").getFile())), 1);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			new ImageIOService().write(img, stream);

			Assert.assertEquals(107489, stream.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
