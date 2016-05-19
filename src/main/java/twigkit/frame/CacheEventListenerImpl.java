package twigkit.frame;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.*;

/**
 * Created by a-flammarion on 19/05/2016.
 */
public class CacheEventListenerImpl implements net.sf.ehcache.event.CacheEventListener {
    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {
    }

    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {

    }

    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {

    }

    public void notifyElementExpired(Ehcache ehcache, Element element) {
        CachedImageIOService.deleteFromRepository((String)element.getObjectKey());
    }

    public void notifyElementEvicted(Ehcache ehcache, Element element) {

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
