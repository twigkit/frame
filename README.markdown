Frame - The Java Image Service
========

**Simple API and Web Service for resizing and cropping images.**

## Troubleshooting

### Image services failing for TIFF images on a Windows machine

The standard JDK for Windows only ships with a limited number of image codecs, which are needed for manipulating images in Frame. This results in image services failing for certain image types, such as TIFF. A typical symptom is a null-pointer exception when Frame is trying to read the image data, e.g.

    java.lang.NullPointerException
	    at twigkit.frame.Image.getWidth(Image.java:77)
	    at twigkit.frame.BasicImageIOService.resize(BasicImageIOService.java:26)

To solve this issue, we recommend installing the [Java Advanced Imaging Image I/O Tools](http://download.java.net/media/jai-imageio/builds/release/1.1/INSTALL-jai_imageio.html#Windows) which provide a number of image codecs missing from the standard Sun JDK for Windows. You should either install the [JDK](http://download.java.net/media/jai-imageio/builds/release/1.1/jai_imageio-1_1-lib-windows-i586-jdk.exe) or the [JRE](http://download.java.net/media/jai-imageio/builds/release/1.1/jai_imageio-1_1-lib-windows-i586-jre.exe) version of the toolkit, depending on whether you are using the JRE or JDK in your runtime environment. Once you've downloaded the executable, you should copy it to the server that will be running Frame and follow the install instructions. Once installed, you need to restart any application that is using Frame services, to get the new codecs into the runtime.