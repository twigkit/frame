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
package twigkit.frame.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author mr.olafsson
 */
public class FunctionsTest {

	@Test
	public void testUseImageService() {
		String HTML = "Html with inline images like <img src=\"http://wikipedia.org/image.png\" height=\"100\" width=\"200\" /> other tags like <a href=\"\">text</a> and additional <img src='http://anotherurl/image.jpg' style=\"border: 0px;\">.";
		System.out.println(Functions.useImageService(HTML, "/twigkit/services/images/resize/", 100, 200));
		Assert.assertEquals("Html with inline images like <img src=\"/twigkit/services/images/resize/?width=100&height=200&url=http://wikipedia.org/image.png\" width=\"100\" height=\"200\" /> other tags like <a href=\"\">text</a> and additional <img src=\"/twigkit/services/images/resize/?width=100&height=200&url=http://anotherurl/image.jpg\" width=\"100\" height=\"200\" />.", Functions.useImageService(HTML, "/twigkit/services/images/resize/", 100, 200));
	}
}
