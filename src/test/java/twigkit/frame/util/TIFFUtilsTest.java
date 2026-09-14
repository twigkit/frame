package twigkit.frame.util;

import org.junit.Assert;
import org.junit.Test;
import twigkit.frame.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author mr.olafsson
 */
public class TIFFUtilsTest {

	@Test
	public void multiPage() throws IOException {
		Image firstPage = TIFFUtils.getPage(sampleStream(), 0);

		Assert.assertNotNull(firstPage);
		Assert.assertEquals(1728, firstPage.getWidth());
		Assert.assertEquals(2266, firstPage.getHeight());

		Image secondPage = TIFFUtils.getPage(sampleStream(), 1);

		Assert.assertNotNull(secondPage);
		Assert.assertTrue(secondPage.getWidth() > 0);
		Assert.assertTrue(secondPage.getHeight() > 0);

		Assert.assertFalse("second page should have different content to the first",
				imagesEqual(firstPage, secondPage));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void multiPageOutOfRangeIndexIsRejected() throws IOException {
		TIFFUtils.getPage(sampleStream(), Integer.MAX_VALUE);
	}

	private boolean imagesEqual(Image a, Image b) {
		java.awt.image.BufferedImage imageA = a.getBufferedImage();
		java.awt.image.BufferedImage imageB = b.getBufferedImage();

		if (imageA.getWidth() != imageB.getWidth() || imageA.getHeight() != imageB.getHeight()) {
			return false;
		}

		for (int y = 0; y < imageA.getHeight(); y++) {
			for (int x = 0; x < imageA.getWidth(); x++) {
				if (imageA.getRGB(x, y) != imageB.getRGB(x, y)) {
					return false;
				}
			}
		}

		return true;
	}

	private FileInputStream sampleStream() throws IOException {
		return new FileInputStream(new File(this.getClass().getClassLoader().getResource("multipage-sample.tif").getFile()));
	}
}
