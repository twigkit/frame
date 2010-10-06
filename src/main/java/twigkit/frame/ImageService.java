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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author mr.olafsson
 */
public interface ImageService {

	Image fromURL(String urlAsString) throws IOException;

	Image fromURL(URL url) throws IOException;

	Image from(InputStream inputStream) throws IOException;

	Image resize(Image image, int newWidthInPixels, int newHeightInPixels) throws Exception;

	void write(Image image, File file) throws IOException;

	void write(Image image, OutputStream outputStream) throws IOException;

	void write(Image image, OutputStream outputStream, Image.ContentType contentType) throws IOException;
	
}
