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
		Assert.assertEquals(1728, secondPage.getWidth());
		Assert.assertEquals(2267, secondPage.getHeight());

		// Page 2 shares page 0's dimensions, so this actually exercises the
		// pixel comparison loop in imagesEqual rather than short-circuiting
		// on a width/height mismatch.
		Image thirdPage = TIFFUtils.getPage(sampleStream(), 2);

		Assert.assertNotNull(thirdPage);
		Assert.assertEquals(1728, thirdPage.getWidth());
		Assert.assertEquals(2266, thirdPage.getHeight());

		Assert.assertFalse("pages of identical dimensions should still have different content",
				imagesEqual(firstPage, thirdPage));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void multiPageOutOfRangeIndexIsRejected() throws IOException {
		int pageCount = countPages();

		TIFFUtils.getPage(sampleStream(), pageCount);
	}

	private int countPages() throws IOException {
		int pageCount = 0;

		try {
			while (true) {
				TIFFUtils.getPage(sampleStream(), pageCount);
				pageCount++;
			}
		} catch (IndexOutOfBoundsException e) {
			return pageCount;
		}
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
