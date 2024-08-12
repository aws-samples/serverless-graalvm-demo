
package software.amazonaws.example.product;

import org.graalvm.nativeimage.hosted.Feature;
import software.amazon.awssdk.crt.CRT;

public class NativeFeature implements Feature {

    @Override
    public void afterImageWrite(AfterImageWriteAccess access) {
      new CRT();
      CRT.extractLibrary(access.getImagePath().getParent().toString());
    }
}