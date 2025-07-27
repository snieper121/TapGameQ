package android.content;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public interface IContentProvider extends IBinder {
    Bundle call(String attributeTag, String callingPkg, String authority, 
                String method, String arg, Bundle extras) throws RemoteException;
    
    Bundle call(Object attributionSource, String authority, String method, 
                String arg, Bundle extras) throws RemoteException;
}
