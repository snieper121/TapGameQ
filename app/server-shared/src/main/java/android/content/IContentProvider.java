package android.content;

import android.os.Bundle;
import android.os.RemoteException;

public interface IContentProvider {
    Bundle call(String attributeTag, String callingPkg, String authority, 
                String method, String arg, Bundle extras) throws RemoteException;
}
