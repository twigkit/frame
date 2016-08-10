package twigkit.frame;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

/**
 * @author a-flammarion
 */
public class CacheEventListenerImpl implements CacheEventListener {
    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {
        CachedImageIOService.deleteFromRepository((String)element.getObjectKey());
    }

    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {

    }

    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {

    }

    public void notifyElementExpired(Ehcache ehcache, Element element) {
        CachedImageIOService.deleteFromRepository((String)element.getObjectKey());
    }

    public void notifyElementEvicted(Ehcache ehcache, Element element) {
        CachedImageIOService.deleteFromRepository((String)element.getObjectKey());
    }

    public void notifyRemoveAll(Ehcache ehcache) {

    }

    public void dispose() {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        throw new CloneNotSupportedException();
    }
}
