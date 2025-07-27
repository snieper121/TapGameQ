package android.content;

public class AttributionSource {
    public static class Builder {
        private int uid;
        private String attributionTag;
        private String packageName;
        
        public Builder(int uid) {
            this.uid = uid;
        }
        
        public Builder setAttributionTag(String tag) {
            this.attributionTag = tag;
            return this;
        }
        
        public Builder setPackageName(String pkg) {
            this.packageName = pkg;
            return this;
        }
        
        public AttributionSource build() {
            return new AttributionSource(uid, attributionTag, packageName);
        }
    }
    
    private int uid;
    private String attributionTag;
    private String packageName;
    
    public AttributionSource(int uid, String attributionTag, String packageName) {
        this.uid = uid;
        this.attributionTag = attributionTag;
        this.packageName = packageName;
    }
}
